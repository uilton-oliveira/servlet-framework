/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.uiltonsites.servletframework.files;

import br.com.uiltonsites.servletframework.interfaces.SFFileReadLine;
import br.com.uiltonsites.servletframework.interfaces.SFFileWriteLine;
import br.com.uiltonsites.servletframework.utility.SFLogger;
import br.com.uiltonsites.servletframework.utility.SFTimer;
import br.com.uiltonsites.servletframework.abstracts.SFMyLogger;
import br.com.uiltonsites.servletframework.utility.exceptions.SFEmptyFileException;
import br.com.uiltonsites.servletframework.utility.exceptions.SFGenericException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.lang3.RandomStringUtils;

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
public class SFFileUtils extends SFMyLogger {

    public SFFileUtils(SFLogger logger) {
        super(logger);
    }

    public SFFileUtils() {
        super();
    }

    /**
     * Write the content in an file, line by line, using the interface FileWriteLine as callback (recommended to big files)
     * @param pathToFile
     * @param fileWriteLine
     * @throws IOException 
     */
    public void writeToFile(String pathToFile, SFFileWriteLine fileWriteLine) throws IOException {
        File file = new File(pathToFile);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            String line;
            int lineCount = 1;
            while ((line = fileWriteLine.writeLine(lineCount == 1, lineCount)) != null) {
                writer.append(line + "\n");
                lineCount++;
            }
        }
    }
    
    /**
     * Get all files from an folder with an sufix
     * @param dirPath
     * @param extension
     * @return 
     */
    public File[] getFileList(String dirPath, String extension) {
        
        File dir  = new File(dirPath);
        return dir.listFiles((File dir1, String name) -> name.endsWith(extension));
        
    }
    
    
    /**
     * Write the content in file and throw an exception in case of error.
     * @param pathToFile
     * @param content
     * @throws IOException
     */
    public void writeToFile(String pathToFile, CharSequence content) throws IOException {
        File file = new File(pathToFile);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.append(content);
        }
    }
    
    /**
     * Write the content in file and write in log in case of error.
     * @param pathToFile
     * @param content
     * @return boolean that indicate if it failed to write or not.
     */
    public boolean tryWriteToFile(String pathToFile, CharSequence content) {
        logger.debug("Writing on: \"" + pathToFile + "\"");
        boolean ret = false;
        try {
            writeToFile(pathToFile, content);
            ret = true;
            logger.debug("Successfully written!");
        } catch (Exception ex) {
            logger.debug("Exception while writing to the file!", ex);
        }
        return ret;
    }
    
    /**
     * Rename file and throw an exception in case of error
     * @param from
     * @param to
     * @throws Exception
     */
    public void rename(String from, String to) throws Exception {
        logger.debug("Moving file from: \"" + from + "\" to: \"" + to + "\"");
        boolean result = false;
        try {
            result = new File(from).renameTo(new File(to));
        } catch (Exception e) {
            throw new Exception("Failed to move \""+from+"\" to \""+to+"\"");
        }
        if (!result) {
            throw new Exception("Failed to move \""+from+"\" to \""+to+"\"");
        }
    }
    
    /**
     * Rename an file and write in log in case of error
     * @param from
     * @param to
     * @return boolean that indicate if it failed to move or not.
     */
    public boolean tryRename(String from, String to) {
        logger.debug("Moving from: \"" + from + "\" to: \"" + to + "\"");
        boolean result;
        try {
            result = new File(from).renameTo(new File(to));
        } catch (Exception e) {
            logger.info("Failed to move \""+from+"\" to \""+to+"\"");
            result = false;
        }
        if (!result) {
            logger.info("Failed to move \""+from+"\" to \""+to+"\"");
        }
        return result;
    }
    
    
