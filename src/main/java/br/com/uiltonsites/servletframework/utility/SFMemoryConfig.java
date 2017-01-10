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
import br.com.uiltonsites.servletframework.domain.SFKVBean;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Uilton Oliveira - uilton.dev@gmail.com
 */
public abstract class SFMemoryConfig extends SFMyLogger {
    private final Map<String, Object> cfg = new HashMap<>();
    
    public boolean containsKey(String key) {
        return cfg.containsKey(key);
    }
    
    public boolean containsValue(Object value) {
        return cfg.containsValue(value);
    }

    public String getString(String key) {
        
        Object ret = cfg.get(key);
        return ret != null ? ret.toString() : null;
    }
    
    public String getString(String key, String defaultValue) {
        Object ret = cfg.getOrDefault(key, defaultValue);
        return ret != null ? ret.toString() : null;
    }
    
    public Boolean getBoolean(String key, Boolean defaultValue) {
        String ret = cfg.getOrDefault(key, String.valueOf(defaultValue)).toString();
        
        if (ret == null) {
            return null;
        }
        
        return ret.equals("1") || ret.equalsIgnoreCase("true");
    }
    
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }
    
    public Integer getInteger(String key) {
        return getInteger(key, null);
    }
    
    public Integer getInteger(String key, Integer defaultValue) {
        try {
            return Integer.parseInt(cfg.getOrDefault(key, String.valueOf(defaultValue)).toString());
        } catch (NumberFormatException ex) {
            logger.error("NumberFormatException", ex);
            return null;
        }
    }
    
    public Long getLong(String key) {
        return getLong(key, null);
    }
    
    public Long getLong(String key, Long defaultValue) {
        try {
            return Long.parseLong(cfg.getOrDefault(key, String.valueOf(defaultValue)).toString());
        } catch (NumberFormatException ex) {
            logger.error("NumberFormatException", ex);
            return null;
        }
    }
    
    public Double getDouble(String key, Double defaultValue) {
        try {
            return Double.parseDouble(cfg.getOrDefault(key, String.valueOf(defaultValue)).toString());
        } catch (NumberFormatException ex) {
            logger.error("NumberFormatException", ex);
            return null;
        }
    }
    
    public Double getDouble(String key) {
        return getDouble(key, null);
    }
    
    public Float getFloat(String key, Float defaultValue) {
        try {
            return Float.parseFloat(cfg.getOrDefault(key, String.valueOf(defaultValue)).toString());
        } catch (NumberFormatException ex) {
            logger.error("NumberFormatException", ex);
            return null;
        }
    }
    
    public Float getFloat(String key) {
        return getFloat(key, null);
    }
    
    public <T> T getObject(String key, Object defaultValue,  Class<T> clazz) {
        
        Object ret = cfg.getOrDefault(key, defaultValue);
        return ret != null ? clazz.cast(ret) : null;
        
    }
    
    public <T> T getObject(String key,  Class<T> clazz) {
        return getObject(key, null, clazz);
    }
    
    public void setConfig(String key, String value) {
        cfg.put(key, value);
    }
    
    public void setConfig(SFKVBean dbConfig) {
        cfg.put(dbConfig.getKey(), dbConfig.getValue());
    }
    
    public void setConfig(Map<String, String> kv) {
        cfg.putAll(kv);
    }

    public Map<String, Object> getConfigMap() {
        return cfg;
    }
    
}
