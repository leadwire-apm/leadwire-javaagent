/***************************************************************************
 * Copyright 2017 Lead Wire (https://leadwire-apm.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package kieker.monitoring.probe.aspectj.jdbc;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @author Wassim DHIB
 * 
 * @since 1.3	
 */
@Aspect
public class OperationExecutionAspectFullJDBC extends AbstractOperationExecutionAspectJDBC {

	/**
	 * Default constructor.
	 */
	public OperationExecutionAspectFullJDBC() {
		// empty default constructor
	}

	/*
	@Override
	@Pointcut("execution(* org.postgresql.jdbc.PgStatement.execute(..)) && args(p_sql)")
	public void monitoredStatement1(final String p_sql) {
		// Aspect Declaration (MUST be empty)
	}
*/
	@Override
	@Pointcut("execution(* org.postgresql.jdbc.PgStatement.execute(..)) && args(queryToExecute, queryParameters, flags)")
	public void monitoredStatement2(final org.postgresql.core.CachedQuery queryToExecute,final org.postgresql.core.ParameterList queryParameters, final int flags ) {
		// Aspect Declaration (MUST be empty)
	}
	

	
	@Override
	@Pointcut("execution(* org.postgresql.jdbc.PgStatement.execute(..))")
	public void monitoredOperation() {
		// Aspect Declaration (MUST be empty)
	}

}

