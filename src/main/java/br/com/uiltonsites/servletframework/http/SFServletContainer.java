/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.uiltonsites.servletframework.http;

import br.com.uiltonsites.servletframework.utility.SFLogger;
import br.com.uiltonsites.servletframework.abstracts.SFMyLogger;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

/**
 * Container that encapsulate the HttpRequest and HttpResponse
 * and contains few method that make easier to manager requests
 *
 * @author Uilton Oliveira <uilton.dev@gmail.com>
 *
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
 *
 */
public class SFServletContainer extends SFMyLogger {

    protected String viewPath = "/WEB-INF/views/";
    protected String loginUrl = "admin/login";
    protected String loggedInAttribute = "isLoggedIn";
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected List<String> pathParameters;
    protected Map<String, Object> jsVarMap = new HashMap<>();
    protected PrintWriter out;

    public SFServletContainer(HttpServletRequest request, HttpServletResponse response, List<String> pathParameters, PrintWriter out, SFLogger logger) {
        super(logger);
        this.request = request;
        this.response = response;
        this.pathParameters = pathParameters;
        this.out = out;
    }

    public SFServletContainer(HttpServletRequest request, HttpServletResponse response, PrintWriter out, List<String> pathParameters) {
        super();
        this.request = request;
        this.response = response;
        this.pathParameters = pathParameters;
        this.out = out;
    }

    /**
     * Return true in case of specified param is null
     *
     * @param name
     * @return
     */
    public boolean isEmptyParam(String name) {
        String param = request.getParameter(name);
        return param == null || param.isEmpty();
    }

    /**
     * Return true in case any of the specified params is null
     *
     * @param name
     * @return
     */
    public boolean isEmptyParams(String... name) {

        for (String o : name) {
            if (isEmptyParam(o)) {
                return true;
            }
        }
        return false;
    }

