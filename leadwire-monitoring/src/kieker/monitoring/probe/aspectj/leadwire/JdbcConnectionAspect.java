/***************************************************************************
 * Copyright 2018 Lead Wire (https://leadwire.io)
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

package kieker.monitoring.probe.aspectj.leadwire;



import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;
import kieker.monitoring.core.controller.IMonitoringController;
import kieker.monitoring.core.controller.MonitoringController;
import kieker.monitoring.probe.aspectj.AbstractAspectJProbe;
import kieker.monitoring.probe.aspectj.operationExecution.AbstractOperationExecutionAspect;

/**
 * @author Wassim DHIB
 * 
 * @since 1.13
 */
@Aspect
public class JdbcConnectionAspect extends AbstractAspectJProbe {

	private static final Log LOG = LogFactory.getLog(AbstractOperationExecutionAspect.class);

	private static final IMonitoringController CTRLINST = MonitoringController.getInstance();
	private static final Map<Object, String> statementSqlMap = CTRLINST.getStatementSqlMap();

	
	
	/* Configuration */
	private static final String NOT_WITHIN = " notWithinKieker()";


	/* http://docs.oracle.com/javase/7/docs/api/java/sql/Statement.html */
	private static final String RELATED_CALLS =
			/** Connection **/
			/* prepareCall */
		      "(  execution(java.sql.CallableStatement java.sql.Connection.prepareCall(String)) "
			+ "|| execution(java.sql.CallableStatement java.sql.Connection.prepareCall(String, int, int)) "
			+ "|| execution(java.sql.CallableStatement java.sql.Connection.prepareCall(String, int, int, int)) "
			/* prepareStatement */
			+ "|| execution(java.sql.PreparedStatement java.sql.Connection.prepareStatement(String)) "
			+ "|| execution(java.sql.PreparedStatement java.sql.Connection.prepareStatement(String, int)) "
			+ "|| execution(java.sql.PreparedStatement java.sql.Connection.prepareStatement(String, int[])) "
			+ "|| execution(java.sql.PreparedStatement java.sql.Connection.prepareStatement(String, int, int)) "
			+ "|| execution(java.sql.PreparedStatement java.sql.Connection.prepareStatement(String, int, int, int)) "
			+ "|| execution(java.sql.PreparedStatement java.sql.Connection.prepareStatement(String, String[])) "
			+ ") && this(thisObject) && " + NOT_WITHIN;

	

	@Around(RELATED_CALLS)
	public final Object operation(final Object thisObject, final ProceedingJoinPoint thisJoinPoint)
			throws Throwable {
		
		if (!CTRLINST.isMonitoringEnabled()) {
			return thisJoinPoint.proceed();
		}
		if (!CTRLINST.isProbeActivated(this.signatureToLongString(thisJoinPoint.getSignature()))) {
			return thisJoinPoint.proceed();
		}
		   
	final String sqlStatement = (String) thisJoinPoint.getArgs()[0];
			
	Object PreparedStatement  = thisJoinPoint.proceed();
		
    statementSqlMap.put(PreparedStatement , sqlStatement);
	
	return PreparedStatement ;
		
			
	}


}
