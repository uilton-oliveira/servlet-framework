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

import br.com.uiltonsites.servletframework.interfaces.SFStartupInterface;
import br.com.uiltonsites.servletframework.http.SFHttpUtil;
import br.com.uiltonsites.servletframework.utility.SFLogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * <pre>
 *     Abstract Class that manage what should be started/ended together with glassfish.
 *     This class should be extended only ONE TIME per project.
 * </pre>
 *
 * @author Uilton Oliveira <uilton.dev@gmail.com>
 */
public abstract class SFStartupAbstract extends SFMyLogger implements SFStartupInterface {
    
    private static final List<SFThread> threads = new ArrayList<>();
    private static final List<SFLifeCycle> lifeCycles = new ArrayList<>();
    private static SFLogger staticLogger = null;
    
    
    @PostConstruct
    private void acoStartup() {
        staticLogger = logger;
        
        logger.info("STARTUP CALLED!");
        
        startLifeCycle("http_util", new SFHttpUtil(logger));
        
        startup();

    }
    
    @PreDestroy
    private void acoShutdown() {
        logger.info("SHUTDOWN CALLED!");
        
        shutdown();

        // stop all threads
        stopThreads();

        // call method onDestroy in all LifeCycles started
        stopLifeCycle();
    }
    
    /**
     * Register an object with LifeCycle and call the method onCreate, and will be ended automatically on shutdown
     * @param name
     * @param lifeCycle 
     */
    public void startLifeCycle(String name, SFLifeCycle lifeCycle) {
        if (lifeCycle == null) {
            return;
        }
        logger.info("Starting an object with LifeCycle: " + name);
        lifeCycle.setObjectName(name);
        lifeCycle.onCreate();
        lifeCycles.add(lifeCycle);
    }
    
    /**
     * Register an object with LifeCycle and call the method onCreate, and will be ended automatically on shutdown
     * @param lifeCycle 
     */
    public void startLifeCycle(SFLifeCycle lifeCycle) {
        startLifeCycle(lifeCycle.getClass().getSimpleName(), lifeCycle);
    }
    
    /**
     * Call the method onDestroy when the application is about to be shutdown
     */
    private void stopLifeCycle() {
        Iterator<SFLifeCycle> i = lifeCycles.iterator();
        while (i.hasNext()) {
           SFLifeCycle lifeCycle = i.next();
           
           logger.info("Ending object with LifeCycle: " + lifeCycle.getObjectName());
           
           lifeCycle.onDestroy();
           
           i.remove();
        }
        
        logger.info("All objects with LifeCycle ended");
    }
    
    
    /**
     * Start an thread if it was not started yet, and add it to an list to be finished on application shutdown
     * @param name
     * @param acoThread
     */
    public static void startThread(String name, SFThread acoThread) {
        if (acoThread == null) {
            return;
        }
        if (staticLogger != null) {
            staticLogger.info("Starting Thread: " + name);
        }
        threads.add(acoThread.startThread(name));
    }
    
    /**
     * Start an thread if it was not started yet, and add it to an list to be finished on application shutdown
     * @param acoThread
     */
    public static void startThread(SFThread acoThread) {
        startThread(acoThread.getClass().getName(), acoThread);
    }
    
    /**
     * Send an shutdown signal to all threads started with startThread method
     */
    private void stopThreads() {
        
        // Send signal to stop all threads
        for (SFThread acoThread : threads) {
            logger.info("Parando thread: " + acoThread.getThreadName());
            acoThread.shutdown();
        }
        
        // wait until all threads is finished (timeout 30 secounds per thread) and remove it from the internal list.
        Iterator<SFThread> i = threads.iterator();
        while (i.hasNext()) {
            SFThread acoThread = i.next();
            
            try {
                acoThread.getThread().join(30000);
            } catch (InterruptedException ex) {
                logger.info("Failed to join stopThread: " + ex.getMessage(), ex);
            }
            
            i.remove();
            
        }
        
        logger.info("All threads ended!");
    }
    
}
