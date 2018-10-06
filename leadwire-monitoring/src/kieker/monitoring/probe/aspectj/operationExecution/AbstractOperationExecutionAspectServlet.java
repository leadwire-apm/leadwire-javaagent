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

package kieker.monitoring.probe.aspectj.operationExecution;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kieker.monitoring.core.controller.IMonitoringController;
import kieker.monitoring.core.controller.MonitoringController;
import kieker.monitoring.core.registry.ControlFlowRegistry;
import kieker.monitoring.core.registry.SessionRegistry;
import kieker.monitoring.probe.aspectj.operationExecution.AbstractOperationExecutionAspect;
import kieker.monitoring.writer.tcp.DualSocketTcpWriter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @author Wassim Dhib
 * 
 * @since 1.0
 */
@Aspect
public abstract class AbstractOperationExecutionAspectServlet extends AbstractOperationExecutionAspect {
	
	private static final ControlFlowRegistry CFREGISTRY = ControlFlowRegistry.INSTANCE;
	private static final SessionRegistry SESSIONREGISTRY = SessionRegistry.INSTANCE;
	private static final IMonitoringController CTRLINST = MonitoringController.getInstance();
	
	@Pointcut
	public abstract void monitoredServlet(final HttpServletRequest request, final HttpServletResponse response);

	@Around("monitoredServlet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse) && notWithinKieker()")
public Object servlet(final ProceedingJoinPoint thisJoinPoint) throws Throwable { // NOCS (Throwable)
if (!CTRLINST.isMonitoringEnabled()) {
	return thisJoinPoint.proceed();
}
if (!CTRLINST.isProbeActivated(this.signatureToLongString(thisJoinPoint.getSignature()))) {
	return thisJoinPoint.proceed();
}
final HttpServletRequest req = (HttpServletRequest) thisJoinPoint.getArgs()[0];
final String sessionId = (req != null) ? req.getSession(true).getId() : null; // NOPMD (assign null) // NOCS (inline cond)
	SESSIONREGISTRY.storeThreadLocalSessionId(sessionId);

	
	Object retVal;

	long traceId = CFREGISTRY.recallThreadLocalTraceId(); // traceId, -1 if entry point



	try {
		final HttpServletResponse rep = (HttpServletResponse) thisJoinPoint.getArgs()[1];
		HtmlResponseWrapper capturingResponseWrapper = new HtmlResponseWrapper(
				(HttpServletResponse) rep);
		Object[] arg0 = {req,capturingResponseWrapper};
		retVal = thisJoinPoint.proceed(arg0);
		
		if (CTRLINST.isRumEnable()){
			final String apmServer = CTRLINST.getapmServer();

		String isBeaconed = null;
		String content = capturingResponseWrapper.getCaptureAsString();
		StringBuilder _sb = new StringBuilder(content);
		
		if (rep.getContentType() != null
				&& rep.getContentType().contains("text/html")) {			
			if (content.contains("boomerang") ) 
			{
				isBeaconed="true";
			}
			if ( isBeaconed == null ) {								
				String rum= "<script src=\"http://"+apmServer+"/boomerang-master/boomerang.js\"></script> "
						+ "<script src=\"http://"+apmServer+"/boomerang-master/plugins/rt.js\"></script> "
						+ "<script type=\"text/javascript\" > "
						+ "  BOOMR.init({ "
						+ "	traceid: \""+traceId+"\","
						+ "	sessionid: \""+sessionId+"\","
						+ "      beacon_url: \"http://"+apmServer+"/\" "
						+ " }); "
						+ "</script>  ";										
						_sb.insert(0, rum );
						rep.setContentLength(_sb.toString().length());				
					}				
				}
				rep.getWriter().write(_sb.toString());		
		}
		
			} finally {
				SESSIONREGISTRY.unsetThreadLocalSessionId();
			}
			return retVal;
			
}



@Pointcut
public abstract void monitoredServletservice(final HttpServletRequest request, final HttpServletResponse response);

@Around("monitoredServletservice(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse) && notWithinKieker()")
public Object servletservice(final ProceedingJoinPoint thisJoinPoint) throws Throwable { // NOCS (Throwable)
if (!CTRLINST.isMonitoringEnabled()) {
	return thisJoinPoint.proceed();
}
if (!CTRLINST.isProbeActivated(this.signatureToLongString(thisJoinPoint.getSignature()))) {
	return thisJoinPoint.proceed();
}
final HttpServletRequest req = (HttpServletRequest) thisJoinPoint.getArgs()[0];
final String sessionId = (req != null) ? req.getSession(true).getId() : null; // NOPMD (assign null) // NOCS (inline cond)
SESSIONREGISTRY.storeThreadLocalSessionId(sessionId);
Object retVal;

long traceId = CFREGISTRY.recallThreadLocalTraceId(); // traceId, -1 if entry point



try {
	final HttpServletResponse rep = (HttpServletResponse) thisJoinPoint.getArgs()[1];
	HtmlResponseWrapper capturingResponseWrapper = new HtmlResponseWrapper(
			(HttpServletResponse) rep);
	Object[] arg0 = {req,capturingResponseWrapper};
	retVal = thisJoinPoint.proceed(arg0);
	
	if (CTRLINST.isRumEnable()){
		final String apmServer = CTRLINST.getapmServer();

	String isBeaconed = null;
	String content = capturingResponseWrapper.getCaptureAsString();
	StringBuilder _sb = new StringBuilder(content);
	
	if (rep.getContentType() != null
			&& rep.getContentType().contains("text/html")) {			
		if (content.contains("boomerang") ) 
		{
			isBeaconed="true";
		}
		if ( isBeaconed == null ) {								
			String rum= "<script src=\"http://"+apmServer+"/boomerang-master/boomerang.js\"></script> "
					+ "<script src=\"http://"+apmServer+"/boomerang-master/plugins/rt.js\"></script> "
					+ "<script type=\"text/javascript\" > "
					+ "  BOOMR.init({ "
					+ "	traceid: \""+traceId+"\","
					+ "	sessionid: \""+sessionId+"\","
					+ "      beacon_url: \"http://"+apmServer+"/\" "
					+ " }); "
					+ "</script>  ";										
					_sb.insert(0, rum );
					rep.setContentLength(_sb.toString().length());				
				}				
			}
			rep.getWriter().write(_sb.toString());		
	}
	
		} finally {
			SESSIONREGISTRY.unsetThreadLocalSessionId();
		}
		return retVal;
	}	
}