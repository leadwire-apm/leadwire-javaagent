/***************************************************************************
 * Copyright 2017 LeadWire (http://leadwire-apm.com)
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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;
import kieker.common.record.controlflow.OperationExecutionRecord;
import kieker.common.record.jdbc.JDBCOperationExecutionRecord;
import kieker.monitoring.core.controller.IMonitoringController;
import kieker.monitoring.core.controller.MonitoringController;
import kieker.monitoring.core.registry.ControlFlowRegistry;
import kieker.monitoring.core.registry.SessionRegistry;
import kieker.monitoring.probe.aspectj.operationExecution.AbstractOperationExecutionAspect;
import kieker.monitoring.timer.ITimeSource;

/**
 * @author Wassim Dhib
 * 
 * @since 1.3
 */ 


@Aspect
public abstract class AbstractOperationExecutionAspectJDBC extends AbstractOperationExecutionAspect {
	private static final Log LOG = LogFactory.getLog(AbstractOperationExecutionAspect.class);

	private static final ControlFlowRegistry CFREGISTRY = ControlFlowRegistry.INSTANCE;
	private static final SessionRegistry SESSIONREGISTRY = SessionRegistry.INSTANCE;
	private static final IMonitoringController CTRLINST = MonitoringController.getInstance();
	private static final ITimeSource TIME = CTRLINST.getTimeSource();
	private static final String VMNAME = CTRLINST.getHostname();

	/*
	@Pointcut
	public abstract void monitoredStatement1(final String p_sql);

	@Around("monitoredStatement1(String) && notWithinKieker()")
public Object statement1(final ProceedingJoinPoint thisJoinPoint) throws Throwable { // NOCS (Throwable)
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
	final String sqlStatement = (String) thisJoinPoint.getArgs()[0];
	//Object[] arg0 = {sqlStatement} ;	
	
	try {	
		
		retVal = thisJoinPoint.proceed();
		
			} finally {
				System.out.println("atraceId="+traceId+";req="+sqlStatement);
				final long tout = TIME.getTime();
				CTRLINST.newMonitoringRecord(new JDBCOperationExecutionRecord(sqlStatement, sessionId, traceId, tin, tout, hostname, eoi, ess));
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

	*/
	
	@Pointcut
	public abstract void monitoredStatement2(final org.postgresql.core.CachedQuery queryToExecute,final org.postgresql.core.ParameterList queryParameters, final int flags);

	@Around("monitoredStatement2(org.postgresql.core.CachedQuery, org.postgresql.core.ParameterList, int) && notWithinKieker() ")
public Object statement2(final ProceedingJoinPoint thisJoinPoint) throws Throwable { // NOCS (Throwable)		
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
	final org.postgresql.core.CachedQuery query = (org.postgresql.core.CachedQuery) thisJoinPoint.getArgs()[0];
	final String sqlStatement = query.toString().replaceAll("\\r\\n|\\r|\\n", " ");
	
	try {	
		
		retVal = thisJoinPoint.proceed();
		
			} finally {
				System.out.println("traceId="+traceId+";req="+sqlStatement);
				final long tout = TIME.getTime();
				CTRLINST.newMonitoringRecord(new JDBCOperationExecutionRecord(sqlStatement, sessionId, traceId, tin, tout, hostname, eoi, ess));
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


	/*
	@Pointcut
	public abstract void monitoredStatement3();
	
	@Around("monitoredStatement3()")
public Object statement3(final ProceedingJoinPoint thisJoinPoint) throws Throwable { // NOCS (Throwable)
		final String signature = this.signatureToLongString(thisJoinPoint.getSignature());
		System.out.println("test="+signature);
		
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
	final org.postgresql.core.CachedQuery query = (org.postgresql.core.CachedQuery) thisJoinPoint.getArgs()[0];
	final String sqlStatement = query.toString();
	
	try {	
		
		retVal = thisJoinPoint.proceed();
		
			} finally {
				System.out.println("traceId="+traceId+";req="+sqlStatement);
				final long tout = TIME.getTime();
				CTRLINST.newMonitoringRecord(new JDBCOperationExecutionRecord(sqlStatement, sessionId, traceId, tin, tout, hostname, eoi, ess));
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
*/
	

}