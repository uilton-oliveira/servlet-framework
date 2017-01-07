/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.uiltonsites.servletframework.utility;

import br.com.uiltonsites.servletframework.utility.exceptions.SFQueueFullException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Abstract class and Thread Safe to manage memory queues
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
public abstract class SFQueue<E>{
    protected final ConcurrentLinkedQueue <E> queue = new ConcurrentLinkedQueue<>();
    protected long maxSize = 9000000;
    protected final Object a = new Object();
    protected int size = 0;
    
    public SFQueue() {
        super();
    }
    
    public void add(E obj) throws SFQueueFullException {
        if (obj == null) {
            return;
        }
        synchronized(a) {
            if (size >= maxSize) {
                throw new SFQueueFullException("Full queue: " + size + " / " + maxSize);
            }
            size++;
        }
        queue.add(obj);
    }
    
    /**
     * Try to add object to the queue, if its full, return false
     * @param obj
     * @return 
     */
    public boolean tryAdd(E obj) {
        if (obj == null) {
            return true;
        }
        synchronized(a) {
            if (size >= maxSize) {
                return false;
            }
            size++;
        }
        queue.add(obj);
        return true;
    }
    
    /**
     * Add to the queue, ignoring the maximum queue size
     * @param obj
     */
    public void forceAdd(E obj) {
        if (obj == null) {
            return;
        }
        synchronized(a) {
            size++;
        }
        queue.add(obj);
    }
    
    public boolean offer(E obj) {
        synchronized(a) {
            if (size >= maxSize) {
                return false;
            }
            size++;
        }
        
        queue.offer(obj);        
        return true;
    }
    
    public E poll() {
        synchronized(a) {
            E obj = queue.poll();       
            if (obj != null) {
                size--;
            }
            return obj;
        }
    }
    
    public void clear() {
        queue.clear();
    }
    
    public int size() {
        synchronized(a) {
            return size;
        }
    }
    
    /**
     * Add all to the queue, cosidering the maximum queue size
     * @param c
     * @return 
     */
    public boolean addAll(Collection<? extends E> c) {
        synchronized(a) {
            if (c.size() + size > maxSize) {
                return false;
            }
            queue.addAll(c);
            size += c.size();
            return true;
        }
    }
    
    /**
     * Add all to the queue, ignoring the maximum queue size
     * @param c
     */
    public void forceAddAll(Collection<? extends E> c) {
        synchronized(a) {
            queue.addAll(c);
            size += c.size();
        }
    }
    
    /**
     * Return an copy of the queue used internally, as list
     * @return 
     */
    public List<E> getQueue() {
        return new ArrayList<>(queue);
    }
}
