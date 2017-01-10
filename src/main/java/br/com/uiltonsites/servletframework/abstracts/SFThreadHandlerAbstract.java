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

import br.com.uiltonsites.servletframework.interfaces.SFThreadHandlerInterface;
import br.com.uiltonsites.servletframework.utility.SFLogger;
import br.com.uiltonsites.servletframework.utility.SFQueue;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author Uilton Oliveira - uilton.dev@gmail.com
 */
public abstract class SFThreadHandlerAbstract extends SFThread implements SFThreadHandlerInterface {
    
    private final ConcurrentLinkedQueue<SFThread> threads = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<SFThread> threadsToBeFinished = new ConcurrentLinkedQueue<>();

    // Maximum threads that should be kept running
    private int maxThreads = 50;

    // Minimum threads to keep running (in case theres something in the queue)
    private int minThreads = 10;

    // Number of elements necessary in queue to start an new thread (eg. threshold: 100, elements in queue: 300 = 3 threads)
    private int threshold = 100;

    // this class will monitor the number of elements in this queue to start N threads as necessary
    private final SFQueue queue;
    
    
    public SFThreadHandlerAbstract() {
        super();
        this.queue = queueInstance();
    }

    public SFThreadHandlerAbstract(SFLogger logger) {
        super(logger);
        this.queue = queueInstance();
    }

    /**
     * Return an list of active threads
     * @return 
     */
    public ConcurrentLinkedQueue<SFThread> getThreads() {
        return this.threads;
    }
    
    /**
     * Return an list of active threads
     * @return 
     */
    public ConcurrentLinkedQueue<SFThread> getThreadsToBeFinished() {
        return this.threadsToBeFinished;
    }
    
    /**
     * Return the size of queue
     * @return 
     */
    public int getQueueSize() {
        return queue.size();
    }
    
    /**
     * Return the size of threads
     * @return 
     */
    public int getThreadsTotalSize() {
        return threads.size() + threadsToBeFinished.size();
    }

    /**.
     * Calculate the amount of threads required to handle the queue size
     * @return 
     */
    public int neededThreads() {
        int neededThreads = queue.size() / threshold;
        if (neededThreads < minThreads) {
            neededThreads = minThreads;
        }
        if (neededThreads > maxThreads) {
            neededThreads = maxThreads;
        }
        
        return neededThreads;
    }
    
    public void status() {
        logger.debug("Threads required: [" + getThreadsTotalSize() + "/"+neededThreads()+"] / Maximum: "+maxThreads+" | Queue: " + queue.size());
    }
    
    /**
     * Start N new threads
     * @param count 
     */
    public void startThreads(int count) {
        logger.info("Iniciando novas threads: " + count);
        for (int i = 0; i < count; i++) {
            threads.add(newThreadInstance());
        }
    }
    
    /**
     * Finish N active threads
     * @param count 
     */
    public void stopThreads(int count) {
        logger.info("Finalizando threads: " + count);
        for (int i = 0; i < count; i++) {
            SFThread t = threads.poll();
            threadsToBeFinished.add(t);
            t.stop();
        }
    }

    /**
     * Finish this thread and all active sub threads
     */
    @Override
    public void shutdown() {
        shutdownThreads();
        super.shutdown();
    }
    
    /**
     * Finish this thread and all active sub threads
     */
    @Override
    public void stop() {
        shutdownThreads();
        super.stop();
    }
    
    
    /**
     * Finish all active sub threads and wait
     */
    public void shutdownThreads() {
        logger.info("Parando todas threads filhas");
        
        ConcurrentLinkedQueue<SFThread> waitingList = new ConcurrentLinkedQueue();
        SFThread at;

        // Notify all threads to stop
        while ((at = threads.poll()) != null) {
            at.shutdown();
            waitingList.add(at);
        }

        // Notify all threads that was already flagged to stop, to stop as soon as possible
        while ((at = threadsToBeFinished.poll()) != null) {
            at.shutdown();
            waitingList.add(at);
        }

        // Wait all threads to be finished (max wait of 30 seconds each)
        for (SFThread abstractThread : waitingList) {
            try {
                abstractThread.getThread().join(30000);
            } catch (InterruptedException ex) {}
        }
    }
    
    
    /**
     * Start new threads based on the amount needed
     */
    public void checkNeededThreads() {
        
        clearDeadThreads();
        
        int needed = neededThreads();
        
        if (getThreadsTotalSize() < needed) {
            startThreads(needed - threads.size());
        }
        
    }
    
    /**
     * End threads based on the amount needed
     */
    public void checkUnneededThreads() {
        
        int needed = neededThreads();
        
        if (getThreadsTotalSize() > needed) {
            stopThreads(threads.size() - needed);
        }
        
    }
    
    
    private void clearDeadThreads(Iterator<SFThread> i) {
        
        while (i.hasNext()) {
            
            SFThread at = i.next();
            
            if (!at.getThread().isAlive()) {
                i.remove();
            }
            
            if (queue.size() > 0 && at.getThread().getState() == Thread.State.WAITING) {
                at.stop();
                i.remove();
            }
        }
    }
    
    /**
     * Check between all threads if any is dead, and remove from the list
     */
    private void clearDeadThreads() {
        clearDeadThreads(threads.iterator());
        clearDeadThreads(threadsToBeFinished.iterator());
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        if (maxThreads <= 0) {
            logger.warn("Invalid value in maxThreads: " + maxThreads + ", changing to value: 1");
            this.maxThreads = 1;
        } else {
            this.maxThreads = maxThreads;
        }
    }

    public int getMinThreads() {
        return minThreads;
    }

    public void setMinThreads(int minThreads) {
        if (minThreads <= 0) {
            logger.info("Invalid value in minThreads: " + minThreads + ", changing to value: 1");
            minThreads = 1;
        } else {
            this.minThreads = minThreads;
        }
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        if (threshold <= 0) {
            logger.info("Invalid value in threshold: " + threshold + ", changing to value: 1");
            threshold = 1;
        } else {
            this.threshold = threshold;
        }
    }
    
    
    
}
