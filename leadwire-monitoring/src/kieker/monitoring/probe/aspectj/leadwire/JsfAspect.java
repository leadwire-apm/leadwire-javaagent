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

import kieker.common.record.http.HttpOperationExecutionRecord;
import kieker.monitoring.core.controller.IMonitoringController;
import kieker.monitoring.core.controller.MonitoringController;
import kieker.monitoring.core.registry.ControlFlowRegistry;
import kieker.monitoring.core.registry.SessionRegistry;
import kieker.monitoring.probe.aspectj.AbstractAspectJProbe;
import kieker.monitoring.timer.ITimeSource;

/**
 * @author Wassim Dhib
 * 
 * @since 1.13
 */ 


@Aspect
public class JsfAspect extends AbstractAspectJProbe {

	private static final ControlFlowRegistry CFREGISTRY = ControlFlowRegistry.INSTANCE;
	private static final SessionRegistry SESSIONREGISTRY = SessionRegistry.INSTANCE;
	private static final IMonitoringController CTRLINST = MonitoringController.getInstance();
		private static final String VMNAME = CTRLINST.getHostname();
	private static final ITimeSource TIME = CTRLINST.getTimeSource();
	public static final String SESSION_ID_ASYNC_TRACE = "NOSESSION-ASYNCIN";


	@Around("execution(* javax.faces.lifecycle.Lifecycle.execute(javax.faces.context.FacesContext))")
	public Object jsfexecute(final ProceedingJoinPoint thisJoinPoint) throws Throwable { // NOCS (Throwable)
	
		if (!CTRLINST.isMonitoringEnabled()) {
			return thisJoinPoint.proceed();
		}
		if (!CTRLINST.isProbeActivated(this.signatureToLongString(thisJoinPoint.getSignature()))) {
			return thisJoinPoint.proceed();
		}
		
		final Object fCtx = (Object) thisJoinPoint.getArgs()[0];

		
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

		} else {

			return thisJoinPoint.proceed();	
		}
		
		

	// getRequestURL 
	Method aMethodgetExternalContext = fCtx.getClass().getMethod("getExternalContext");
	aMethodgetExternalContext.setAccessible(Boolean.TRUE); 
	Object externalContext =  aMethodgetExternalContext.invoke(fCtx);
	
	String requestServletPath = null;
			
	 if (externalContext != null) {
		 
		 Method aMethodgetRequestServletPath = externalContext.getClass().getMethod("getRequestServletPath");
		 aMethodgetRequestServletPath.setAccessible(Boolean.TRUE); 
			requestServletPath =  (String) aMethodgetRequestServletPath.invoke(externalContext);
			
         }
 
				
	// measure before
	final long tin = TIME.getTime();
	Object retVal = null;
	
		try {

			retVal = thisJoinPoint.proceed();
		}
		finally	{


			final long tout = TIME.getTime();
			CTRLINST.newMonitoringRecord(new HttpOperationExecutionRecord(requestServletPath, sessionId, traceId, tin, tout, hostname, eoi, ess));
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
