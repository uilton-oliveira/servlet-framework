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
package br.com.uiltonsites.servletframework.abstracts;

import br.com.uiltonsites.servletframework.utility.SFLogger;

/**
 * Abstract class to write/receive an instance of SFLogger.
 *
 * @author Uilton Oliveira - uilton.dev@gmail.com
 */
public abstract class SFMyLogger {
    public SFLogger logger;

    /**
     * Receive an instance of SFLogger to be utilized in the class
     * @param logger 
     */
    public SFMyLogger(SFLogger logger) {
        //this.logger = LoggerToken.getLogger(this.getClass(), logger.token);
        this.logger = logger;
    }
    
    /**
     * Create an new instance of SFLogger to be utilized in the class
     */
    public SFMyLogger() {
        this.logger = SFLogger.getLogger(this.getClass());
    }
    
    public void setLogger(SFLogger logger) {
        this.logger = logger;
    }
    
}