    public String getCurrentUrl() {
        StringBuffer fullUrl = request.getRequestURL();
        String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            int endIndex = fullUrl.lastIndexOf(pathInfo);
            return fullUrl.subSequence(0, endIndex) + "/";
        } else {
            return fullUrl.toString() + "/";
        }
    }

    public String getBaseUrl() {
        StringBuffer url = request.getRequestURL();
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        String base = url.substring(0, url.length() - uri.length() + ctx.length()) + "/";
        return base;
    }

    public String getFullURL() {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }

    /**
     * Get an HttpServletRequest instance
     *
     * @return
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * Get an HttpServletResponse instance
     *
     * @return
     */
    public HttpServletResponse getResponse() {
        return response;
    }

    /**
     * [REQUEST] {@link javax.servlet.http.HttpServletRequest#getMethod() }
     *
     * @return
     */
    public String getRequestMethod() {
        return request.getMethod();
    }

    /**
     * [REQUEST] Get parameter from url path, example:
     * http://google.com/adduser/username/password will return an array with
     * username and password
     *
     * @return
     */
    public List<String> getPathParameters() {
        return pathParameters;
    }

    /**
     * [REQUEST] Get parameter from url path, example:
     * http://google.com/adduser/username/password will return an array with
     * username and password
     *
     * @param index
     * @return
     */
    public String getPathParameter(int index) {
        try {
            return pathParameters.get(index);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }

    /**
     * [REQUEST]
     * {@link javax.servlet.http.HttpServletRequest#getParameter(String)}
     *
     * @param name
     * @return
     */
    public String getParameter(String name) {
        return request.getParameter(name);
    }

    /**
     * [REQUEST]
     * {@link javax.servlet.http.HttpServletRequest#getParameter(String)}
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public String getParameter(String name, String defaultValue) {
        String param = request.getParameter(name);
        if (param != null) {
            return param;
        } else {
            return defaultValue;
        }
    }

    /**
     * [REQUEST]
     * {@link javax.servlet.http.HttpServletRequest#getParameter(String)}
     *
     * @param <T>
     * @param name
     * @param defaultValue
     * @param clazz
     * @return
     */
    public <T> T getParameter(String name, Object defaultValue, Class<T> clazz) {
        String param = request.getParameter(name);
        return SFServletContainer.castString(param, defaultValue, clazz);
    }
    
    /**
     * Cast String object to the clazz type, or return defaultValue if could not cast or value is null
     *
     * @param <T>
     * @param value
     * @param defaultValue
     * @param clazz
     * @return
     */
    public static <T> T castString(String value, Object defaultValue, Class<T> clazz) {
        if (value != null) {
            if (String.class.isAssignableFrom(clazz)) {
                return clazz.cast(value);
            }
            if (Integer.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz)) {
                try {
                    return clazz.cast(Integer.valueOf(value));
                } catch (NumberFormatException ex) {
                    return clazz.cast(defaultValue);
                }
            }
            if (Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz)) {
                try {
                    return clazz.cast(Double.valueOf(value));
                } catch (NumberFormatException ex) {
                    return clazz.cast(defaultValue);
                }
            }
            if (Float.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz)) {
                try {
                    return clazz.cast(Float.valueOf(value));
                } catch (NumberFormatException ex) {
                    return clazz.cast(defaultValue);
                }
            }
            if (Long.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz)) {
                try {
                    return clazz.cast(Long.valueOf(value));
                } catch (NumberFormatException ex) {
                    return clazz.cast(defaultValue);
                }
            }
            if (Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz)) {
                return clazz.cast(value.equalsIgnoreCase("true") || value.equals("1"));
            }
            return clazz.cast(value);
        } else {
            return clazz.cast(defaultValue);
        }
    }

    public Part getPart(String name) {
        try {
            return request.getPart(name);
        } catch (IOException | ServletException ex) {
            logger.error("Exception: " + ex.getMessage(), ex);
            return null;
        }
    }

    public Collection<Part> getParts() throws IOException, ServletException {
        return request.getParts();
    }

    /**
     * [REQUEST]
     * {@link javax.servlet.http.HttpServletRequest#getParameter(String)}
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public Integer getParameterInt(String name, Integer defaultValue) {
        return getParameter(name, defaultValue, Integer.class);
    }

    /**
     * [REQUEST]
     * {@link javax.servlet.http.HttpServletRequest#getParameter(String)}
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public Long getParameterLong(String name, Long defaultValue) {
        return getParameter(name, defaultValue, Long.class);
    }

    /**
     * [REQUEST]
     * {@link javax.servlet.http.HttpServletRequest#getParameter(String)}
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public Double getParameterDouble(String name, Double defaultValue) {
        return getParameter(name, defaultValue, Double.class);
    }

    /**
     * [REQUEST]
     * {@link javax.servlet.http.HttpServletRequest#getParameter(String)}
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public Boolean getParameterBoolean(String name, Boolean defaultValue) {
        return getParameter(name, defaultValue, Boolean.class);
    }

    /**
     * [REQUEST]
     * {@link javax.servlet.http.HttpServletRequest#getParameter(String)}
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public Float getParameterFloat(String name, Float defaultValue) {
        return getParameter(name, defaultValue, Float.class);
    }

    /**
     * [REQUEST]
     * {@link javax.servlet.http.HttpServletRequest#getParameter(String)}
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public String getParameterString(String name, String defaultValue) {
        return getParameter(name, defaultValue, String.class);
    }

    /**
     * [REQUEST] {@link javax.servlet.http.HttpServletRequest#getSession()}
     *
     * @return
     */
    public HttpSession getSession() {
        return request.getSession();
    }

    /**
     * [REQUEST]
     * {@link javax.servlet.http.HttpServletRequest#getSession(boolean)}
     *
     * @param create
     * @return
     */
    public HttpSession getSession(boolean create) {
        return request.getSession(create);
    }

    /**
     * [REQUEST]
     * {@link javax.servlet.http.HttpServletRequest#getParameter(String)}
     *
     * @param name
     * @return
     */
    public String getParameterIsensitive(String name) {
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue()[0];
            }
        }
        return null;
    }

    /**
     * [REQUEST]
     * {@link javax.servlet.http.HttpServletRequest#setAttribute(String, Object)}
     *
     * @param name
     * @param o
     */
    public void setAttribute(String name, Object o) {
        request.setAttribute(name, o);
    }

    /**
     * This method will create an variable that can be accessed from
     * javascript<br>
     * If you define the name as "obj" in javascript it will be
     * "jsvar.obj"<br><br>
     *
     * This function already convert the object to json.
     *
     * @param name
     * @param o
     */
    public void setAttributeJS(String name, Object o) {
        jsVarMap.put(name, o);
    }

    private void setAllStoredJS() {
        request.setAttribute("jsvar", new Gson().toJson(jsVarMap));
    }

    /**
     * Get an object in session
     *
     * @param attribute
     * @return
     */
    public Object getSessionAttribute(String attribute) {
        return this.getSession().getAttribute(attribute);
    }

    /**
     * Get an object in session
     *
     * @param <T>
     * @param attribute
     * @param clazz
     * @return
     */
    public <T> T getSessionAttribute(String attribute, Class<T> clazz) {
        return clazz.cast(this.getSessionAttribute(attribute));
    }

    /**
     * Write an object in session
     *
     * @param attribute
     * @param o
     */
    public void setSessionAttribute(String attribute, Object o) {
        this.getSession().setAttribute(attribute, o);
    }

    /**
     * Remove object from session
     *
     * @param attribute
     */
    public void removeSessionAttribute(String attribute) {
        this.getSession().removeAttribute(attribute);
    }

    /**
     * Delete the entire session
     */
    public void invalidateSession() {
        this.getSession().invalidate();
    }

    // ###### PS: This method is commented because it was used as LDAP only, i'll improve it later and add it again ######
