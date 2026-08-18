# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

* * *

## [Unreleased]

### Added

- Admonition/callout blocks (`!!! type "Title"`, collapsible `???`/`???+`) via a new `enableAdmonition` setting (Flexmark's own admonition extension)
- Footnote references (`[^1]`) via a new `enableFootnotes` setting
- Definition lists via a new `enableDefinitionLists` setting
- A genuine plugin/extension mechanism: `MarkdownService.registerExtension()`/`unregisterExtension()`, and `markdownRegister()`/`markdownUnregister()` BIFs, for registering any Flexmark extension - not just the ones this module wires up as settings - without forking this module

## [1.0.0] - 2025-04-21

- First iteration of this module

[Unreleased]: https://github.com/ortus-boxlang/bx-markdown/compare/v1.0.0...HEAD

[1.0.0]: https://github.com/ortus-boxlang/bx-markdown/compare/37ff1997381dfc0cda9f3ae0172053704da901f2...v1.0.0
