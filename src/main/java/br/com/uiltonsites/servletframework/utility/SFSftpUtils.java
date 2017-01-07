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
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.apache.commons.lang3.StringUtils;

/**
 * Class used to manage SFTP connections
 *
 * @author Uilton Oliveira <uilton.dev@gmail.com>
 */
public class SFSftpUtils extends SFMyLogger implements SFFileTransferUtils, AutoCloseable {

    protected final String username;
    protected final String password;
    protected final String host;
    protected final int port;
    protected Session session;
    JSch jsch = new JSch();
    
    public int timeout = 60000;
    
    
    public SFSftpUtils(String host, int port, String username, String password, SFLogger logger) {
        super(logger);
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        printToLog();
    }
    
    private void printToLog() {
        logger.trace("### SFTP Details ###");
        logger.trace("Host: " + host);
        logger.trace("Port: " + port);
        logger.trace("Username: " + username);
        logger.trace("Password: " + password);
        logger.trace("####################");
    }
    
    /**
     * Start an sftp connection
     * @param timeout (ms)
     * @return 
     */
    @Override
    public boolean connect(int timeout) {
        disconnect();
        
        boolean ret = true;
        try {
            session = jsch.getSession(username, host, this.port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);
            session.connect(timeout);
            
        } catch (JSchException e) {
            logger.error("Failed to connecto to SFTP: "+e.getMessage(), e);
            ret = false;
        }
        return ret;
    }
    
    /**
     * Start an sftp connection
     * @return 
     */
    @Override
    public boolean connect() {
        return connect(this.timeout);
    }
    
    /**
     * disconnect from sftp
     */
    @Override
    public void disconnect() {
        if (session != null) {
            try {
                session.disconnect();
            } catch (Exception e) {
            }
        }
    }
    
    protected ChannelSftp openSftpChannel(int timeout) {
        ChannelSftp channel;
        try {
            channel = (ChannelSftp)session.openChannel("sftp");
            channel.connect(timeout);
        } catch (JSchException ex) {
            logger.error("Failed to open SFTP Channel: " + ex.getMessage(), ex);
            return null;
        }
        return channel;
    }
    
    protected ChannelSftp openSftpChannel() {
        return openSftpChannel(this.timeout);
    }
    
    
    /**
     * Execute the command "ls" in the specified path to list the files and folders
     * @param path
     * @return 
     */
    @Override
    public List<String> ls(String path) {
        ChannelSftp sftpChannel = openSftpChannel();
        List<String> output = new ArrayList<>();
        
        if (sftpChannel == null) {
            logger.error("Channel is not open");
            return output;
        }
        
        try {
            Vector<ChannelSftp.LsEntry> filelist = sftpChannel.ls(path);
            for (ChannelSftp.LsEntry entry : filelist) {
                output.add(entry.getFilename());
            }
        } catch (SftpException ex) {
            logger.error("Failed to execute ls command: " + ex.getMessage(), ex);
        } finally {
            disconnectQuietly(sftpChannel);
        }
        return output;
    }
    
    /**
     * Create the remote path and all subpaths
     * @param path
     * @return 
     */
    @Override
    public boolean mkdirs(String path) {
        ChannelSftp sftpChannel = openSftpChannel();
        
        if (sftpChannel == null) {
            logger.error("Channel is not open");
            return false;
        }
        
        boolean ret = true;
        try {
            String[] folders = StringUtils.split(path, "/");
            for ( String folder : folders ) {
                if ( folder.length() > 0 ) {
                    try {
                        sftpChannel.cd( folder );
                    }
                    catch ( SftpException e ) {
                        try {
                            logger.debug("Trying to create remote directory: " + folder);
                            sftpChannel.mkdir(folder );
                            sftpChannel.cd( folder );
                        } catch (SftpException ex) {
                            ret = false;
                            logger.error("Failed to create sftp folder: " + folder, ex);
                        }
                    }
                }
            }
        } finally {
            disconnectQuietly(sftpChannel);
        }
        return ret;
    }
    
