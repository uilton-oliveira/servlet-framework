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
import br.com.uiltonsites.servletframework.interfaces.SFMemoryMappedReader;
import br.com.uiltonsites.servletframework.interfaces.SFMemoryMappedSerializable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.BufferOverflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Abstract class to write in an file thought an MemoryMappedFile (good performance)
 *
 * @author Uilton Oliveira - uilton.dev@gmail.com
 */
public abstract class SFMemoryMappedAbstract extends SFMyLogger {
    protected MappedByteBuffer mappedByteBuffer;
    protected int fileSize = 104857600; // 100 mb
    public final Object lock = new Object();
    private static final byte EOF = 0;
    protected char delimiter = '\n';
    private long linesCount = 0;
//    protected String charset = "UTF-8";
    protected String charset = "ISO-8859-1";

    public SFMemoryMappedAbstract(SFLogger logger) {
        super(logger);
    }

    public SFMemoryMappedAbstract() {
        super();
    }
    
    
    public void openFile(File file) {
        file.getParentFile().mkdirs();
        try {
            
            FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel();
            mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
            
        } catch (FileNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    
    public void clear() {
        synchronized(lock) {
            
            mappedByteBuffer.rewind();
            
            for (int i = 0; i < 10; i++) {
                mappedByteBuffer.put(EOF);
            }
            
            mappedByteBuffer.rewind();
            linesCount = 0;
            
            //logger.info("Limpando MemoryMappedFile");
        }
    }
    
    public void force() {
        synchronized(lock) {
            mappedByteBuffer.force();
        }
    }
    
    public String getPositionString() {
        synchronized(lock) {
            return String.valueOf(mappedByteBuffer.position());
        }
    }
    
    public int getPosition() {
        synchronized(lock) {
            return mappedByteBuffer.position();
        }
    }
    
    public long getLineCount() {
        synchronized(lock) {
            return linesCount;
        }
    }
    
    public void write(String str) throws BufferOverflowException {
        synchronized(lock) {
            
            try {
                
                int position = mappedByteBuffer.position();
                if (position > 0) {
                    mappedByteBuffer.position(position-1);
                }
                
                mappedByteBuffer.put(str.getBytes(charset));
                mappedByteBuffer.put(EOF);
                
            } catch (BufferOverflowException ex) {
                
                while(mappedByteBuffer.hasRemaining()) {
                    mappedByteBuffer.put(EOF);
                }
                throw ex;
            } catch (UnsupportedEncodingException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }
    
    public void writeLine(SFMemoryMappedSerializable ss) throws BufferOverflowException {
        synchronized(lock) {
            
            try {
                
                int position = mappedByteBuffer.position();
                if (position > 0) {
                    mappedByteBuffer.position(position-1);
                }
                
                String str = ss.serialize() + delimiter;
                
                mappedByteBuffer.put(str.getBytes(charset));
                mappedByteBuffer.put(EOF);
                linesCount++;
                
            } catch (BufferOverflowException ex) {
                while(mappedByteBuffer.hasRemaining()) {
                    mappedByteBuffer.put(EOF);
                }
                throw ex;
            } catch (UnsupportedEncodingException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }
    
    public void writeLine(String str) throws BufferOverflowException {
        synchronized(lock) {
            str += delimiter;
            write(str);
            linesCount++;
        }
    }
    
    public void read(SFMemoryMappedReader handler) {
        synchronized(lock) {
            
            int oldPosition = mappedByteBuffer.position();
            mappedByteBuffer.rewind();
            StringBuilder sb = new StringBuilder();
            
            while (mappedByteBuffer.hasRemaining()) {
                
                byte thisByte = mappedByteBuffer.get();
                char thisChar = (char)thisByte;
                
                if (thisChar != delimiter) {
                    
                    if (thisByte == EOF) {
                        break;
                    }
                    sb.append(thisChar);
                    
                } else {
                    
                    if (!handler.readLine(sb.toString())) {
                        break;
                    } else {
                        sb = new StringBuilder();
                    }
                    
                }
            }
            
            mappedByteBuffer.position(oldPosition);
        }
    }
    
}
