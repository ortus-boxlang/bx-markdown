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
package ortus.boxlang.markdown.components;

import ortus.boxlang.markdown.MarkdownService;
import ortus.boxlang.markdown.util.KeyDictionary;
import ortus.boxlang.runtime.components.Attribute;
import ortus.boxlang.runtime.components.BoxComponent;
import ortus.boxlang.runtime.components.Component;
import ortus.boxlang.runtime.context.IBoxContext;
import ortus.boxlang.runtime.dynamic.ExpressionInterpreter;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.IStruct;

@BoxComponent( requiresBody = true )
public class Markdown extends Component {

	/**
	 * Markdown service
	 */
	protected MarkdownService markdownService = ( MarkdownService ) runtime.getGlobalService( KeyDictionary.markdowService );

	/**
	 * --------------------------------------------------------------------------
	 * Constructor(s)
	 * --------------------------------------------------------------------------
	 */

	public Markdown() {
		super();
		declaredAttributes = new Attribute[] {
		    new Attribute( Key.variable, "string" )
		};
	}

	/**
	 * Any markdown output in the body of this component will be parsed to HTML
	 *
	 * @param context        The context in which the Component is being invoked
	 * @param attributes     The attributes to the Component
	 * @param body           The body of the Component
	 * @param executionState The execution state of the Component
	 *
	 * @attribute.variable The variable in which to store the parsed markdown. If empty, we will output the parsed html to the page buffer
	 *
	 */
	public BodyResult _invoke( IBoxContext context, IStruct attributes, ComponentBody body, IStruct executionState ) {
		String			variable	= attributes.getAsString( Key.variable );
		StringBuffer	buffer		= new StringBuffer();
		BodyResult		bodyResult	= processBody( context, body, buffer );

		// IF there was a return statement inside our body, we early exit now
		if ( bodyResult.isEarlyExit() ) {
			// A return statement inside discards all output that was built up
			return bodyResult;
		}

		if ( variable != null && !variable.isEmpty() ) {
			ExpressionInterpreter.setVariable(
			    context,
			    variable,
			    markdownService.toHtml( buffer.toString() )
			);
		} else {
			context.writeToBuffer( markdownService.toHtml( buffer.toString() ) );
		}

		return DEFAULT_RETURN;
	}

}