    /**
     * Rename an file or folder remote
     * @param oldFile
     * @param newFile
     * @return 
     */
    @Override
    public boolean rename(String oldFile, String newFile) {
        boolean ret = true;
        ChannelSftp sftpChannel = openSftpChannel();
        
        if (sftpChannel == null) {
            logger.error("Channel is not open");
            return false;
        }
        
        try {
            sftpChannel.rename(oldFile, newFile);
        } catch (SftpException ex) {
            ret = false;
            logger.error("Failed to rename sftp file from: " + oldFile + " / to: " + newFile, ex);
        } finally {
            disconnectQuietly(sftpChannel);
        }
        return ret;
    }
    
    /**
     * Remove an remote file
     * @param remoteFile
     * @return 
     */
    @Override
    public boolean remove(String remoteFile) {
        ChannelSftp sftpChannel = openSftpChannel();
        
        if (sftpChannel == null) {
            logger.error("Channel is not open");
            return false;
        }
        
        boolean ret = true;
        try {
            sftpChannel.rm(remoteFile);
        } catch (SftpException ex) {
            ret = false;
            logger.error("Failed to remove sftp file: " + remoteFile, ex);
        } finally {
            disconnectQuietly(sftpChannel);
        }
        return ret;
    }
    
    /**
     * Send an local file to sftp
     * @param localFile
     * @param remoteFile
     * @return 
     */
    @Override
    public boolean send(String localFile, String remoteFile) {
        return send(localFile, remoteFile, ChannelSftp.OVERWRITE, null);
    }
    
    /**
     * Send an local file to sftp
     * @param localFile
     * @param remoteFile
     * @return 
     */
    public boolean send(String localFile, String remoteFile, int mode) {
        return send(localFile, remoteFile, mode, null);
    }
    
    /**
     * Send an local file to sftp
     * @param localFile
     * @param remoteFile
     * @return 
     */
    public boolean send(String localFile, String remoteFile, int mode, SftpProgressMonitor monitor) {
        boolean ret = true;
        ChannelSftp sftpChannel = openSftpChannel();
        
        if (sftpChannel == null) {
            logger.error("Channel is not open");
            return false;
        }
        
        try {
            
            logger.debug("Sending file, from: \"" + localFile + "\" to: \"" + remoteFile + "\"");
            
            String tmp = remoteFile+".tmp";
            
            if (monitor != null) {
                sftpChannel.put(localFile, tmp, monitor, mode);
            } else {
                sftpChannel.put(localFile, tmp, mode);
            }
            
            sftpChannel.rename(tmp, remoteFile);
        } catch (SftpException e) {
            ret = false;
            logger.error("Failed to send file \""+localFile+"\" to \""+remoteFile+"\" with SFTP: "+e.getMessage(), e);
        } finally {
            disconnectQuietly(sftpChannel);
        }
        
        return ret;
    }
    
    /**
     * Download an remote file
     * @param localFile
     * @param remoteFile
     * @param deleteRemote
     * @return 
     */
    @Override
    public boolean download(String localFile, String remoteFile, boolean deleteRemote) {
        
        boolean ret = true;
        ChannelSftp sftpChannel = openSftpChannel();
        
        if (sftpChannel == null) {
            logger.error("Channel is not open");
            return false;
        }
        
        try {
            logger.debug("Downloading file, from: \"" + remoteFile + "\" to: \"" + localFile + "\"");
            sftpChannel.get(remoteFile, localFile);
            if (deleteRemote) {
                remove(remoteFile);
            }

        } catch (SftpException e) {
            ret = false;
            logger.error("Failed to download the file \""+remoteFile+"\" to \""+localFile+"\" with SFTP: "+e.getMessage(), e);
        } finally {
            disconnectQuietly(sftpChannel);
        }
        
        return ret;
    }

    /**
     * Send an local file to sftp
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
    
    
    
    protected static void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }
    
    protected static void disconnectQuietly(ChannelSftp closeable) {
        if (closeable == null) {
            return;
        }
        closeable.disconnect();
    }
}