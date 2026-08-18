# ⚡︎ BoxLang Markdown

```
|:------------------------------------------------------:|
| ⚡︎ B o x L a n g ⚡︎
| Dynamic : Modular : Productive
|:------------------------------------------------------:|
```

<blockquote>
	Copyright Since 2023 by Ortus Solutions, Corp
	<br>
	<a href="https://www.boxlang.io">www.boxlang.io</a> |
	<a href="https://www.ortussolutions.com">www.ortussolutions.com</a>
</blockquote>

<p>&nbsp;</p>

Welcome to the BoxLang Markdown module.  This provides native support for Markdown in BoxLang based on the popular Flexmark library.

## Built In Functions (BIFs)

The following BIFs are available for use in your BoxLang code:

* `markdown()`
* `HtmlToMarkdown()`
* `registerMarkdownExtension()`
* `unregisterMarkdownExtension()`

### `markdown()`

Converts markdown markup to HTML.

```js
markdown( txt )
```

**Arguments:**

* `txt` - The markdown text to convert to HTML

**Returns:**

 The HTML equivalent string of the markup.

**Example:**

```js
markdown( "# Hello World" )
```

**Output:**

```html
<h1>Hello World</h1>
```

### `HtmlToMarkdown()`

Converts HTML markup to markdown.

```js
HtmlToMarkdown( markup )
```

**Arguments:**

* `markup` - The HTML string to convert.

**Returns:**

* The markdown equivalent string of the markup.

**Example:**

```js
HtmlToMarkdown( "<h1>Hello World</h1>" )
```

**Output:**

```markdown
# Hello World
```

### `registerMarkdownExtension()`

