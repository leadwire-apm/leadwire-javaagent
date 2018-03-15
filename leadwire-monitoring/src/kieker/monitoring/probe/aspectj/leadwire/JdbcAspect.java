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

package kieker.monitoring.probe.aspectj.leadwire;



import java.lang.reflect.Method;
import java.lang.reflect.Field;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;
import kieker.common.record.jdbc.JDBCOperationExecutionRecord;
import kieker.monitoring.core.controller.IMonitoringController;
import kieker.monitoring.core.controller.MonitoringController;
import kieker.monitoring.core.registry.ControlFlowRegistry;
import kieker.monitoring.core.registry.SessionRegistry;
import kieker.monitoring.probe.aspectj.AbstractAspectJProbe;
import kieker.monitoring.probe.aspectj.operationExecution.AbstractOperationExecutionAspect;
import kieker.monitoring.timer.ITimeSource;

/**
 * @author Wassim DHIB
 * 
 * @since 1.13
 */
@Aspect
public class JdbcAspect extends AbstractAspectJProbe {

	private static final Log LOG = LogFactory.getLog(AbstractOperationExecutionAspect.class);

	private static final ControlFlowRegistry CFREGISTRY = ControlFlowRegistry.INSTANCE;
	private static final SessionRegistry SESSIONREGISTRY = SessionRegistry.INSTANCE;
	private static final IMonitoringController CTRLINST = MonitoringController.getInstance();
	private static final ITimeSource TIME = CTRLINST.getTimeSource();
	private static final String VMNAME = CTRLINST.getHostname();
	private static final long SQL_THRESHOLD = CTRLINST.getSqlThreshold();

	
	
	/* Configuration */
	private static final String NOT_WITHIN = " notWithinKieker()";


	/* http://docs.oracle.com/javase/7/docs/api/java/sql/Statement.html */
	private static final String RELATED_CALLS =
			/** Batches **/
			  "( execution(int[] java.sql.Statement.executeBatch())"
			/** Statement **/
			/* execute */
			+ "|| execution(boolean java.sql.Statement.execute(String)) "
			+ "|| execution(boolean java.sql.Statement.execute(String,int)) "
			+ "|| execution(boolean java.sql.Statement.execute(String,int[])) "
			+ "|| execution(boolean java.sql.Statement.execute(String,String[])) "
			/* executeQuery */
			+ "|| execution(java.sql.ResultSet java.sql.Statement.executeQuery(String)) "
			/* executeUpdate */
			+ "|| execution(int java.sql.Statement.executeUpdate(String)) "
			+ "|| execution(int java.sql.Statement.executeUpdate(String, int)) "
			+ "|| execution(int java.sql.Statement.executeUpdate(String, int[])) "
			+ "|| execution(int java.sql.Statement.executeUpdate(String, String[])) "
			/** PreparedStatement **/
			/* execute */
			+ "|| execution(boolean java.sql.PreparedStatement.execute()) "
			/* executeQuery */
			+ "|| execution(java.sql.ResultSet java.sql.PreparedStatement.executeQuery()) "
			/* executeUpdate */
			+ "|| execution(int java.sql.PreparedStatement.executeUpdate()) "
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

		
// collect data
final boolean entrypoint;
final String hostname = VMNAME;
final String sessionId = SESSIONREGISTRY.recallThreadLocalSessionId();
final int eoi; // this is executionOrderIndex-th execution in this trace
final int ess; // this is the height in the dynamic call tree of this execution
long traceId = CFREGISTRY.recallThreadLocalTraceId(); // traceId, -1 if entry point
if (traceId == -1) {
	entrypoint = true;
	traceId = CFREGISTRY.getAndStoreUniqueThreadLocalTraceId();
	CFREGISTRY.storeThreadLocalEOI(0);
	CFREGISTRY.storeThreadLocalESS(1); // next operation is ess + 1
	eoi = 0;
	ess = 0;
} else {
	entrypoint = false;
	eoi = CFREGISTRY.incrementAndRecallThreadLocalEOI(); // ess > 1
	ess = CFREGISTRY.recallAndIncrementThreadLocalESS(); // ess >= 0
	if ((eoi == -1) || (ess == -1)) {
		LOG.error("eoi and/or ess have invalid values:" + " eoi == " + eoi + " ess == " + ess);
		CTRLINST.terminateMonitoring();
	}
}
// measure before
final long tin = TIME.getTime();

	Object retVal;
	    
	final String sqlStatement = thisJoinPoint.getThis().toString().replaceAll("\\r\\n|\\r|\\n", " ");
	
	try {	
		
		retVal = thisJoinPoint.proceed();
		
			} finally {
				
				final long tout = TIME.getTime();
				if (tout-tin > SQL_THRESHOLD) {
				CTRLINST.newMonitoringRecord(new JDBCOperationExecutionRecord(sqlStatement, sessionId, traceId, tin, tout, hostname, eoi, ess));
				}
				SESSIONREGISTRY.unsetThreadLocalSessionId();
				
				// cleanup
				if (entrypoint) {
					CFREGISTRY.unsetThreadLocalTraceId();
					CFREGISTRY.unsetThreadLocalEOI();
					CFREGISTRY.unsetThreadLocalESS();
				} else {
					CFREGISTRY.storeThreadLocalESS(ess); // next operation is ess
				}
				
			}
			return retVal;
		
			
	}


}
