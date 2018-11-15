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

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;
import kieker.common.record.controlflow.OperationExecutionRecord;
import kieker.monitoring.core.controller.IMonitoringController;
import kieker.monitoring.core.controller.MonitoringController;
import kieker.monitoring.core.registry.ControlFlowRegistry;
import kieker.monitoring.core.registry.SessionRegistry;
import kieker.monitoring.probe.aspectj.AbstractAspectJProbe;
import kieker.monitoring.probe.aspectj.operationExecution.AbstractOperationExecutionAspect;
import kieker.monitoring.timer.ITimeSource;

/**
 * @author Wassim Dhib
 * 
 * @since 1.13
 */ 


@Aspect
public class HttpClientAspect extends AbstractAspectJProbe {

	private static final ControlFlowRegistry CFREGISTRY = ControlFlowRegistry.INSTANCE;
	private static final SessionRegistry SESSIONREGISTRY = SessionRegistry.INSTANCE;
	private static final IMonitoringController CTRLINST = MonitoringController.getInstance();
	private static final Log LOG = LogFactory.getLog(AbstractOperationExecutionAspect.class);
	private static final String VMNAME = CTRLINST.getHostname();
	private static final ITimeSource TIME = CTRLINST.getTimeSource();
	public static final String SESSION_ID_ASYNC_TRACE = "NOSESSION-ASYNCIN";


	@Around("execution(org.apache.http.client.methods.CloseableHttpResponse org.apache.http.impl.execchain.ClientExecChain.execute(org.apache.http.conn.routing.HttpRoute, org.apache.http.client.methods.HttpRequestWrapper, org.apache.http.client.protocol.HttpClientContext, org.apache.http.client.methods.HttpExecutionAware))")
	public Object httpexecute(final ProceedingJoinPoint thisJoinPoint) throws Throwable { // NOCS (Throwable)
		if (!CTRLINST.isMonitoringEnabled()) {
			return thisJoinPoint.proceed();
		}
		
		final String signature = this.signatureToLongString(thisJoinPoint.getSignature());

		if (!CTRLINST.isProbeActivated(signature)) {
			return thisJoinPoint.proceed();
		}
		
		final Object req = (Object) thisJoinPoint.getArgs()[1];

		//if( ! req.getClass().getName().equals("org.apache.http.client.methods.HttpRequestWrapper") ){
			//return thisJoinPoint.proceed();
		//}
		
		boolean entrypoint = true;
		final String hostname = VMNAME;
		final String sessionId = SESSIONREGISTRY.recallThreadLocalSessionId();
		final int eoi; // this is executionOrderIndex-th execution in this trace
		final int ess; // this is the height in the dynamic call tree of this execution
		final int nextESS;
		long traceId = CFREGISTRY.recallThreadLocalTraceId(); // traceId, -1 if entry point
		if (traceId == -1) {
			entrypoint = true;
			traceId = CFREGISTRY.getAndStoreUniqueThreadLocalTraceId();
			CFREGISTRY.storeThreadLocalEOI(0);
			CFREGISTRY.storeThreadLocalESS(1); // next operation is ess + 1
			eoi = 0;
			ess = 0;
			nextESS = 1;
		} else {
			entrypoint = false;
			eoi = CFREGISTRY.incrementAndRecallThreadLocalEOI();
			ess = CFREGISTRY.recallAndIncrementThreadLocalESS();
			nextESS = ess + 1;
			if ((eoi == -1) || (ess == -1)) {
				LOG.error("eoi and/or ess have invalid values:" + " eoi == " + eoi + " ess == " + ess);
				CTRLINST.terminateMonitoring();
			}
		}
		
		
		
		
		//check if header contains 
		boolean containsRequestHeader = false;
		Method aMethodcontainsHeader = req.getClass().getMethod("containsHeader", String.class);
		aMethodcontainsHeader.setAccessible(Boolean.TRUE); 
		containsRequestHeader = (boolean) aMethodcontainsHeader.invoke(req, HttpClientHeaderConstants.OPERATION_EXECUTION_HTTPCLIENT_HEADER );
		
		
		if (!containsRequestHeader) {

			final String requestHeader = Long.toString(traceId) + "," + sessionId + "," + Integer.toString(eoi) + "," + Integer.toString(nextESS);
			
			Method aMethodaddHeader = req.getClass().getMethod("addHeader");
			aMethodaddHeader.setAccessible(Boolean.TRUE); 
			aMethodaddHeader.invoke(req, HttpClientHeaderConstants.OPERATION_EXECUTION_HTTPCLIENT_HEADER, requestHeader  );

		} 

				
	// measure before
	final long tin = TIME.getTime();
	Object retVal = null;
	
		try {
			retVal = thisJoinPoint.proceed();
		}
		finally	{

			final long tout = TIME.getTime();
			CTRLINST.newMonitoringRecord(new OperationExecutionRecord(signature, sessionId, traceId, tin, tout, hostname, eoi, ess));
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
