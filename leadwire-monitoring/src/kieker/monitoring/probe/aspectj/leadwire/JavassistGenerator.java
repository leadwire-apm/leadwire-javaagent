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


import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.NotFoundException;

public class JavassistGenerator {

	public static Class generateHtmlResponseWrapper(String className, ClassLoader cl) throws NotFoundException,
			CannotCompileException, ClassNotFoundException {

		ClassPool pool = ClassPool.getDefault();
		CtClass cc ;
		 try {
			cc = pool.get (className); 
			cc.defrost();
			cc.stopPruning(true);
			return cc.toClass(cl);
            } catch (NotFoundException e) {
            cc = pool.makeClass(className);
            }
		 
	    Class<?> aClassHttpServletResponseWrapper = Class.forName("javax.servlet.http.HttpServletResponseWrapper", true, cl);

		cc.setSuperclass(resolveCtClass(aClassHttpServletResponseWrapper));

		Class<?> aClassServletOutputStream = Class.forName("javax.servlet.ServletOutputStream", true, cl);
		Class<?> aFieldcapture = java.io.ByteArrayOutputStream.class;
		Class<?> aFieldoutput = aClassServletOutputStream;
		Class<?> aFieldwriter = java.io.PrintWriter.class;
				
		cc.addField(new CtField(resolveCtClass(aFieldcapture), "capture", cc));
		cc.addField(new CtField(resolveCtClass(aFieldoutput), "output", cc));
		cc.addField(new CtField(resolveCtClass(aFieldwriter), "writer", cc));

		Class<?> aClassHttpServletResponse = Class.forName("javax.servlet.http.HttpServletResponse", true, cl);
		ClassClassPath ccpr = new ClassClassPath(aClassHttpServletResponse);
		pool.insertClassPath(ccpr);
				 
		CtConstructor defaultConstructor = CtNewConstructor.make("public " + cc.getSimpleName() + "(javax.servlet.http.HttpServletResponse rep) {super(rep); capture = new java.io.ByteArrayOutputStream(((javax.servlet.http.HttpServletResponse) rep).getBufferSize());}", cc);
	    cc.addConstructor(defaultConstructor);
	    cc.addMethod(generate_flushBuffer(cc));
		cc.addMethod(generate_getOutputStream(cc));
		cc.addMethod(generate_getWriter(cc));
		cc.addMethod(generate_getCaptureAsBytes(cc));
		cc.addMethod(generate_getCaptureAsString(cc));

		cc.stopPruning(true);
		return cc.toClass(cl);
         
	}


	private static CtMethod generate_flushBuffer (CtClass declaringClass )
			throws CannotCompileException {
		StringBuffer sb = new StringBuffer();
		sb.append("	public void flushBuffer() throws java.io.IOException {\r\n" + 
				"		super.flushBuffer();\r\n" + 
				"\r\n" + 
				"		if (writer != null) {\r\n" + 
				"			writer.flush();\r\n" + 
				"		} else if (output != null) {\r\n" + 
				"			output.flush();\r\n" + 
				"		}\r\n" + 
				"	}");
		return CtMethod.make(sb.toString(), declaringClass);
	
	}

	private static CtMethod generate_getWriter (CtClass declaringClass )
			throws CannotCompileException {
		StringBuffer sb = new StringBuffer();
		sb.append("public java.io.PrintWriter getWriter() throws java.io.IOException {\r\n" + 
				"	if (output != null) {\r\n" + 
				"		throw new java.lang.IllegalStateException(\"getOutputStream() has already been called on this response.\");\r\n" + 
				"		}\r\n" + 
				"\r\n" + 
				"		if (writer == null) {\r\n" + 
				"			writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(capture,getCharacterEncoding()));\r\n" + 
				"		}\r\n" + 
				"\r\n" + 
				"		return writer;\r\n" + 
				"	}");
		return CtMethod.make(sb.toString(), declaringClass);
	
	}
	
	
	private static CtMethod generate_getCaptureAsBytes (CtClass declaringClass )
			throws CannotCompileException {
		StringBuffer sb = new StringBuffer();
		sb.append("public byte[] getCaptureAsBytes() throws java.io.IOException {\r\n" + 
				"		if (writer != null) {\r\n" + 
				"			writer.close();\r\n" + 
				"		} else if (output != null) {\r\n" + 
				"			output.close();\r\n" + 
				"		}\r\n" + 
				"\r\n" + 
				"		return capture.toByteArray();\r\n" + 
				"	}");
		return CtMethod.make(sb.toString(), declaringClass);
	
	}
	
