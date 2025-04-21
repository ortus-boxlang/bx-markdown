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
}
