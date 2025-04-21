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
package ortus.boxlang.markdown;

import java.util.ArrayList;

import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.youtube.embedded.YouTubeLinkExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.misc.Extension;

import ortus.boxlang.markdown.util.KeyDictionary;
import ortus.boxlang.runtime.BoxRuntime;
import ortus.boxlang.runtime.dynamic.casters.BooleanCaster;
import ortus.boxlang.runtime.dynamic.casters.StringCaster;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.services.BaseService;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.Struct;

/**
 * The Markdown service is responsible for converting HTML to Markdown and
 * Markdown to HTML using the modules configured settings.
 */
public class MarkdownService extends BaseService {

	/**
	 * --------------------------------------------------------------------------
	 * Constants
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Default Options
	 */
	private static final IStruct	DEFAULT_OPTIONS	= Struct.of(
	    // Whether to auto link URLs
	    "autoLinkUrls", true,
	    // Whether to enable anchor links
	    "anchorLinks", true,
	    // Set the anchor id
	    "anchorSetId", true,
	    // Set the anchor id but also the name
	    "achorSetName", true,
	    // Do we create the anchor for the full header or just before it. True is wrap, false is just create anchor tag
	    "anchorWrapText", false,
	    // The class(es) to apply to the anchor
	    "anchorClass", "anchor",
	    // raw html prefix. Added before heading text, wrapped or unwrapped
	    "anchorPrefix", "",
	    // raw html suffix. Added before heading text, wrapped or unwrapped
	    "anchorSuffix", "",
	    // Enable youtube embedded link transformer
	    "enableYouTubeTransformer", false,
	    // default null, custom inline code open HTML
	    "codeStyleHTMLOpen", "<code>",
	    // default null, custom inline code close HTML
	    "codeStyleHTMLClose", "</code>",
	    // default "language-", prefix used for generating the <code> class for a fenced code block, only used if info is not empty and language is not
	    // defined in
	    "fencedCodeLanguageClassPrefix", "language-"
	);

	/**
	 * The default table options for the Markdown service
	 */
	private static final IStruct	TABLE_OPTIONS	= Struct.of(
	    // Treat consecutive pipes at the end of a column as defining spanning column.
	    "columnSpans", true,
	    // Whether table body columns should be at least the number or header columns.
	    "appendMissingColumns", true,
	    // Whether to discard body columns that are beyond what is defined in the header
	    "discardExtraColumns", true,
	    // Class name to use on tables
	    "className", "table",
	    // When true only tables whose header lines contain the same number of columns as the separator line will be recognized
	    "headerSeparationColumnMatch", true
	);

	/**
	 * The module settings for the Markdown service
	 */
	private final IStruct			settings;

	/**
	 * The loaded parser, it's lazy loaded when needed
	 */
	private Parser					parser;

	/**
	 * The HTML renderer
	 */
	private HtmlRenderer			htmlRenderer;

	/**
	 * The converter
	 */
	private FlexmarkHtmlConverter	converter;

	/**
	 * --------------------------------------------------------------------------
	 * Constructors
	 * --------------------------------------------------------------------------
	 */

	/**
	 * public no-arg constructor for the ServiceProvider
	 */
	public MarkdownService() {
		this( BoxRuntime.getInstance(), KeyDictionary.markdowService );
	}

	/**
	 * Constructor for the MarkdownService
	 *
	 * @param runtime the BoxRuntime instance
	 * @param name    the name of the service
	 */
	public MarkdownService( BoxRuntime runtime, Key name ) {
		super( runtime, name );
		this.settings = runtime.getModuleService().getModuleRecord( KeyDictionary.moduleName ).settings;
		DEFAULT_OPTIONS
		    .entrySet()
		    .stream()
		    .forEach( entry -> this.settings.putIfAbsent( ( Key ) entry.getKey(), entry.getValue() ) );
	}

	/**
	 * --------------------------------------------------------------------------
	 * Life-cycle methods
	 * --------------------------------------------------------------------------
	 */

	@Override
	public void onConfigurationLoad() {
	}

	@Override
	public void onShutdown( Boolean arg0 ) {
	}

	@Override
	public void onStartup() {
	}

	/**
	 * --------------------------------------------------------------------------
	 * Methods
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Get the module settings for the Markdown service
	 *
	 * @return the module settings
	 */
	public IStruct getSettings() {
		return this.settings;
	}

	/**
	 * Convert the incoming markdown to HTML using the configured settings
	 *
	 * @param txt the markdown to convert
	 *
	 * @return The converted HTML
	 */
	public String toHtml( String txt ) {
		Document document = buildOrGetParser().parse( txt.trim() );
		return buildOrGetHtmlRenderer().render( document );
	}

	/**
	 * Convert the incoming markdown to HTML using the configured settings
	 *
	 * @param html the HTML to convert
	 *
	 * @return The converted markdown
	 */
	public String toMarkdown( String html ) {
		return buildOrGetConverter().convert( html.trim() );
	}

	/**
	 * --------------------------------------------------------------------------
	 * Private Helpers
	 * --------------------------------------------------------------------------
	 */

