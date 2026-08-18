/**
 * [BoxLang]
 *
 * Copyright [2023] [Ortus Solutions, Corp]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package ortus.boxlang.markdown;

import static com.google.common.truth.Truth.assertThat;

import com.vladsch.flexmark.ext.emoji.EmojiExtension;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.scopes.Key;

/**
 * This loads the module and runs an integration test on the module.
 */
public class IntegrationTest extends BaseIntegrationTest {

	@DisplayName( "Test the module loads in BoxLang" )
	@Test
	public void testModuleLoads() {
		assertThat( moduleService.getRegistry().containsKey( moduleName ) ).isTrue();

		// @formatter:off
		runtime.executeSource(
		    """
			result = markdown( "#### Hello World" )
			println( result )

			bx:markdown variable="result2"{
				writeoutput( "#### Hello World" )
			}
			println( result2 )

			bx:markdown{
				writeoutput( "#### Hola Mundo" )
			}
			""",
		    context
		);
		// @formatter:on

		// Asserts here
		assertThat( variables.get( "result" ) ).isNotNull();
		assertThat( variables.getAsString( Key.result ).trim() )
		    .isEqualTo( "<h2 id=\"hello-world\"><a href=\"#hello-world\" id=\"hello-world\" name=\"hello-world\" class=\"anchor\"></a>Hello World</h2>" );

	}

	/**
	 * Toggles one setting on for the duration of the given work, then restores
	 * it and forces a rebuild - so a new setting's own test never leaks its
	 * effect into any other test in this class, regardless of execution order.
	 * Goes entirely through BoxLang script execution (dynamic dispatch), never
	 * a raw Java cast to MarkdownService - the module is loaded from its own
	 * packaged jar under a separate classloader from this test's own compiled
	 * classes, so a `(MarkdownService)` cast in Java here would ClassCastException
	 * even though it's "the same class" by source.
	 */
	private void withSetting( String settingName, String enabledValue, String disabledValue, Runnable work ) {
		toggleSetting( settingName, enabledValue );
		try {
			work.run();
		} finally {
			toggleSetting( settingName, disabledValue );
		}
	}

	private void toggleSetting( String settingName, String value ) {
		runtime.executeSource(
		    "getBoxRuntime().getGlobalService( 'MarkdownService' ).getSettings()[ '" + settingName + "' ] = " + value + ";" +
		        "getBoxRuntime().getGlobalService( 'MarkdownService' ).resetBuilders();",
		    context
		);
	}

	@DisplayName( "Test admonition blocks are off by default" )
	@Test
	public void testAdmonitionOffByDefault() {
		runtime.executeSource( "result = markdown( '!!! note \"Heads Up\"\n    Body text.' )", context );
		// with the extension off, the !!! line is just an unrecognized paragraph
		assertThat( variables.getAsString( Key.result ) ).doesNotContain( "admonition" );
	}

	@DisplayName( "Test admonition blocks render as a titled div when enabled, with nested markdown intact" )
	@Test
	public void testAdmonitionEnabled() {
		withSetting( "enableAdmonition", "true", "false", () -> {
			runtime.executeSource(
			    "result = markdown( '!!! note \"Heads Up\"\n    Some **bold** text.' )",
			    context
			);
			String html = variables.getAsString( Key.result );
			assertThat( html ).contains( "adm-block adm-note" );
			assertThat( html ).contains( "Heads Up" );
			assertThat( html ).contains( "<strong>bold</strong>" );
		} );
	}

	@DisplayName( "Test collapsible admonition (???) blocks render with an adm-collapsed marker class when enabled" )
	@Test
	public void testCollapsibleAdmonitionEnabled() {
		withSetting( "enableAdmonition", "true", "false", () -> {
			runtime.executeSource(
			    "result = markdown( '??? tip \"Optional\"\n    Collapsed by default.' )",
			    context
			);
			assertThat( variables.getAsString( Key.result ) ).contains( "adm-collapsed" );
		} );
	}

