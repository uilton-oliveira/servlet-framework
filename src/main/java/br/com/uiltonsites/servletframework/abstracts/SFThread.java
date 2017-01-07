/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.uiltonsites.servletframework.abstracts;

import br.com.uiltonsites.servletframework.utility.SFLogger;

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
public abstract class SFThread extends SFMyLogger implements Runnable {

    private Thread thread = null;
    private volatile boolean running = true;
    private volatile boolean shutdown = false;

    public SFThread(SFLogger logger) {
        super(logger);
    }

    public SFThread() {
        super();
    }

    /**
     * Use this method to start an thread in this object. If an threads is already active in this object,
     * nothing will be done.
     *
     * @param name
     * @return
     */
    public SFThread startThread(String name) {
        if (this.thread != null && this.thread.isAlive()) {
            return this;
        }

        Thread t = new Thread(this, name);
        this.thread = t;
        thread.start();
        return this;
    }

    /**
     * Use this method to start an thread in this object. If an threads is already active in this object,
     * nothing will be done.
     *
     * @return
     */
    public SFThread startThread() {
        return startThread("AbstractThread");
    }

    public String getThreadName() {
        return thread.getName();
    }

    /**
     * Sleep that will be interrupted in case of shutdown(), use this instead of Thread.sleep
     *
     * @param ms
     */
    public void sleepSafe(int ms) {
        int total = ms;
        final int part = 100;
        while (total > 0 && !shutdown) {
            int sleepTime = total <= part ? total : part;
            try {
                Thread.sleep(sleepTime);
                total -= part;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                stop();
            }
        }
    }

    /**
     * Sleep that will be interrupted in case of shutdown(), use this instead of Thread.sleep
     *
     * @param ms
     */
    public void sleep(int ms) {
        int total = ms;
        final int part = 100;
        while (total > 0 && running) {
            int sleepTime = total <= part ? total : part;
            try {
                Thread.sleep(sleepTime);
                total -= part;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                stop();
            }
        }
    }

    /**
     * Sleep that will be interrupted in case of shutdown(), use this instead of Thread.sleep
     *
     * @param sec
     */
    public void secSleep(int sec) {
        sleep(sec * 1000);
    }

    /**
     * Sleep that will be interrupted in case of shutdown(), use this instead of Thread.sleep
     *
     * @param sec
     */
    public void secSleepSafe(int sec) {
        sleepSafe(sec * 1000);
    }

    /**
     * Sleep that will be interrupted in case of shutdown(), use this instead of Thread.sleep
     *
     * @param min
     */
    public void minSleep(int min) {
        secSleep(min * 60);
    }

    /**
     * Sleep that will be interrupted in case of shutdown(), use this instead of Thread.sleep
     *
     * @param min
     */
    public void minSleepSafe(int min) {
        secSleepSafe(min * 60);
    }

    /**
     * This method will flag that this thread should be finished
     */
    public void stop() {
        running = false;
    }

    /**
     * This method will flag that this thread should be finished as soon as possible,
     * because the application is about to be finished
     */
    public void shutdown() {
        shutdown = true;
        stop();
    }

    public abstract void loop() throws InterruptedException, IllegalStateException, Exception;

    protected void onThreadShutdown() {
    }

    @Override
    public void run() {
        try {

            while (running) {
                loop();
            }

        } catch (InterruptedException ex) {
        } catch (IllegalStateException ex) {
            try {
                logger.error("Server is shutting down??", ex);
            } catch (Exception ignore) {
                // ignore
            }
        } catch (Exception ex) {
            logger.error("Unhandled Exception [" + ex.getClass().getCanonicalName() + "]: " + ex.getMessage(), ex);
        } finally {
            onThreadShutdown();
            logger.info("Thread finished!");
        }
    }

    /**
     * Return the thread instance created from startThread
     *
     * @return
     */
    public Thread getThread() {
        return thread;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isShutdown() {
        return shutdown;
    }

}