	/**
	 * Lazy load the FlexmarkHtmlConverter
	 *
	 * @return the converter
	 */
	private FlexmarkHtmlConverter buildOrGetConverter() {
		if ( this.converter == null ) {
			synchronized ( this ) {
				this.converter = FlexmarkHtmlConverter.builder( createOptions() ).build();
			}
		}
		return this.converter;
	}

	/**
	 * Lazy load the parser, lock to ensure thread safety
	 *
	 * @return the parser
	 */
	private Parser buildOrGetParser() {
		if ( this.parser == null ) {
			synchronized ( this ) {
				this.parser = Parser.builder( createOptions() ).build();
			}
		}
		return this.parser;
	}

	/**
	 * Lazy load the HTML renderer, lock to ensure thread safety
	 *
	 * @return the HTML renderer
	 */
	private HtmlRenderer buildOrGetHtmlRenderer() {
		if ( this.htmlRenderer == null ) {
			synchronized ( this ) {
				this.htmlRenderer = HtmlRenderer.builder( createOptions() ).build();
			}
		}
		return this.htmlRenderer;
	}

	/**
	 * Build the options from the settings
	 */
	private MutableDataSet createOptions() {
		MutableDataSet			options				= new MutableDataSet();

		// Base Extensions to load
		ArrayList<Extension>	extensionsToLoad	= new ArrayList<>();
		extensionsToLoad.add( TablesExtension.create() );
		extensionsToLoad.add( com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension.create() );
		extensionsToLoad.add( TaskListExtension.create() );
		extensionsToLoad.add( TocExtension.create() );

		// Auto link urls: autoLinkUrls
		boolean autoLinkUrls = BooleanCaster.cast( this.settings.get( "autoLinkUrls" ) );
		if ( autoLinkUrls ) {
			extensionsToLoad.add( AutolinkExtension.create() );
		}

		// Anchor Links: anchorLinks
		boolean anchorLinks = BooleanCaster.cast( this.settings.get( "anchorLinks" ) );
		if ( anchorLinks ) {
			extensionsToLoad.add( AnchorLinkExtension.create() );
		}

		// Youtube Transformer: enableYouTubeTransformer
		boolean enableYouTubeTransformer = BooleanCaster.cast( this.settings.get( "enableYouTubeTransformer" ) );
		if ( enableYouTubeTransformer ) {
			extensionsToLoad.add( YouTubeLinkExtension.create() );
		}

		// Table Options
		IStruct tableOptions = ( IStruct ) this.settings.getOrDefault( KeyDictionary.tableOptions, TABLE_OPTIONS );
		TABLE_OPTIONS
		    .entrySet()
		    .stream()
		    .forEach( entry -> tableOptions.putIfAbsent( ( Key ) entry.getKey(), entry.getValue() ) );

		options
		    // Auto Links
		    .set( Parser.WWW_AUTO_LINK_ELEMENT, autoLinkUrls )
		    // Anchor Links
		    .set( AnchorLinkExtension.ANCHORLINKS_SET_ID, BooleanCaster.cast( this.settings.get( "anchorSetId" ) ) )
		    .set( AnchorLinkExtension.ANCHORLINKS_SET_NAME, BooleanCaster.cast( this.settings.get( "achorSetName" ) ) )
		    .set( AnchorLinkExtension.ANCHORLINKS_WRAP_TEXT, BooleanCaster.cast( this.settings.get( "anchorWrapText" ) ) )
		    .set( AnchorLinkExtension.ANCHORLINKS_ANCHOR_CLASS, StringCaster.cast( this.settings.get( "anchorClass" ) ) )
		    .set( AnchorLinkExtension.ANCHORLINKS_TEXT_PREFIX, StringCaster.cast( this.settings.get( "anchorPrefix" ) ) )
		    .set( AnchorLinkExtension.ANCHORLINKS_TEXT_SUFFIX, StringCaster.cast( this.settings.get( "anchorSuffix" ) ) )
		    // Code Styles
		    .set( HtmlRenderer.CODE_STYLE_HTML_OPEN, StringCaster.cast( this.settings.get( "codeStyleHTMLOpen" ) ) )
		    .set( HtmlRenderer.CODE_STYLE_HTML_CLOSE, StringCaster.cast( this.settings.get( "codeStyleHTMLClose" ) ) )
		    .set( HtmlRenderer.FENCED_CODE_LANGUAGE_CLASS_PREFIX, StringCaster.cast( this.settings.get( "fencedCodeLanguageClassPrefix" ) ) )
		    // Table Options
		    .set( TablesExtension.COLUMN_SPANS, BooleanCaster.cast( tableOptions.get( "columnSpans" ) ) )
		    .set( TablesExtension.APPEND_MISSING_COLUMNS, BooleanCaster.cast( tableOptions.get( "appendMissingColumns" ) ) )
		    .set( TablesExtension.DISCARD_EXTRA_COLUMNS, BooleanCaster.cast( tableOptions.get( "discardExtraColumns" ) ) )
		    .set( TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, BooleanCaster.cast( tableOptions.get( "headerSeparationColumnMatch" ) ) )
		    .set( TablesExtension.CLASS_NAME, StringCaster.cast( tableOptions.get( "className" ) ) )
		    // Load Extensions
		    .set( Parser.EXTENSIONS, extensionsToLoad );

		return options;
	}

}