	private static CtMethod generate_getCaptureAsString(CtClass declaringClass )
			throws CannotCompileException {
		StringBuffer sb = new StringBuffer();
		sb.append("public String getCaptureAsString() throws java.io.IOException {\r\n" + 
				"		return new String(getCaptureAsBytes(), getCharacterEncoding());\r\n" + 
				"	}");
		return CtMethod.make(sb.toString(), declaringClass);
	
	}
	
	private static CtMethod generate_getOutputStream (CtClass declaringClass )
			throws CannotCompileException {
		StringBuffer sb = new StringBuffer();
		sb.append("public javax.servlet.ServletOutputStream getOutputStream() {\r\n" + 
				"		if (writer != null) {\r\n" + 
				"			throw new java.lang.IllegalStateException(\r\n" + 
				"					\"getWriter() has already been called on this response.\");\r\n" + 
				"	}\r\n" + 
				"\r\n" + 
				"	if (output == null) {\r\n" + 
				"		output = new kieker.monitoring.probe.aspectj.leadwire.javassist.customServletOutputStream(capture) ; "+ //{\r\n" + 
				"	}\r\n" + 
				"\r\n" + 
				"	return output;\r\n" + 
				"}");
		return CtMethod.make(sb.toString(), declaringClass);
	
	}

	private static CtClass resolveCtClass(Class clazz) throws NotFoundException {
		ClassPool pool = ClassPool.getDefault();
		return pool.get(clazz.getName());
	}


	public static Class<?> generatecustomServletOutputStream(String string, ClassLoader cl) throws ClassNotFoundException, CannotCompileException, NotFoundException {
		ClassPool pool = ClassPool.getDefault();
		CtClass acustomServletOutputStream;
		 try {
			 acustomServletOutputStream = pool.get ("kieker.monitoring.probe.aspectj.leadwire.javassist.customServletOutputStream"); // just getting the class

           } catch (NotFoundException e) {
           	acustomServletOutputStream = pool.makeClass("kieker.monitoring.probe.aspectj.leadwire.javassist.customServletOutputStream");

				Class<?> aClassServletOutputStream = Class.forName("javax.servlet.ServletOutputStream", true, cl);
				ClassClassPath aClassClassPath1 = new ClassClassPath(aClassServletOutputStream);
				pool.insertClassPath(aClassClassPath1);
				 
				acustomServletOutputStream.setSuperclass(resolveCtClass(aClassServletOutputStream));
				 Class<?> aFieldcapture = java.io.ByteArrayOutputStream.class;
	
				acustomServletOutputStream.addField(new CtField(resolveCtClass(aFieldcapture), "capture", acustomServletOutputStream));
					 CtConstructor defaultConstructor = CtNewConstructor.make("public customServletOutputStream(java.io.ByteArrayOutputStream pCapture){\r\n" + 
					 		"		super();\r\n" + 
					 		"		capture=pCapture;\r\n" + 
					 		"		}", acustomServletOutputStream);
				acustomServletOutputStream.addConstructor(defaultConstructor);
				      
				acustomServletOutputStream.addMethod(generate_write(acustomServletOutputStream));
				acustomServletOutputStream.addMethod(generate_flush(acustomServletOutputStream));
				acustomServletOutputStream.addMethod(generate_close(acustomServletOutputStream));
				acustomServletOutputStream.toClass();
              
           }
		return null;
	}
	
	private static CtMethod generate_write (CtClass declaringClass )
			throws CannotCompileException {
		StringBuffer sb = new StringBuffer();
		sb.append("public void write(int b) throws java.io.IOException {\r\n" + 
				"		capture.write(b);\r\n" + 
				"	}");
		return CtMethod.make(sb.toString(), declaringClass);
	
	}
	
	private static CtMethod generate_flush (CtClass declaringClass )
			throws CannotCompileException {
		StringBuffer sb = new StringBuffer();
		sb.append("public void flush() throws java.io.IOException {\r\n" + 
				"		capture.flush();\r\n" + 
				"	}");
		return CtMethod.make(sb.toString(), declaringClass);
	
	}
	
	private static CtMethod generate_close (CtClass declaringClass )
			throws CannotCompileException {
		StringBuffer sb = new StringBuffer();
		sb.append("	public void close() throws java.io.IOException {\r\n" + 
				"		capture.close();\r\n" + 
				"		\r\n" + 
				"		\r\n" + 
				"	}");
		return CtMethod.make(sb.toString(), declaringClass);
	
	}
	
	
}