//    public List<String> readFile(String filename) throws IOException {
//        List<String> list;
//        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
//            String line;
//            list = new ArrayList<String>();
//            while ((line = br.readLine()) != null) {
//                list.add(line);
//            }
//        }
//        return list;
//    }
    
    /**
     * Read an file and return as callback with an interface FileReadLine, line by line. (recommended to big files)
     * @param file
     * @param fileReadLine
     * @throws InterruptedException 
     */
    // ref: http://stackoverflow.com/a/2293230
    public void readFile(File file, SFFileReadLine fileReadLine) throws InterruptedException, SFEmptyFileException {
        bReadFile(file, fileReadLine);
    }
    
    /**
     * Read an file and return as callback with an interface FileReadLine, line by line. (recommended to big files)
     * @param file
     * @param fileReadLine
     * @return 
     * @throws InterruptedException 
     */
    // ref: http://stackoverflow.com/a/2293230
    public boolean bReadFile(File file, SFFileReadLine fileReadLine) throws InterruptedException, SFEmptyFileException, SFGenericException {
        BufferedReader reader = null;
        SFTimer timer = new SFTimer().start();
        try {
            reader = new BufferedReader(new FileReader(file));
            String next, line = reader.readLine();
            if (line == null) {
                throw new SFEmptyFileException("Empty file: " + file.getName());
            }
            int count = 1;
            for (boolean first = true, last = (line == null); !last; first = false, line = next) {
                
                if (Thread.interrupted()) {
                    throw new InterruptedException("Thread stopped");
                }
                
                last = ((next = reader.readLine()) == null);
                try {
                    if (!fileReadLine.readLine(line, count, first, last)) {
                        logger.info("Read File aborted: " + timer.getElapsedTime() + "ms");
                        return false;
                    }
                } catch (Exception ex) {
                    logger.error("Unhandled Exception: " + ex.getMessage(), ex);
                }
                count++;
            }
            logger.info("Read File finished in: " + timer.getElapsedTime() + "ms");
            return true;
        } catch (FileNotFoundException ex) {
            logger.error("FileNotFoundException: " + file.getAbsolutePath(), ex);
            return false;
        } catch (IOException ex) {
            logger.error("IOException: " + ex.getMessage(), ex);
            return false;
        } finally {
            if (reader != null) {
                try { 
                    reader.close();
                } catch (IOException ex) {
                    logger.error("Failed to close Reader.", ex);
                }
            }
        }
        
        
    }
    
    /**
     * Read an file and return as String (not recommended to big files)
     * @param filePath
     * @param encoding
     * @return
     * @throws IOException 
     */
    public String readFileToString(Path filePath, String encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(filePath);
        return new String(encoded, encoding);
    }
    
    /**
     * Read an file and return as String (not recommended to big files)
     * @param filePath
     * @param encoding
     * @return
     * @throws IOException 
     */
    public String readFileToString(String filePath, String encoding) throws IOException {
        return readFileToString(Paths.get(filePath), encoding);
    }
    
    /**
     * Read an file and return as String (not recommended to big files)
     * Default encoding: UTF-8
     * @param filePath
     * @return
     * @throws IOException 
     */
    public String readFileToString(Path filePath) throws IOException {
        return readFileToString(filePath, "UTF-8");
    }
    
    /**
     * Read an file and return as String (not recommended to big files)
     * Default encoding: UTF-8
     * @param filePath
     * @return
     * @throws IOException 
     */
    public String readFileToString(String filePath) throws IOException {
        return readFileToString(filePath, "UTF-8");
    }
    
    /**
     * Create an directory with all subdirectory
     * @param localPath
     * @return 
     */
    public boolean mkdirs(String localPath) {               
        Path path = Paths.get(localPath);
        if (Files.notExists(path)) {
            path.toFile().mkdirs();
            logger.debug("Directory not found: " + path.toString() + ", creating...");
            if (Files.notExists(path)) {
                logger.error("Failed to create the directory: " + path.toString());
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
    
    public String executeCommand(String command) {
 
        StringBuilder output = new StringBuilder();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = 
                new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;			
            while ((line = reader.readLine())!= null) {
                output.append(line).append("\n");
            }

        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage(), e);
        }

        return output.toString().trim();
 
    }
    
    public String randomFileName(String ext){
        long millis = System.currentTimeMillis();
        String rndchars = RandomStringUtils.randomAlphanumeric(16);
        return rndchars + "_" + millis + ext;
    }
}
