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
 * Removes a Flexmark extension previously registered via
 * {@code markdownRegisterExtension()} - a no-op if it was never registered
 * (or already removed).
 */
@BoxBIF
public class MarkdownUnregisterExtension extends BIF {

	/**
	 * Markdown service
	 */
	protected MarkdownService markdownService = ( MarkdownService ) runtime.getGlobalService( KeyDictionary.markdowService );

	/**
	 * Constructor
	 */
	public MarkdownUnregisterExtension() {
		super();
		this.declaredArguments = new Argument[] {
		    new Argument( true, Argument.ANY, KeyDictionary.extension, Set.of( Validator.REQUIRED ) )
		};
	}

	/**
	 * Unregisters a previously-registered Flexmark extension.
	 *
	 * @param context   The context in which the BIF is being invoked.
	 * @param arguments Argument scope for the BIF.
	 *
	 * @argument.extension The same `com.vladsch.flexmark.util.misc.Extension` instance passed to `markdownRegisterExtension()`.
	 *
	 * @return null
	 */
	public Object _invoke( IBoxContext context, ArgumentsScope arguments ) {
		Object candidate = arguments.get( KeyDictionary.extension );
		if ( ! ( candidate instanceof Extension ) ) {
			throw new BoxRuntimeException(
			    "markdownUnregisterExtension() requires a com.vladsch.flexmark.util.misc.Extension instance, received: "
			        + ( candidate == null ? "null" : candidate.getClass().getName() )
			);
		}
		this.markdownService.unregisterExtension( ( Extension ) candidate );
		return null;
	}

}
