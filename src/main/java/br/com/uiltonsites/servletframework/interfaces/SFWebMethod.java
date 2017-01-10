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
package br.com.uiltonsites.servletframework.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Uilton Oliveira - uilton.dev@gmail.com
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD) //can use in method only.
public @interface SFWebMethod {
    public String name() default defaultName;
    public String pathVars() default defaultPathVars;
    public String method() default defaultMethod;
    public String contentType() default defaultContentType;
//    public boolean loginRequired() default false;
    
    public static final String defaultPathVars = "#ANY#";
    public static final String defaultName = "METHOD_NAME";
    public static final String defaultMethod = "ALL";
    public static final String defaultContentType = "text/html";
}
