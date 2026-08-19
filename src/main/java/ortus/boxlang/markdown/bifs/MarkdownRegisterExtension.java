/**
 * [BoxLang]
 *
 * Copyright [2023] [Ortus Solutions, Corp]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ortus.boxlang.markdown.bifs;

import java.util.Set;

import com.vladsch.flexmark.util.misc.Extension;

import ortus.boxlang.markdown.MarkdownService;
import ortus.boxlang.markdown.util.KeyDictionary;
import ortus.boxlang.runtime.bifs.BIF;
import ortus.boxlang.runtime.bifs.BoxBIF;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.scopes.ArgumentsScope;
import ortus.boxlang.runtime.types.Argument;
import ortus.boxlang.runtime.types.exceptions.BoxRuntimeException;
import ortus.boxlang.runtime.validation.Validator;

/**
 * The plugin entry point for this module: registers a Flexmark
 * {@code com.vladsch.flexmark.util.misc.Extension} instance - one of
 * Flexmark's own bundled extensions this module's built-in settings don't
 * already cover, or a fully custom one of your own - so it's loaded on
 * every subsequent {@code markdown()}/{@code HtmlToMarkdown()}/{@code bx:markdown}
 * call, no fork of this module required. Registering the same instance
 * twice is a no-op.
 */
@BoxBIF
public class MarkdownRegisterExtension extends BIF {

	/**
	 * Markdown service
	 */
	protected MarkdownService markdownService = ( MarkdownService ) runtime.getGlobalService( KeyDictionary.markdowService );

	/**
	 * Constructor
	 */
	public MarkdownRegisterExtension() {
		super();
		this.declaredArguments = new Argument[] {
		    new Argument( true, Argument.ANY, KeyDictionary.extension, Set.of( Validator.REQUIRED ) )
		};
	}

	/**
	 * Registers a Flexmark extension for use on every markdown conversion going forward.
	 *
	 * @param context   The context in which the BIF is being invoked.
	 * @param arguments Argument scope for the BIF.
	 *
	 * @argument.extension A {@code com.vladsch.flexmark.util.misc.Extension} instance, e.g. from
	 *                     `createObject( "java", "com.vladsch.flexmark.ext.emoji.EmojiExtension" ).create()`.
	 *
	 * @return null
	 */
	public Object _invoke( IBoxContext context, ArgumentsScope arguments ) {
		Object candidate = arguments.get( KeyDictionary.extension );
		if ( ! ( candidate instanceof Extension ) ) {
			throw new BoxRuntimeException(
			    "markdownRegisterExtension() requires a com.vladsch.flexmark.util.misc.Extension instance, received: "
			        + ( candidate == null ? "null" : candidate.getClass().getName() )
			);
		}
		this.markdownService.registerExtension( ( Extension ) candidate );
		return null;
	}

}
