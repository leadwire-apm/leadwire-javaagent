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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import javassist.ClassPool;
import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;
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
public abstract class AbstractServletAspect extends AbstractAspectJProbe {

	private static final ControlFlowRegistry CFREGISTRY = ControlFlowRegistry.INSTANCE;
	private static final SessionRegistry SESSIONREGISTRY = SessionRegistry.INSTANCE;
	private static final IMonitoringController CTRLINST = MonitoringController.getInstance();
	private static final Log LOG = LogFactory.getLog(AbstractOperationExecutionAspect.class);
	private static final String VMNAME = CTRLINST.getHostname();
	private static final ITimeSource TIME = CTRLINST.getTimeSource();



	@Pointcut
	public abstract void monitoredServletservice(final javax.servlet.http.HttpServletRequest request, final javax.servlet.http.HttpServletResponse response);

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
		}
		else {
			
			return thisJoinPoint.proceed();
			
		}
		
		
		// measure before
		final long tin = TIME.getTime(); 

		final Object req = (Object) thisJoinPoint.getArgs()[0];



		if (sessionId==null) {

			Method aMethodgetSession = req.getClass().getMethod("getSession", boolean.class);
			aMethodgetSession.setAccessible(Boolean.TRUE); 
			Object aSession = (Object) aMethodgetSession.invoke(req, true); 

			Method aMethodgetId = aSession.getClass().getMethod("getId");
			aMethodgetId.setAccessible(Boolean.TRUE); 
			String aId = (String) aMethodgetId.invoke(aSession);	 

			sessionId = (req != null) ? aId : null; 
			SESSIONREGISTRY.storeThreadLocalSessionId(sessionId);

		}

		Object retVal;
		String completeURL = null;
		try {
			// check getRequestURL for static resources  jsp/css/js/png/jpg/gif
			Method aMethodgetRequestURL = req.getClass().getMethod("getRequestURL");
			aMethodgetRequestURL.setAccessible(Boolean.TRUE); 
			StringBuffer requestURL =  (StringBuffer) aMethodgetRequestURL.invoke(req);
			completeURL = requestURL.toString();

			if (	completeURL.endsWith(".jsp") || 
					completeURL.endsWith(".css") || 
					completeURL.endsWith(".js")  || 
					completeURL.endsWith(".gif") || 
					completeURL.endsWith(".jpg") ||
					completeURL.endsWith(".png")) {
				retVal = thisJoinPoint.proceed();
			} 
			else {
				
				final Object rep = (Object) thisJoinPoint.getArgs()[1];			
				
				////// Begin of javassist phase
				ClassPool pool = ClassPool.getDefault();
				
				 try {
					 rep.getClass().getClassLoader().loadClass("kieker.monitoring.probe.aspectj.leadwire.javassist.customServletOutputStream");	 
				 } catch (ClassNotFoundException e) {
					 JavassistGenerator.generatecustomServletOutputStream("kieker.monitoring.probe.aspectj.leadwire.javassist.customServletOutputStream",rep.getClass().getClassLoader());
					 
				 }
				 		
				 Class<?> clazzHtmlResponseWrapper = null;
				 try {
					 clazzHtmlResponseWrapper = rep.getClass().getClassLoader().loadClass("kieker.monitoring.probe.aspectj.leadwire.javassist.HtmlResponseWrapper");	 
				 } catch (ClassNotFoundException e) {
					 clazzHtmlResponseWrapper = JavassistGenerator.generateHtmlResponseWrapper("kieker.monitoring.probe.aspectj.leadwire.javassist.HtmlResponseWrapper", rep.getClass().getClassLoader());
					 
				 }
				 					
				  Class<?> aClassHttpServletResponse = Class.forName("javax.servlet.http.HttpServletResponse", true, rep.getClass().getClassLoader());
				//  ClassClassPath aClassClassPath2 = new ClassClassPath(aClassHttpServletResponse);
				//  pool.insertClassPath(aClassClassPath2);
				 
				Constructor<?> aConstructor = clazzHtmlResponseWrapper.getDeclaredConstructor(aClassHttpServletResponse);				
				Object capturingResponseWrapper = aConstructor.newInstance(rep);
				
				////// End of javassist phase
				
				Object[] arg0 = {req,capturingResponseWrapper};
				retVal = thisJoinPoint.proceed(arg0);

				if (CTRLINST.isRumEnable()){
					final String apmServer = CTRLINST.getapmServer();
					final String cdnServer = CTRLINST.getCDNServer();

					final String appUuid = CTRLINST.getAppUuid();


					String isBeaconed = null;
					Method aMethodgetCaptureAsString = capturingResponseWrapper.getClass().getMethod("getCaptureAsString");
					aMethodgetCaptureAsString.setAccessible(Boolean.TRUE); 
					String content = (String) aMethodgetCaptureAsString.invoke(capturingResponseWrapper);

					
					
					StringBuilder _sb = new StringBuilder(content);

					Method aMethodgetContentType = rep.getClass().getMethod("getContentType");
					aMethodgetContentType.setAccessible(Boolean.TRUE); 
					String aContentType = (String) aMethodgetContentType.invoke(rep);

					if (aContentType != null
							&& aContentType.contains("text/html")) {			
						if (content.contains(appUuid) ) 
						{
							isBeaconed="true"; 
						}

						if ( isBeaconed == null ) {		

							/*String rum= "\n"
									+ "<script type=\"text/javascript\" src=\"https://"+apmServer+"/rum/boomerang-master/boomerang.js\"></script> "
									+ "<script type=\"text/javascript\" src=\"https://"+apmServer+"/rum/boomerang-master/plugins/navtiming.js\"></script> "
									+ "<script type=\"text/javascript\" src=\"https://"+apmServer+"/rum/boomerang-master/plugins/rt.js\"></script> "
									+ "<script type=\"text/javascript\" > "
									+ "  BOOMR.init({ "
									+ "	traceid: \""+traceId+"\","
									+ "	sessionid: \""+sessionId+"\","
									+ "	appuuid: \""+appUuid+"\","
									+ "      beacon_url: \"https://"+apmServer+"/rum/\" "
									+ " }); "
									+ "</script> "
									+ "\n";
									*/
							
							String rum ="<script>\n" + 
									"(function(){\n" + 
									"  // Boomerang Loader Snippet version 10\n" + 
									"  if (window.BOOMR && (window.BOOMR.version || window.BOOMR.snippetExecuted)) {\n" + 
									"    return;\n" + 
									"  }\n" + 
									"\n" + 
									"  window.BOOMR = window.BOOMR || {};\n" + 
									"  window.BOOMR.snippetExecuted = true;\n" + 
									"  \n" + 
									"  BOOMR_sessionid=\""+sessionId+"\";\n" + 
									"  BOOMR_traceid=\""+traceId+"\";\n" + 
									"  BOOMR_appuuid=\""+appUuid+"\";\n" + 
									"  BOOMR_apmServer=\""+apmServer+"\";\n" + 
									"\n" + 
									"  var dom, doc, where, iframe = document.createElement(\"iframe\"), win = window;\n" + 
									"\n" + 
									"  function boomerangSaveLoadTime(e) {\n" + 
									"    win.BOOMR_onload = (e && e.timeStamp) || new Date().getTime();\n" + 
									"  }\n" + 
									"\n" + 
									"  if (win.addEventListener) {\n" + 
									"    win.addEventListener(\"load\", boomerangSaveLoadTime, false);\n" + 
									"  } else if (win.attachEvent) {\n" + 
									"    win.attachEvent(\"onload\", boomerangSaveLoadTime);\n" + 
									"  }\n" + 
									"\n" + 
									"  iframe.src = \"javascript:void(0)\";\n" + 
									"  iframe.title = \"\";\n" + 
									"  iframe.role = \"presentation\";\n" + 
									"  (iframe.frameElement || iframe).style.cssText = \"width:0;height:0;border:0;display:none;\";\n" + 
									"  where = document.getElementsByTagName(\"script\")[0];\n" + 
									"  where.parentNode.insertBefore(iframe, where);\n" + 
									"\n" + 
									"  try {\n" + 
									"    doc = iframe.contentWindow.document;\n" + 
									"  } catch (e) {\n" + 
									"    dom = document.domain;\n" + 
									"    iframe.src = \"javascript:var d=document.open();d.domain='\" + dom + \"';void(0);\";\n" + 
									"    doc = iframe.contentWindow.document;\n" + 
									"  }\n" + 
									"\n" + 
									"  doc.open()._l = function() {\n" + 
									"    var js = this.createElement(\"script\");\n" + 
									"    if (dom) {\n" + 
									"      this.domain = dom;\n" + 
									"    }\n" + 
									"    js.id = \"boomr-if-as\";\n" + 
									"    js.src = 'https://"+cdnServer+"/boomerang-1.0.0.min.js';\n" + 
									"    BOOMR_lstart = new Date().getTime();\n" + 
									"    this.body.appendChild(js);\n" + 
									"  };\n" + 
									"  doc.write('<bo' + 'dy onload=\"document._l();\">');\n" + 
									"  doc.close();\n" + 
									"})();\n" + 
									"</script>";

							int indexOfHead = _sb.indexOf("</head>");

							if (indexOfHead>=0) {

								_sb.insert(indexOfHead, rum );

								
								Method aMethodgetHeader= rep.getClass().getMethod("getHeader", String.class);
								aMethodgetHeader.setAccessible(Boolean.TRUE); 
								String aTransferEncoding = (String) aMethodgetHeader.invoke(rep, "Transfer-Encoding");
								
								if (aTransferEncoding==null || ! aTransferEncoding.contains("chunked")) {

								Method aMethodsetContentLength = rep.getClass().getMethod("setContentLength", int.class);
								aMethodsetContentLength.setAccessible(Boolean.TRUE); 
								aMethodsetContentLength.invoke(rep, _sb.toString().length());
								
								}
								
							}
						}				
					}

					Method aMethodgetWriter = rep.getClass().getMethod("getWriter");
					aMethodgetWriter.setAccessible(Boolean.TRUE); 
						Object aWriter = (Object) aMethodgetWriter.invoke(rep);

					Method aMethodwrite = aWriter.getClass().getDeclaredMethod("write", String.class);
					aMethodwrite.setAccessible(Boolean.TRUE); 
					aMethodwrite.invoke(aWriter, _sb.toString());

				}

			} 

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