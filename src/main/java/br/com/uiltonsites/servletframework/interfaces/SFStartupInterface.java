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

/**
 *
 * @author Uilton Oliveira - uilton.dev@gmail.com
 */
public interface SFStartupInterface {
    /**
     * Add here what should be executed when the container do the project deploy
     */
    public void startup();
    
    /**
     * <pre>
     *     Add here what should be executed when container do the project undeploy
     *
     *     PS: By default, it already end all threads started with startThread() and so objects started with startLifeCycle()
     * </pre>
     */
    public void shutdown();
}
