/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package br.com.uiltonsites.servletframework.http;

import br.com.uiltonsites.servletframework.interfaces.SFAllowCORS;
import br.com.uiltonsites.servletframework.utility.SFLogger;
import br.com.uiltonsites.servletframework.interfaces.SFWebMethod;
import br.com.uiltonsites.servletframework.utility.SFMsisdn;
import br.com.uiltonsites.servletframework.utility.exceptions.SFParseParameterException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ApplicationScoped;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 *
 * @author Uilton Oliveira - uilton.dev@gmail.com
 */
public abstract class SFHttpServlet extends HttpServlet {

    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_HEAD = "HEAD";
    public static final String METHOD_GET = "GET";
    public static final String METHOD_OPTIONS = "OPTIONS";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_TRACE = "TRACE";
    public static final String METHOD_ALL = "ALL";
    public boolean ignoreExtension = true;

    public final static String CONTENT_TYPE_APPLICATION_XML = "application/xml";
    public final static String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public final static String CONTENT_TYPE_APPLICATION_JSONP = "application/javascript";
    public final static String CONTENT_TYPE_TEXT_HTML = "text/html";
    public final static String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    protected String requestEncoding = "UTF-8";
    protected String responseEncoding = "UTF-8";

    protected SFLogger logger;

    public SFHttpServlet(SFLogger logger) {
        this.logger = logger;
    }

    public SFHttpServlet() {
        this.logger = SFLogger.getLogger(this.getClass());
    }

    public String getRequestEncoding() {
        return requestEncoding;
    }

    protected void setRequestEncoding(String requestEncoding) {
        this.requestEncoding = requestEncoding;
    }

    public String getResponseEncoding() {
        return responseEncoding;
    }

    protected void setResponseEncoding(String responseEncoding) {
        this.responseEncoding = responseEncoding;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processRequest(req, resp, getLogger());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processRequest(req, resp, getLogger());
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp, getLogger());
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doHead(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp, getLogger());
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doOptions(req, resp);