Registers a [Flexmark](https://github.com/vsch/flexmark-java) `com.vladsch.flexmark.util.misc.Extension` instance so it's loaded on every subsequent `markdown()`/`HtmlToMarkdown()`/`bx:markdown` call - the plugin entry point described in [Plugin Extensions](#plugin-extensions) below. Registering the same instance twice is a no-op.

```js
registerMarkdownExtension( extension )
```

**Arguments:**

* `extension` - A `com.vladsch.flexmark.util.misc.Extension` instance.

**Returns:**

* Nothing (`null`).

### `unregisterMarkdownExtension()`

Removes a previously-registered extension. A no-op if it was never registered (or already removed).

```js
unregisterMarkdownExtension( extension )
```

**Arguments:**

* `extension` - The same extension instance passed to `registerMarkdownExtension()`.

**Returns:**

* Nothing (`null`).

## Components

This module also provides a `bx:markdown` component that can be used to convert markdown to HTML in a wrapping approach.  You can use it in script or in the templating language.  The following attributes are available:

* `variable` - The variable to store the output in.  If not set, the output will be written to the response.

Example with variable:

```js
// The content of the component will be parsed and stored in the variable: data.
bx:markdown variable="data"{
	writeOutput( "## Hola" )
}
```

Example with no variable, outputs to the response:

```js
bx:markdown{
	writeOutput( "## Hola" )
}
```

Example in the templating language using a variable:

```html
<bx:markdown variable="html">
	## Hola Mundo

	My beautiful markdown text
</bx:markdown>
<bx:output>#html#</bx:output>
```

Example in the templating language with no variable:

```html
<bx:markdown>
	## Hola mundo

	This is a markdown test
</bx:markdown>
```

## Settings

A subset of the flexmark options are supported.  These can be configured in your `boxlang.json` in the `modules` section:

```js
"modules" : {

	"bxMarkdown" : {
		"enabled" : true,
		"settings" : {
			// Looks for www or emails and converts them to links
			"autoLinkUrls"                  : true,
			// Creates anchor links for headings
			"anchorLinks"                   : true,
			// Set the anchor id
			"anchorSetId"                   : true,
			// Set the anchor id but also the name
			"achorSetName"                  : true,
			// Do we create the anchor for the full header or just before it. True is wrap, false is just create anchor tag
			"anchorWrapText"                : false,
			// The class(es) to apply to the anchor
			"anchorClass"                   : "anchor",
			// raw html prefix. Added before heading text, wrapped or unwrapped
			"anchorPrefix"                  : "",
			// raw html suffix. Added before heading text, wrapped or unwrapped
			"anchorSuffix"                  : "",
			// Enable youtube embedded link transformer
			"enableYouTubeTransformer"      : false,
			// default null, custom inline code open HTML
			"codeStyleHTMLOpen"             : "<code>",
			// default null, custom inline code close HTML
			"codeStyleHTMLClose"            : "</code>",
			// default "language-", prefix used for generating the <code> class for a fenced code block, only used if info is not empty and language is not defined in
			"fencedCodeLanguageClassPrefix" : "language-",
			// Enable admonition/callout blocks: !!! type "Title" (or ??? / ???+ for a collapsible version)
			"enableAdmonition"              : false,
			// Enable footnote references: [^1] ... [^1]: definition
			"enableFootnotes"               : false,
			// Enable definition lists: Term \n : Definition
			"enableDefinitionLists"         : false,
			// Table options
			"tableOptions"                  : {
				// Treat consecutive pipes at the end of a column as defining spanning column.
				"columnSpans"                 : true,
				// Whether table body columns should be at least the number or header columns.
				"appendMissingColumns"        : true,
				// Whether to discard body columns that are beyond what is defined in the header
				"discardExtraColumns"         : true,
				// Class name to use on tables
				"className"                   : "table",
				// When true only tables whose header lines contain the same number of columns as the separator line will be recognized
				"headerSeparationColumnMatch" : true
			}
		}
	}
	// end markdown config

};
```

### Admonitions

Enable with `enableAdmonition: true`. Renders [Flexmark's admonition extension](https://github.com/vsch/flexmark-java/wiki/Admonition-Extension) - `!!!` for a regular callout, `???`/`???+` for a collapsible one (marked with an `adm-collapsed` CSS class; the extension doesn't ship its own JS toggle, so wire up a click handler for it if you want it interactive):

```markdown
!!! note "Optional Title"
    Body text - regular markdown, including **bold**, `code`, links, etc.

??? tip
    Collapsed by default.
```

Renders as `<div class="adm-block adm-note">`/`<div class="adm-block adm-tip adm-collapsed">` with an `adm-heading`/`adm-body` structure and an inline SVG icon - style it yourself, or call the extension's own `AdmonitionExtension.getDefaultCSS()`/`getDefaultScript()` (Java) for its bundled defaults.

### Footnotes

Enable with `enableFootnotes: true`:

```markdown
Here's a claim[^1].

[^1]: The source for that claim.
```

### Definition Lists

Enable with `enableDefinitionLists: true`:

```markdown
Term
:   Definition text.
```

## Plugin Extensions

Beyond the settings above, `MarkdownService` exposes a genuine extension point for anything Flexmark itself supports that isn't already wired up as a setting - register any `com.vladsch.flexmark.util.misc.Extension` instance (one of [Flexmark's own bundled extensions](https://github.com/vsch/flexmark-java/wiki/Extensions), or a fully custom one of your own) and it's loaded on every subsequent conversion, no fork of this module required:

```js
// Java-side, e.g. from another module's own code
markdownService.registerExtension( SomeFlexmarkExtension.create() )

// Or from BoxLang script, if the extension class is on your own classpath
registerMarkdownExtension( createObject( "java", "com.vladsch.flexmark.ext.footnotes.FootnoteExtension" ).create() )
```

`unregisterMarkdownExtension()` (or `MarkdownService.unregisterExtension()`) removes one again.

**A classloader caveat:** this module bundles Flexmark (`flexmark-all`) as its own dependency. Whether a *different* BoxLang module (or a top-level script) can construct a Flexmark extension object that this module's own `instanceof Extension` check accepts depends on whether your deployment's classloader topology gives the two of you a shared view of those Flexmark classes - straightforward for code that's part of this module itself (or shares its classloader), less certain across an isolated module boundary. If `registerMarkdownExtension()` throws complaining the object it received "requires a com.vladsch.flexmark.util.misc.Extension instance", that's this boundary - construct/register the extension from code that shares this module's own classpath instead of a separate one.

## Ortus Sponsors

BoxLang is a professional open-source project and it is completely funded by the [community](https://patreon.com/ortussolutions) and [Ortus Solutions, Corp](https://www.ortussolutions.com). Ortus Patreons get many benefits like a cfcasts account, a FORGEBOX Pro account and so much more. If you are interested in becoming a sponsor, please visit our patronage page: [https://patreon.com/ortussolutions](https://patreon.com/ortussolutions)

### THE DAILY BREAD

> "I am the way, and the truth, and the life; no one comes to the Father, but by me (JESUS)" Jn 14:1-12
