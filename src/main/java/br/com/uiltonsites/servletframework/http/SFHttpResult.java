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

import br.com.uiltonsites.servletframework.utility.SFJsonHandler;
import br.com.uiltonsites.servletframework.utility.SFXmlHandler;
import com.google.gson.Gson;

/**
 * Return the text response from the http request
 *
 * @author Uilton Oliveira <uilton.dev@gmail.com>
 */
public class SFHttpResult {
    public String output = null;
    /**
     * Return the status code from the http request
     */
    public int statusCode = 500;
    public String reasonPhrase = "Unknow errror";

    public <T> T parseJson(Class<T> clazz) {
        return new Gson().fromJson(output, clazz);
    }

    public SFJsonHandler parseJson() {
        if (output != null) {
            return new SFJsonHandler(output);
        } else {
            return null;
        }
    }

    public SFXmlHandler parseXml() {
        if (output != null)  {
            return new SFXmlHandler(output);
        } else {
            return null;
        }
    }

    /**
     * Act the same as httpResult.output
     * @return 
     */
    @Override
    public String toString() {
        return output;
    }
}
