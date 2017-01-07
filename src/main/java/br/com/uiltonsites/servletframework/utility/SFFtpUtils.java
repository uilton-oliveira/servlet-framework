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

package br.com.uiltonsites.servletframework.utility;

import br.com.uiltonsites.servletframework.abstracts.SFMyLogger;
import br.com.uiltonsites.servletframework.interfaces.SFFileTransferUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;

/**
 * Class used to manage FTP connections
 *
 * @author Uilton Oliveira <uilton.dev@gmail.com>
 */
public class SFFtpUtils extends SFMyLogger implements SFFileTransferUtils, AutoCloseable{

    protected final String username;
    protected final String password;
    protected final String host;
    protected final int port;

    public FTPClient client;
    public int fileType = FTP.BINARY_FILE_TYPE;
    public int timeout = 60000;
    public boolean ssl = false;
    public String encoding = null;
    public boolean printHash = false;
    public boolean listHiddenFiles = false;
    public boolean saveUnparseable = false;
    public String defaultDateFormat = null;
    public String recentDateFormat = null;
    public boolean localActive = false;
    public boolean useEpsvWithIPv4 = false;
    
    /**
     * During file transfers, the data connection is busy, but the control connection is idle. FTP servers know that the control connection is in use, so won't close it through lack of activity, but it's a lot harder for network routers to know that the control and data connections are associated with each other. Some routers may treat the control connection as idle, and disconnect it if the transfer over the data connection takes longer than the allowable idle time for the router. 
     * One solution to this is to send a safe command (i.e. NOOP) over the control connection to reset the router's idle timer
     */
    public int keepAliveTimeout = 300; // sec

    public SFFtpUtils(String host, int port, String username, String password, SFLogger logger) {
        super(logger);
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        printToLog();
    }

    private void printToLog() {
        logger.trace("### "+getClientType()+" Details ###");
        logger.trace("Host: " + host);
        logger.trace("Port: " + port);
        logger.trace("Username: " + username);
        logger.trace("Password: " + password);
        logger.trace("####################");
    }
    
    protected String getClientType() {
        return ssl ? "FTPS" : "FTP";
    }

    protected FTPSClient newFTPSClient() {
        return new FTPSClient();
    }
    
    protected FTPClient newFTPClient() {
        return new FTPClient();
    }
    
    protected CopyStreamListener createListener(){
        return new CopyStreamListener(){
            private long megsTransferred = 0;

            @Override
            public void bytesTransferred(CopyStreamEvent event) {
                bytesTransferred(event.getTotalBytesTransferred(), event.getBytesTransferred(), event.getStreamSize());
            }

            @Override
            public void bytesTransferred(long totalBytesTransferred,
                    int bytesTransferred, long streamSize) {
                
                long megs = totalBytesTransferred / 1000000;
                
                
                if (streamSize == -1) {
                    
                    // filesize is unknow
                    
                    for (long l = megsTransferred; l < megs; l++) {
                        logger.warn(l + "Mb transferred");
                    }

                    
                } else {
                
                    // filesize is know
                    long megsTotal = streamSize / 1000000;

                    long percent = megs / megsTotal * 100;

                    if (percent % 10 == 0) {
                        logger.trace(percent + "%");
                    }
                }
                
                megsTransferred = megs;
            }
        };
    }

    /**
     * Start an ftp connection
     *
     * @param timeout (ms)
     * @param ftps connect using ftps (ftp over ssl) instead of ftp
     * @return
     */
    public boolean connect(int timeout, boolean ftps) {
        try {

            disconnect();

            client = ftps ? newFTPSClient() : newFTPClient();
            
            if (keepAliveTimeout >= 0) {
                client.setControlKeepAliveTimeout(keepAliveTimeout);
            }
            
            if (encoding != null) {
                client.setControlEncoding(encoding);
            }
            
            if (printHash) {
                client.setCopyStreamListener(createListener());
            }
            
            client.setConnectTimeout(timeout);
            
            client.setListHiddenFiles(listHiddenFiles);
            final FTPClientConfig config;
            config = new FTPClientConfig();
            config.setUnparseableEntries(saveUnparseable);
            
            if (defaultDateFormat != null) {
                config.setDefaultDateFormatStr(defaultDateFormat);
            }
            
            if (recentDateFormat != null) {
                config.setRecentDateFormatStr(recentDateFormat);
            }
            
            client.configure(config);
            
            client.connect(host, port);
            
            // After connection attempt, you should check the reply code to verify
            // success.
            int reply = client.getReplyCode();
            
            if (!FTPReply.isPositiveCompletion(reply))
            {
                logger.error(getClientType() + " server refused connection.");
                client.disconnect();
                return false;
            }


            if (!client.login(username, password)) {
                logger.error("Authentication failed on " + getClientType() + " Server");
                client.disconnect();
                return false;
            }

            if (localActive) {
                client.enterLocalActiveMode();
            } else {
                client.enterLocalPassiveMode();
            }
            
            client.setFileType(fileType);
            
            client.setUseEPSVwithIPv4(useEpsvWithIPv4);

            return true;

        } catch (IOException ex) {
            
            if (client != null && client.isConnected())
            {
                try
                {
                    client.disconnect();
                }
                catch (Exception e)
                {
                    // do nothing
                }
            }
            
            logger.error("Failed to connect to ftp", ex);
            return false;
        }
    }
    
    @Override
    public boolean connect(int timeout) {
        return connect(timeout, ssl);
    }
    
    public boolean connect(boolean ssl) {
        return connect(this.timeout, ssl);
    }

    /**
     * start an connection to ftp
     *
     * @return
     */
    public boolean connect() {
        return connect(this.timeout);
    }