        checkAllowCors(req, resp, null);
    }

    protected SFLogger getLogger() {
        SFLogger thisLogger;
        thisLogger = SFLogger.getLogger(this.getClass());
        return thisLogger;
    }

    /**
     * Search an method in the class that extend this, based on name, by reflection
     *
     * @param name
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    protected Method getMethodToCall(String name) throws SecurityException, NoSuchMethodException {
        // search by the name of the method
        for (Method declaredMethod : this.getClass().getDeclaredMethods()) {
            if (declaredMethod.getName().equals(name)) {
                return declaredMethod;
            }
        }
        throw new NoSuchMethodException("No such method named: " + name);
    }

    protected Method getMethodAnnotatedWithName(List<String> urlParams, String name, HttpServletRequest request) {
        final List<Method> methods = new ArrayList<>();
        Class<?> klass = this.getClass();
        final Method[] allMethods = klass.getDeclaredMethods();
        for (final Method method : allMethods) {
            if (method.isAnnotationPresent(SFWebMethod.class)) {
                SFWebMethod wm = method.getAnnotation(SFWebMethod.class);
                if (wm.name().equals(name) && isHttpMethodTypeCorrect(wm, method, urlParams, request)) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * Search an method in the class that extend this, based on name, by reflection
     *
     * @param urlParams
     * @param request
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    protected Method getMethodToCall(List<String> urlParams, HttpServletRequest request) throws SecurityException, NoSuchMethodException {

        String methodToCall = urlParams.get(0);

        if (ignoreExtension && methodToCall.contains(".")) {
            methodToCall = methodToCall.substring(0, methodToCall.indexOf("."));
        }

        urlParams.remove(0);

        // search by the name of the method
        for (Method declaredMethod : this.getClass().getDeclaredMethods()) {
            if (declaredMethod.getName().equals(methodToCall)) {

                SFWebMethod wm = declaredMethod.getAnnotation(SFWebMethod.class);
                if (isHttpMethodTypeCorrect(wm, declaredMethod, urlParams, request)) {

                    return declaredMethod;
                }
            }
        }

        // search by the attribute name of the annotation SFWebMethod
        Method m = getMethodAnnotatedWithName(urlParams, methodToCall, request);
        if (m == null) {
            throw new NoSuchMethodException("Method with name '" + methodToCall + "' could not be found.");
        } else {
            return m;
        }

    }

    protected String prepareError(int statusCode, String errorCode, String reason, String contentType, HttpServletResponse response) {
        response.setStatus(statusCode);
        response.setHeader("content-type", contentType);
        return "{\"success\":false,\"error_code\":\"" + errorCode + "\",\"reason\":\"" + reason + "\"}";
    }

    protected Object parseUnrecognizedParameter(SFLogger thisLogger, Method method, Parameter p, SFServletContainer container) throws SFParseParameterException {
        return null;
    }

    protected Object parseRecognizedParameter(SFLogger thisLogger, Method method, Parameter p, Object value, SFServletContainer container) throws SFParseParameterException {
        return value;
    }

    protected List<Object> postParseParameters(SFLogger thisLogger, Method method, Parameter[] parameters, List<Object> values, SFServletContainer container) {
        return values;
    }

    protected List<Object> parseParameters(SFLogger thisLogger, Method method, Parameter[] parameters, SFServletContainer container) throws SFParseParameterException {

        List<Object> arrParam = new ArrayList<>();

        SFWebMethod wm = method.getAnnotation(SFWebMethod.class);

        for (Parameter p : parameters) {

            if (SFServletContainer.class.isAssignableFrom(p.getType())) {
                arrParam.add(parseRecognizedParameter(thisLogger, method, p, container, container));
                continue;
            }

            SFRequestParam arp = p.getAnnotation(SFRequestParam.class);
            if (arp != null) {

                Object paramValue;
                // parse param of type SFMsisdn
                if (SFMsisdn.class.isAssignableFrom(p.getType())) {

                    paramValue = container.getParameterLong(arp.name(), null);
                    if (paramValue == null && arp.required()) {
                        throw new SFParseParameterException("Required parameter was not found or have an invalid value: " + arp.name());
                    }

                    SFMsisdn msisdn;
                    if (paramValue == null && !arp.required() && !arp.default_value().equals(SFRequestParam.NULL_VALUE)) {
                        msisdn = new SFMsisdn(arp.default_value(), logger);
                    } else {
                        msisdn = new SFMsisdn((Long) paramValue, logger);
                    }

                    if (!msisdn.isValid() && arp.required()) {
                        throw new SFParseParameterException("framework_invalid_msisdn", "Received an invalid msisdn on parameter: " + arp.name());
                    }
                    paramValue = msisdn;

                    // parse params of system types (String, int, etc..) 
                } else {
                    paramValue = container.getParameter(arp.name(), null, p.getType());
                }

                if (paramValue == null && arp.required()) {
                    throw new SFParseParameterException("Required parameter was not found or have an invalid value: " + arp.name());
                }

                // trim & check if value is empty (in case of string)
                if (paramValue instanceof String) {
                    String val = ((String) paramValue).trim();
                    if (val.isEmpty() && arp.required()) {
                        throw new SFParseParameterException("Required parameter was not found or have an invalid value: " + arp.name());
                    }
                    arrParam.add(parseRecognizedParameter(thisLogger, method, p, val, container));
                    continue;
                }

                if (paramValue == null && !arp.required() && !arp.default_value().equals(SFRequestParam.NULL_VALUE)) {
                    paramValue = SFServletContainer.castString(arp.default_value(), null, p.getType());
                }

                arrParam.add(parseRecognizedParameter(thisLogger, method, p, paramValue, container));
                continue;
            }

            // Parse Path Parameters
            if (wm != null && !wm.pathVars().equals(SFWebMethod.defaultPathVars)) {

                SFPathParam app = p.getAnnotation(SFPathParam.class);
                if (app != null) {
                    List<SFPathValue> parse = SFPathUrlParser.parse(wm.pathVars(), container.getPathParameters());
                    if (parse != null) {
                        boolean found = false;
                        for (SFPathValue pv : parse) {
                            if (app.name().equals(pv.var)) {
                                
                                String defaultStrValue = app.default_value().equals(SFPathParam.NULL_VALUE) ? null : app.default_value();
                                Object defaultValue = SFServletContainer.castString(defaultStrValue, null, p.getType());
                                
                                Object value = SFServletContainer.castString(pv.value, null, p.getType());
                                
                                if (value == null && app.required()) {
                                    throw new SFParseParameterException("Required path parameter was not found or have an invalid value: " + app.name());
                                }
                                
                                if (value instanceof String) {
                                    String val = ((String) value).trim();
                                    if (val.isEmpty() && app.required()) {
                                        throw new SFParseParameterException("Required path parameter was not found or have an invalid value: " + app.name());
                                    }
                                    value = val;
                                }
                                
                                arrParam.add(parseRecognizedParameter(thisLogger, method, p, (value == null ? defaultValue : value), container));
                                
                                found = true;
                                break;
                                
                            }
                        }
                        if (found) {
                            continue;
                        }
                    }
                }
            }

            // if nothing found for that object, set it as null
            arrParam.add(parseUnrecognizedParameter(thisLogger, method, p, container));

        }

        return postParseParameters(thisLogger, method, parameters, arrParam, container);

    }

    protected Object invokeMethod(SFLogger thisLogger, Method method, Object instance, SFWebMethod annotation, SFServletContainer container, Parameter[] parameters, List<Object> paramsValue) throws Exception {
        return method.invoke(instance, paramsValue.toArray());
    }

    protected Object getMethodInstance(SFLogger thisLogger) throws Exception {
        if (this.getClass().getAnnotation(ApplicationScoped.class) != null) {
            return this;
        } else {
            return this.getClass().getConstructor(SFLogger.class).newInstance(thisLogger);
        }
    }

    protected void printOutput(Object output, PrintWriter writter) {
        // if the return is not null, output to user
        if (output != null) {
            writter.print(output.toString());
        }
    }

    protected boolean isHttpMethodTypeCorrect(SFWebMethod wm, Method m, List<String> urlParams, HttpServletRequest req) {

        // discard if theres no annotation SFWebMethod
        if (wm == null) {
            return false;
        }

        // discard if the type of request (get/post/etc) is different from the defined in SFWebMethod
        if (!wm.method().equals(METHOD_ALL) && !wm.method().equals(req.getMethod())) {
            return false;
        }

        if (!wm.pathVars().equals(SFWebMethod.defaultPathVars)) {
            return SFPathUrlParser.parse(wm.pathVars(), urlParams) != null;
        }

        return true;
    }

    /**
     * Call the method in the children class and output the return (if not null)
     *
     * @param thisLogger
     * @param out
     * @param m
     * @param container
     */
    protected void callDestMethod(SFLogger thisLogger, PrintWriter out, Method m, SFServletContainer container) {

        // discard if the container is null
        if (container == null) {
            thisLogger.error("container = null");
            out.print("Internal Server Error: container not found");
            return;
        }

        // discard if method is null
        if (m == null) {
            thisLogger.error("method = null");

            out.println(prepareError(404, "framework_no_such_method", "Page not Found", CONTENT_TYPE_APPLICATION_JSON, container.getResponse()));

            return;
        }

        SFWebMethod wm = m.getAnnotation(SFWebMethod.class);
        try {

            // Set the content type defined by annotation SFWebMethod
            container.setContentType(wm.contentType());

            Object obj;

            try {

                checkAllowCors(container.getRequest(), container.getResponse(), m);

                // call the method and capture the output
                Parameter[] params = m.getParameters();
                List<Object> paramsValue = parseParameters(thisLogger, m, params, container);
                obj = invokeMethod(thisLogger, m, getMethodInstance(thisLogger), wm, container, params, paramsValue);

                // print the output to the user
                printOutput(obj, out);

            } catch (SFParseParameterException ex) {

                thisLogger.error(ex.getMessage());

                out.println(prepareError(400, ex.getErrorCode(), ex.getMessage(), CONTENT_TYPE_APPLICATION_JSON, container.getResponse()));

            } catch (Exception ex) {
                thisLogger.error(ExceptionUtils.getRootCauseMessage(ex), ex);

                out.println(prepareError(500, "framework_unhandled_exception", "Internal Server Error: " + ExceptionUtils.getRootCauseMessage(ex), CONTENT_TYPE_APPLICATION_JSON, container.getResponse()));

            }

        } catch (IllegalArgumentException ex) {
            thisLogger.error("exception", ex);

            out.println(prepareError(500, "framework_unhandled_exception", "Internal Server Error: " + ExceptionUtils.getRootCauseMessage(ex), CONTENT_TYPE_APPLICATION_JSON, container.getResponse()));

        }
    }

    protected void checkAllowCors(HttpServletRequest request, HttpServletResponse response, Method method) {

        
        boolean classHasAnnotation = this.getClass().getAnnotation(SFAllowCORS.class) != null;
        
        if (classHasAnnotation) {
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "OPTIONS, GET, POST, DELETE, PUT");
            response.addHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
        }
    }

    /**
     * Method to proccess an request of type OPTIONS
     *
     * @param request
     * @param response
     */
    protected void processOptionsRequest(HttpServletRequest request, HttpServletResponse response, SFLogger thisLogger) {
        try {
            // set the encoding of the request
            request.setCharacterEncoding(getRequestEncoding());
        } catch (UnsupportedEncodingException ex) {
            thisLogger.error("Failed to set charset: " + ExceptionUtils.getRootCauseMessage(ex), ex);
        }
        response.setCharacterEncoding(getResponseEncoding());

        try {

            // Get the current url, excluding what was defined in urlPatterns
            String pathInfo = request.getPathInfo();

            Method method;


            if (pathInfo != null && pathInfo.length() > 1) {

                // Current url is not empty, then let's split and check if any method
                // on children class that match the request

                List<String> urlParams = splitParams(pathInfo, "/");
                method = getMethodToCall(urlParams, request);


            } else {

                // URL is empty, then let's check if there's an method named index in children class
                method = getMethodToCall("index");

            }

            checkAllowCors(request, response, method);

        } catch (Exception ex) {
            logger.error("Unhandled Exception", ex);
        }

    }

    protected SFServletContainer newSFServletContainer(HttpServletRequest request, HttpServletResponse response, List<String> urlParams, PrintWriter out, SFLogger logger) {
        return new SFServletContainer(request, response, urlParams, out, logger);
    }


    /**
     * Main method to proccess the requests
     *
     * @param request
     * @param response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response, SFLogger thisLogger) {

        try {
            // Set the request encoding
            request.setCharacterEncoding(getRequestEncoding());
        } catch (UnsupportedEncodingException ex) {
            thisLogger.error("Failed to set charset: " + ExceptionUtils.getRootCauseMessage(ex), ex);
        }

        response.setCharacterEncoding(getResponseEncoding());
        try (PrintWriter out = response.getWriter()) {

            // Get the current url, excluding what was defined in urlPatterns
            String pathInfo = request.getPathInfo();

            Method method;

            try {

                SFServletContainer container;

                if (pathInfo != null && pathInfo.length() > 1) {

                    // Current url is not empty, then let's split and check if any method
                    // on children class that match the request

                    List<String> urlParams = splitParams(pathInfo, "/");
                    container = newSFServletContainer(request, response, urlParams, out, thisLogger);
                    method = getMethodToCall(urlParams, request);

                } else {

                    // URL is empty, then let's check if there's an method named index in children class

                    List<String> urlParams = new ArrayList<>();
                    container = newSFServletContainer(request, response, urlParams, out, thisLogger);
                    method = getMethodToCall("index");

                }

                // Call the requested method on children class
                callDestMethod(thisLogger, out, method, container);

            } catch (NoSuchMethodException ex) {
                // In case the method is not found in children class

                out.println(prepareError(404, "framework_no_such_method", "Page not Found", CONTENT_TYPE_APPLICATION_JSON, response));

            } catch (Exception ex) {
                // In case of error not handled

                thisLogger.error(ex.getMessage(), ex);
                out.println(prepareError(500, "framework_unhandled_exception", "Internal Server Error: " + ExceptionUtils.getRootCauseMessage(ex), CONTENT_TYPE_APPLICATION_JSON, response));
            }
        } catch (IOException ex) {
            response.setStatus(500);
            thisLogger.error("IOException", ex);
        } catch (Exception ex) {
            response.setStatus(500);
            thisLogger.error(ex.getMessage(), ex);
        }
    }

    protected static List<String> splitParams(String input, String delim) {
        List<String> list = new ArrayList<>();
        int offset = 0;
        while (true) {
            int index = input.indexOf(delim, offset);
            if (index == -1) {
                list.add(input.substring(offset));
                return list;
            } else {
                if (offset > 0) {
                    list.add(input.substring(offset, index));
                }
                offset = (index + delim.length());
            }
        }
    }

    /**
     * Return true if any of the objects is null
     *
     * @param obj
     * @return
     */
    public boolean isNullObjects(Object... obj) {
        for (Object o : obj) {
            if (o == null) {
                return true;
            }
        }
        return false;
    }

}