//    /**
//     * Check if the user is logged in, case not, redirect to the login page
//     *
//     * @return
//     */
//    public boolean checkLoggedIn() {
//        AcoUserInfo uinfo = this.getSessionAttribute("userinfo", AcoUserInfo.class);
//        if (uinfo == null) {
//            this.setAttribute(loggedInAttribute, false);
//            sendRedirect(loginUrl);
//            return false;
//        } else {
//            this.setAttribute(loggedInAttribute, true);
//            return true;
//        }
//    }
//
//    /**
//     * Check if the user is logged in
//     *
//     * @return
//     */
//    public boolean isLoggedIn() {
//        AcoUserInfo uinfo = this.getSessionAttribute("userinfo", AcoUserInfo.class);
//        if (uinfo == null) {
//            this.setAttribute(loggedInAttribute, false);
//            return false;
//        } else {
//            this.setAttribute(loggedInAttribute, true);
//            return true;
//        }
//    }

    public String getRequestURI() {
        String requestURI = request.getRequestURI();
        String part = "/";
        try {
            part += getBaseUrl().split("/")[3];
        } catch (ArrayIndexOutOfBoundsException ex) { /* ignore */ }

        if (requestURI.startsWith(part)) {
            requestURI = requestURI.substring(part.length());
        }
        return requestURI;
    }

    /**
     * [RequestDispatcher] forward the request to some jsp/jsf page.<br><br>
     * This function will also inject the #{thisURL} attribute to EL.
     *
     * @param name
     */
    public void forward(String name) {

        String[] pages = name.split("/");
        String currentPage = pages.length > 0 ? pages[0] : name;

        request.setAttribute("fullURL", request.getRequestURL());
        request.setAttribute("thisURL", getCurrentUrl());
        request.setAttribute("baseURL", getBaseUrl());
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("requestURI", getRequestURI());

        this.setAllStoredJS();

        try {

            request.getRequestDispatcher(viewPath + name).forward(request, response);

        } catch (ServletException | IOException ex) {

            PrintWriter out = getWriter();

            out.print("Failed to forward request to: " + viewPath + name);
            logger.warn("Failed to forward request to: " + viewPath + name, ex);

        }
    }

    /**
     * [RESPONSE]
     * {@link javax.servlet.http.HttpServletResponse#sendRedirect(String)}
     *
     * @param location
     */
    public void sendRedirect(String location) {
        try {
            String loc = location.startsWith("/") ? location.substring(1) : location;
            response.sendRedirect(getBaseUrl() + loc);
        } catch (IOException ex) {
            getWriter();
            out.print("Failed to redirect request to: " + location);
            logger.warn("Failed to redirect request to: " + location, ex);
        }
    }

    /**
     * [RESPONSE] {@link javax.servlet.http.HttpServletResponse#setStatus(int)}
     *
     * @param sc
     */
    public void setStatus(int sc) {
        response.setStatus(sc);
    }

    /**
     * [RESPONSE] {@link javax.servlet.http.HttpServletResponse#sendError(int)}
     *
     * @param sc Status Code
     * @throws IOException
     */
    public void sendError(int sc) throws IOException {
        response.sendError(sc);
    }

    /**
     * [RESPONSE]
     * {@link javax.servlet.http.HttpServletResponse#sendError(int, String)}
     *
     * @param sc Status Code
     * @param msg
     * @throws IOException
     */
    public void sendError(int sc, String msg) throws IOException {
        response.sendError(sc, msg);
    }

    /**
     * [RESPONSE]
     * {@link javax.servlet.http.HttpServletResponse#setHeader(String, String)}
     *
     * @param name the name of the header
     * @param value the header value If it contains octet string, it should be
     * encoded according to RFC 2047 (http://www.ietf.org/rfc/rfc2047.txt)
     */
    public void setResponseHeader(String name, String value) {
        response.setHeader(name, value);
    }

    public void setContentType(String contentType) {
        response.setHeader("content-type", contentType);
    }

    /**
     * [RESPONSE]
     * {@link javax.servlet.http.HttpServletResponse#addHeader(String, String)}
     *
     * @param name the name of the header
     * @param value the header value If it contains octet string, it should be
     * encoded according to RFC 2047 (http://www.ietf.org/rfc/rfc2047.txt)
     */
    public void addResponseHeader(String name, String value) {
        response.addHeader(name, value);
    }

    /**
     * [REQUEST] {@link javax.servlet.http.HttpServletRequest#getHeader(String)}
     *
     * @param name
     * @return
     */
    public String getRequestHeader(String name) {
        return request.getHeader(name);
    }

    /**
     * [REQUEST]
     * {@link javax.servlet.http.HttpServletRequest#getHeaders(String)}
     *
     * @param name
     * @return
     */
    public Enumeration<String> getRequestHeaders(String name) {
        return request.getHeaders(name);
    }

    /**
     * [RESPONSE]
     * {@link javax.servlet.http.HttpServletResponse#getHeader(String)}
     *
     * @param name
     * @return
     */
    public String getResponseHeader(String name) {
        return response.getHeader(name);
    }

    /**
     * [RESPONSE]
     * {@link javax.servlet.http.HttpServletResponse#getHeaders(String)}
     *
     * @param name
     * @return
     */
    public Collection<String> getResponseHeaders(String name) {
        return response.getHeaders(name);
    }

    /**
     * Return true only if at least one of objects is null
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

    public PrintWriter getWriter() {
        return out;
    }
}
