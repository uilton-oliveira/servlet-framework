/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.uiltonsites.servletframework.interfaces;

import java.util.List;

/**
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
public interface SFFileTransferUtils {
    
    
    public boolean connect(int timeout);
    public boolean connect();
    public void disconnect();
    public List<String> ls(String path);
    public boolean mkdirs(String path);
    public boolean rename(String oldFile, String newFile);
    public boolean remove(String remoteFile);
    public boolean send(String localFile, String remoteFile);
    public boolean download(String localFile, String remoteFile, boolean deleteRemote);
    public boolean download(String localFile, String remoteFile);
    void close() throws Exception;
    
}