	@DisplayName( "Test footnotes are off by default and render as references + a footnote list when enabled" )
	@Test
	public void testFootnotes() {
		String markdown = "Here is a claim[^1].\n\n[^1]: The source for that claim.";

		runtime.executeSource( "result = markdown( '" + markdown + "' )", context );
		assertThat( variables.getAsString( Key.result ) ).doesNotContain( "footnote" );

		withSetting( "enableFootnotes", "true", "false", () -> {
			runtime.executeSource( "result = markdown( '" + markdown + "' )", context );
			String html = variables.getAsString( Key.result );
			assertThat( html ).contains( "footnote" );
			assertThat( html ).contains( "The source for that claim." );
		} );
	}

	@DisplayName( "Test definition lists are off by default and render as <dl>/<dt>/<dd> when enabled" )
	@Test
	public void testDefinitionLists() {
		String markdown = "Term\n:   Definition text.";

		runtime.executeSource( "result = markdown( '" + markdown + "' )", context );
		assertThat( variables.getAsString( Key.result ) ).doesNotContain( "<dl>" );

		withSetting( "enableDefinitionLists", "true", "false", () -> {
			runtime.executeSource( "result = markdown( '" + markdown + "' )", context );
			String html = variables.getAsString( Key.result );
			assertThat( html ).contains( "<dl>" );
			assertThat( html ).contains( "<dt>Term</dt>" );
			assertThat( html ).contains( "Definition text." );
		} );
	}

	/**
	 * markdownRegister()/markdownUnregister() (via
	 * MarkdownService.registerExtension()) are exercised here as plain Java,
	 * constructing a standalone MarkdownService instance directly, rather than
	 * through runtime.executeSource() + createObject("java", ...). The module
	 * under test is loaded (module spec-wise) from its own packaged/shaded jar
	 * under a dedicated classloader (see BaseIntegrationTest.loadModule()); a
	 * Flexmark class resolved via createObject("java", ...) from a plain
	 * top-level script is loaded through BoxLang's own default Java-interop
	 * classloader instead, which is a *different* loader than the one that
	 * loaded MarkdownService's own bundled copy of the same Flexmark classes -
	 * so an object built that way fails MarkdownService's own `instanceof
	 * Extension` check even though it's "the same class" by source. This test
	 * instead builds its own MarkdownService (and its EmojiExtension) entirely
	 * from this test's own compiled classpath - one consistent classloader,
	 * so it correctly proves the registration mechanism itself: settings
	 * changes and registered extensions actually affect toHtml() output.
	 * Whichever classloader topology a real *consuming module* deploys under
	 * determines whether IT can pass raw Flexmark objects across that same
	 * boundary - see readme.md's "Plugin Extensions" section.
	 */
	@DisplayName( "Test MarkdownService.registerExtension()/unregisterExtension() wire up a Flexmark extension with no built-in setting" )
	@Test
	public void testMarkdownServiceRegisterExtension() {
		MarkdownService	service		= new MarkdownService();
		String			markdown	= "I :heart: BoxLang";

		// Emoji shortcodes are plain, unconverted text with no extension registered
		assertThat( service.toHtml( markdown ) ).contains( ":heart:" );
		assertThat( service.getRegisteredExtensions() ).isEmpty();

		EmojiExtension emojiExtension = EmojiExtension.create();
		try {
			service.registerExtension( emojiExtension );

			assertThat( service.getRegisteredExtensions() ).containsExactly( emojiExtension );
			assertThat( service.toHtml( markdown ) ).doesNotContain( ":heart:" );

			// Registering the same instance twice is a no-op
			service.registerExtension( emojiExtension );
			assertThat( service.getRegisteredExtensions() ).hasSize( 1 );
		} finally {
			service.unregisterExtension( emojiExtension );
		}

		assertThat( service.getRegisteredExtensions() ).isEmpty();
		assertThat( service.toHtml( markdown ) ).contains( ":heart:" );
	}

}
