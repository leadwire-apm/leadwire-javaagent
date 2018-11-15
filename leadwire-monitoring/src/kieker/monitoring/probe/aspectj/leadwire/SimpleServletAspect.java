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
import org.aspectj.lang.annotation.Pointcut;

import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;
import kieker.common.record.controlflow.OperationExecutionRecord;
import kieker.common.record.http.HttpOperationExecutionRecord;
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
public class SimpleServletAspect extends AbstractAspectJProbe {

	private static final ControlFlowRegistry CFREGISTRY = ControlFlowRegistry.INSTANCE;
	private static final SessionRegistry SESSIONREGISTRY = SessionRegistry.INSTANCE;
	private static final IMonitoringController CTRLINST = MonitoringController.getInstance();
	private static final Log LOG = LogFactory.getLog(AbstractOperationExecutionAspect.class);
	private static final String VMNAME = CTRLINST.getHostname();
	private static final ITimeSource TIME = CTRLINST.getTimeSource();
	public static final String SESSION_ID_ASYNC_TRACE = "NOSESSION-ASYNCIN";



	@Around("execution(* javax.servlet.http.HttpServlet.service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse))")
	public Object servletservice(final ProceedingJoinPoint thisJoinPoint) throws Throwable { // NOCS (Throwable)

		if (!CTRLINST.isMonitoringEnabled()) {
			return thisJoinPoint.proceed();
		}
		if (!CTRLINST.isProbeActivated(this.signatureToLongString(thisJoinPoint.getSignature()))) {
			return thisJoinPoint.proceed();
		}
		
		final Object req = (Object) thisJoinPoint.getArgs()[0];

		
		// collect data
				boolean entrypoint = false;
				String hostname = VMNAME;
				String sessionId = SESSIONREGISTRY.recallThreadLocalSessionId();
				int eoi; // this is executionOrderIndex-th execution in this trace
				int ess; // this is the height in the dynamic call tree of this execution
				long traceId = CFREGISTRY.recallThreadLocalTraceId(); // traceId, -1 if entry point
				
		if (traceId == -1) {
			
			entrypoint = true;
			traceId = CFREGISTRY.getAndStoreUniqueThreadLocalTraceId();
			CFREGISTRY.storeThreadLocalEOI(0);
			CFREGISTRY.storeThreadLocalESS(1); // next operation is ess + 1
			eoi = 0;
			ess = 0;
			
			
			String requestHeader = null;
			if( req.getClass().getName().equals("javax.servlet.http.HttpServletRequest") ){

			//get request headers
			Method aMethodgetHeader = req.getClass().getMethod("getHeader");
			aMethodgetHeader.setAccessible(Boolean.TRUE); 
			requestHeader = (String) aMethodgetHeader.invoke(req, HttpClientHeaderConstants.OPERATION_EXECUTION_HTTPCLIENT_HEADER );
			}
			
			if (requestHeader != null)  {

			final String[] headerArray = requestHeader.split(",");

			// Extract session id
			sessionId = headerArray[1];
			if ("null".equals(sessionId)) {
				sessionId = OperationExecutionRecord.NO_SESSION_ID;
			}

			// Extract EOI
			final String eoiStr = headerArray[2];
			eoi = -1;
			try {
				eoi = 1 + Integer.parseInt(eoiStr);
			} catch (final NumberFormatException exc) {
				LOG.warn("Invalid eoi", exc);
			}

			// Extract ESS
			final String essStr = headerArray[3];
			ess = -1;
			try {
				ess = Integer.parseInt(essStr);
			} catch (final NumberFormatException exc) {
				LOG.warn("Invalid ess", exc);
			}

			// Extract trace id
			final String traceIdStr = headerArray[0];
			if (traceIdStr != null) {
				try {
					traceId = Long.parseLong(traceIdStr);
				} catch (final NumberFormatException exc) {
					LOG.warn("Invalid trace id", exc);
				}
			} else {
				traceId = CFREGISTRY.getUniqueTraceId();
				sessionId = SESSION_ID_ASYNC_TRACE;
				entrypoint = true;
				eoi = 0; // EOI of this execution
				ess = 0; // ESS of this execution
			}

			// Store thread-local values
			CFREGISTRY.storeThreadLocalTraceId(traceId);
			CFREGISTRY.storeThreadLocalEOI(eoi); // this execution has EOI=eoi; next execution will get eoi with incrementAndRecall
			CFREGISTRY.storeThreadLocalESS(ess + 1); // this execution has ESS=ess
			SESSIONREGISTRY.storeThreadLocalSessionId(sessionId);
		} 
			else if (sessionId==null) {

				Method aMethodgetSession = req.getClass().getMethod("getSession", boolean.class);
				aMethodgetSession.setAccessible(Boolean.TRUE); 
				Object aSession = (Object) aMethodgetSession.invoke(req, true); 

				Method aMethodgetId = aSession.getClass().getMethod("getId");
				aMethodgetId.setAccessible(Boolean.TRUE); 
				String aId = (String) aMethodgetId.invoke(aSession);	 

				sessionId = (req != null) ? aId : null; 
				SESSIONREGISTRY.storeThreadLocalSessionId(sessionId);
			}

		} else {

			return thisJoinPoint.proceed();	
		}
		
		

	// getRequestURL 
	Method aMethodgetRequestURL = req.getClass().getMethod("getRequestURL");
	aMethodgetRequestURL.setAccessible(Boolean.TRUE); 
	StringBuffer requestURL =  (StringBuffer) aMethodgetRequestURL.invoke(req);
	String completeURL = requestURL.toString();
				
	// measure before
	final long tin = TIME.getTime();
	Object retVal = null;
	
		try {

			retVal = thisJoinPoint.proceed();
		}
		finally	{


			final long tout = TIME.getTime();
			CTRLINST.newMonitoringRecord(new HttpOperationExecutionRecord(completeURL, sessionId, traceId, tin, tout, hostname, eoi, ess));
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