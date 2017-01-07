package br.com.uiltonsites.servletframework.utility;

import br.com.uiltonsites.servletframework.abstracts.SFMyLogger;

import java.util.Calendar;

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
public class SFTimer extends SFMyLogger {

    public SFTimer() {
    }
    
    private long startTime=0;
    private long endTime=0;

    /**
     * Start the timer
     */
    public SFTimer start() {
        startTime = Calendar.getInstance().getTimeInMillis();
        return this;
    }

    /**
     * Finish the timer, call getElapsedTime() to see the result.
     */
    public void end() {
        endTime = Calendar.getInstance().getTimeInMillis();
    }

    /**
     * Get the elapsed time since you called start() in ms, this method automatically
     * call end() if you didn't called it yet.
     *
     * @return elapsed time in long
 */
    public long getElapsedTime() {
        if (endTime == 0 && startTime != 0) {
            end();
        }
        long rTimer = endTime - startTime;
        endTime = 0;
        startTime = 0;
        return rTimer;
    }
    
    /**
     * Get the elapsed time since you called start() in ms, this method automatically
     * call end() if you didn't called it yet.
     *
     * @return elapsed time in String, return example: 100 ms
    */
    public String getElapsedTimeAsString() {
        return getElapsedTime() + " ms";
    }

    public void myWait(int time, String token) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            logger.error(e.getMessage(), e);
        }
    }
	
}
