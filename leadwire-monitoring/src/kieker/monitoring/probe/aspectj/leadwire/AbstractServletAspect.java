/***************************************************************************
 * Copyright 2017 Lead Wire (http://leadwire-apm.com)
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

import javax.servlet.http.HttpServletResponse;

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
import kieker.monitoring.probe.aspectj.operationExecution.HtmlResponseWrapper;
import kieker.monitoring.timer.ITimeSource;

/**
 * @author Wassim Dhib
 * 
 * @since 1.13
 */ 


@Aspect
public abstract class AbstractServletAspect extends AbstractOperationExecutionAspect {

	private static final ControlFlowRegistry CFREGISTRY = ControlFlowRegistry.INSTANCE;
	private static final SessionRegistry SESSIONREGISTRY = SessionRegistry.INSTANCE;
	private static final IMonitoringController CTRLINST = MonitoringController.getInstance();
	private static final Log LOG = LogFactory.getLog(AbstractOperationExecutionAspect.class);
	private static final String VMNAME = CTRLINST.getHostname();
	private static final ITimeSource TIME = CTRLINST.getTimeSource();



	@Pointcut
	public abstract void monitoredServletservice(final javax.servlet.http.HttpServletRequest request, final HttpServletResponse response);

	@Around("monitoredServletservice(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse) && notWithinKieker()")
	public Object servletservice(final ProceedingJoinPoint thisJoinPoint) throws Throwable { // NOCS (Throwable)
	if (!CTRLINST.isMonitoringEnabled()) {
		return thisJoinPoint.proceed();
	}
	if (!CTRLINST.isProbeActivated(this.signatureToLongString(thisJoinPoint.getSignature()))) {
		return thisJoinPoint.proceed();
	}
	
		// collect data
		final boolean entrypoint;
		final String signature = this.signatureToLongString(thisJoinPoint.getSignature());
		final String hostname = VMNAME;
		String sessionId = SESSIONREGISTRY.recallThreadLocalSessionId();
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
		
	final Object req = (Object) thisJoinPoint.getArgs()[0];
	
	
	
	if (sessionId==null) {
    
	 Method aMethod = req.getClass().getMethod("getSession", boolean.class);
	 Object aSession = (Object) aMethod.invoke(req, true);
		 
	 Method aMethod2 = aSession.getClass().getMethod("getId");
	 String aId = (String) aMethod2.invoke(aSession);	 

	sessionId = (req != null) ? aId : null; 
	SESSIONREGISTRY.storeThreadLocalSessionId(sessionId);

	}
	
		
	
	Object retVal;

//	long traceId = CFREGISTRY.recallThreadLocalTraceId(); // traceId, -1 if entry point



	try {	
		final Object rep = (Object) thisJoinPoint.getArgs()[1];
		HtmlResponseWrapper capturingResponseWrapper = new HtmlResponseWrapper(
				(HttpServletResponse) rep);
		Object[] arg0 = {req,capturingResponseWrapper};
		retVal = thisJoinPoint.proceed(arg0);
		
		if (CTRLINST.isRumEnable()){
			final String rumServer = CTRLINST.getRumServer();

		String isBeaconed = null;
		String content = capturingResponseWrapper.getCaptureAsString();
		StringBuilder _sb = new StringBuilder(content);
		
		 Method aMethod3 = rep.getClass().getMethod("getContentType");
		 String aContentType = (String) aMethod3.invoke(rep);
		 
		if (aContentType != null
				&& aContentType.contains("text/html")) {			
			if (content.contains("boomerang") ) 
			{
				isBeaconed="true";
			}

			if ( isBeaconed == null ) {								
				String rum= "<script src=\"http://"+rumServer+"/boomerang-master/boomerang.js\"></script> "
						+ "<script src=\"http://"+rumServer+"/boomerang-master/plugins/navtiming.js\"></script> "
						+ "<script src=\"http://"+rumServer+"/boomerang-master/plugins/rt.js\"></script> "
						+ "<script type=\"text/javascript\" > "
						+ "  BOOMR.init({ "
						+ "	traceid: \""+traceId+"\","
						+ "	sessionid: \""+sessionId+"\","
						+ "      beacon_url: \"http://"+rumServer+"/\" "
						+ " }); "
						+ "</script>  ";										
						_sb.insert(0, rum );
						
						Method aMethod4 = rep.getClass().getMethod("setContentLength", int.class);
						aMethod4.invoke(rep, _sb.toString().length());
						 
						 
					//	rep.setContentLength(_sb.toString().length());				
					}				
				}
		
		Method aMethod5 = rep.getClass().getMethod("getWriter");
		Object aWriter = (Object) aMethod5.invoke(rep);
		
		Method aMethod6 = aWriter.getClass().getDeclaredMethod("write", String.class);
		aMethod6.setAccessible(Boolean.TRUE); // here
		aMethod6.invoke(aWriter, _sb.toString());
		
			//	rep.getWriter().write(_sb.toString());		
		}
		
			} finally {
				
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