    /**
     * disconnect from ftp
     */
    @Override
    public void disconnect() {
        if (client != null) {
            try {
                client.disconnect();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Execute the command "ls" in the specified path to list the files and folders
     *
     * @param path
     * @return
     */
    @Override
    public List<String> ls(String path) {
        
        List<String> list = new ArrayList<>();
        if (client == null || !client.isConnected()) {
            logger.error(getClientType() + " is not connected");
            return list;
        }

        try {
            
            
            FTPFile[] listFiles = client.listFiles(path);
            for (FTPFile listFile : listFiles) {
                if (listFile != null) {
                    list.add(listFile.getName());
                }
            }
            
            return list;

        } catch (IOException ex) {
            logger.error("Failed to execute ls command: " + ex.getMessage(), ex);
            return new ArrayList<>();
        }

    }

    /**
     * Create the remote path and all subpaths
     *
     * @param path
     * @return
     */
    @Override
    public boolean mkdirs(String path) {
        
        if (client == null || !client.isConnected()) {
            logger.error(getClientType() + " is not connected");
            return false;
        }

        String[] folders = StringUtils.split(path, "/");
        for (String folder : folders) {
            if (folder.length() > 0) {
                try {
                    if (!client.changeWorkingDirectory(folder)) {
                        if (!client.makeDirectory(folder)) {
                            logger.error("Failed to create sftp folder: " + folder + " / error=" + client.getReplyString());
                            return false;
                        }

                        if (!client.changeWorkingDirectory(folder)) {
                            logger.error("Unable to change into newly created remote directory '" + folder + "'.  error='" + client.getReplyString() + "'");
                            return false;
                        }
                    }
                } catch (Exception ex) {
                    logger.error("Failed to create sftp folder: " + folder, ex);
                }
            }
        }
        return true;
    }

    /**
     * Rename an file or folder remote
     *
     * @param oldFile
     * @param newFile
     * @return
     */
    @Override
    public boolean rename(String oldFile, String newFile) {
        
        if (client == null || !client.isConnected()) {
            logger.error(getClientType() + " is not connected");
            return false;
        }

        try {
            if (!client.rename(oldFile, newFile)) {
                logger.error("Failed to rename " + getClientType() + " file from: " + oldFile + " / to: " + newFile + " / reason: " + client.getReplyString());
                return false;
            }
            return true;
        } catch (Exception ex) {
            logger.error("Failed to rename " + getClientType() + " file from: " + oldFile + " / to: " + newFile, ex);
            return false;
        }
    }

    /**
     * Remove an remote file
     *
     * @param remoteFile
     * @return
     */
    @Override
    public boolean remove(String remoteFile) {

        if (client == null || !client.isConnected()) {
            logger.error(getClientType() + " is not connected");
            return false;
        }
        
        try {
            if (!client.deleteFile(remoteFile)) {
                logger.error("Failed to remove " + getClientType() + " file: " + remoteFile + " / reason: " + client.getReplyString());
                return false;
            }
            return true;
        } catch (Exception ex) {
            logger.error("Failed to remove " + getClientType() + " file: " + remoteFile, ex);
            return false;
        }
        
    }


    /**
     * Send an local file to ftp
     *
     * @param localFile
     * @param remoteFile
     * @return
     */
    @Override
    public boolean send(String localFile, String remoteFile) {

        if (client == null || !client.isConnected()) {
            logger.error(getClientType() + " is not connected");
            return false;
        }
        
        try {
            
            logger.debug("Sending file, from: \"" + localFile + "\" to: \"" + remoteFile + "\"");
            
            String tmp = remoteFile + ".tmp";
            
            boolean res;
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(localFile))) {
                res = client.storeFile(tmp, bis);
            }
            
            if (!res) {
                logger.error("Failed to send file \"" + localFile + "\" to \"" + remoteFile + "\" with " + getClientType() + ": " + client.getReplyString());
                return false;
            }
            
            if (!client.rename(tmp, remoteFile)) {
                logger.error("File sent but failed to rename from temp name \"" + tmp + "\" to \"" + remoteFile + "\" with " + getClientType() + ": " + client.getReplyString());
                return false;
            }

        } catch (Exception e) {
            logger.error("Failed to send file \"" + localFile + "\" to \"" + remoteFile + "\" with SFTP: " + e.getMessage(), e);
            return false;
        }


        return true;
    }

    /**
     * Download an remote file
     *
     * @param localFile
     * @param remoteFile
     * @param deleteRemote
     * @return
     */
    @Override
    public boolean download(String localFile, String remoteFile, boolean deleteRemote) {

        if (client == null || !client.isConnected()) {
            logger.error(getClientType() + " is not connected");
            return false;
        }
        
        try {
            logger.debug("Downloading file, from: \"" + remoteFile + "\" to: \"" + localFile + "\"");
            
            boolean res;
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(localFile))) {
                res = client.retrieveFile(remoteFile, bos);
            }
            
            if (!res) {
                
                logger.error("Failed to download the file \"" + remoteFile + "\" to \"" + localFile + "\" with "+ getClientType() +": " + client.getReplyString());
                return false;
            }
        
            if (deleteRemote) {
                remove(remoteFile);
            }
            
            return true;

        } catch (Exception e) {
            logger.error("Failed to download the file \"" + remoteFile + "\" to \"" + localFile + "\" with "+ getClientType() +": " + e.getMessage(), e);
            return false;
        } 

    }

    /**
     * Download an remote file
     *
     * @param localFile
     * @param remoteFile
     * @return
     */
    @Override
    public boolean download(String localFile, String remoteFile) {
        return download(localFile, remoteFile, false);
    }

    @Override
    public void close() throws Exception {
        disconnect();
    }
}
