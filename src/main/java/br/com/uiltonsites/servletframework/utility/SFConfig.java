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
import br.com.uiltonsites.servletframework.utility.exceptions.SFLoadConfigException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.el.PropertyNotFoundException;

/**
 *
 * @author Uilton Oliveira - uilton.dev@gmail.com
 */
public class SFConfig extends SFMyLogger {
   
    private PropData propData = null;
    private static String env = null;
    private static final Object lock = new Object();
    private static Map<String, PropData>  cacheMap = new HashMap<>();
    
    public enum SystemEnv {
        dev, qa, beta, prod
    }
    
    private class PropData {
        public String configName;
        public String configPath;
        public Properties properties = null;
    }
    
    private static final String ENV_DEFAULT = "default";

    public SFConfig(SFLogger logger) {
        super(logger);
    }

    public SFConfig() {
        super();
    }
  
    
    public void loadSystemEnv() {
        if (env == null) {
            String sysVar = System.getenv("SYSTEM_ENV");
            if (sysVar == null || sysVar.isEmpty()) {
                logger.warn("Failed to read Environment variable: SYSTEM_ENV, configuring as DEV!");
                sysVar = "dev";
            }
            env = sysVar.toLowerCase();
            logger.info("SYSTEM_ENV: " + env);
        }
    }
    
    public SystemEnv getSystemEnv() {
        loadSystemEnv();
        
        switch (env) {
            case "dev":
                return SystemEnv.dev;
            case "qa":
                return SystemEnv.qa;
            case "beta":
                return SystemEnv.beta;
            case "prod":
                return SystemEnv.prod;
            default:
                return SystemEnv.dev;
        }
        
    }
    
    /**
     * Same as initConfig, but throw exception if error is found.
     * @param configName 
     * @throws SFLoadConfigException
     */
    public SFConfig loadConfig(String configName) throws SFLoadConfigException {
        loadSystemEnv();
        
        if ((propData = cacheMap.get(configName)) == null) {
            synchronized(lock) {
                String configPath = "/config/"+env+"/"+configName+".properties";

                Properties prop = new Properties();
                InputStream inputStream = SFConfig.class.getClassLoader().getResourceAsStream(configPath);
                if (inputStream == null && !env.equals(SFConfig.ENV_DEFAULT)) {
                    configPath = "/config/default/"+configName+".properties";
                    inputStream = SFConfig.class.getClassLoader().getResourceAsStream(configPath);
                    if (inputStream == null) {
                        String err = "File configuration not found in "+env+" or default: " + configName + ".properties";
                        logger.error(err);
                        throw new SFLoadConfigException(err, new FileNotFoundException());
                    }
                }
                try {
                    prop.load(inputStream);
                    propData = new PropData();
                    propData.configName = configName;
                    propData.configPath = configPath;
                    propData.properties = prop;
                    cacheMap.put(configName, propData);
                } catch (IOException ex) {
                    String err = "Failed to read configuration file: " + configName + ".properties";
                    logger.error(err, ex);
                    throw new SFLoadConfigException(err, ex);
                } catch (NullPointerException ex) { 
                    logger.error("NullPointerException", ex);
                    throw new SFLoadConfigException("NullPointerException", ex);
                }
            }
        }
        return this;
    }
    
    /**
     * Load the config file
     * @param configName
     * @return 
     */
    public boolean initConfig(String configName) {
        try {
            loadConfig(configName);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    
    private static String removeGetOrIs(String name) {
        String tmp = name;
        if (tmp.startsWith("get")) {
            tmp = tmp.substring(3);
        } else if (tmp.startsWith("is")) {
            tmp = tmp.substring(2);
        }
        return tmp;
    }
    
    
    private static String fieldToUnderscore(String str) {
        StringBuilder sb = new StringBuilder();
        str = removeGetOrIs(str);
        for (int i = 0; i < str.length(); i++) {
            char c = i == 0 ? Character.toLowerCase(str.charAt(i)) : str.charAt(i);
            char next = i!=str.length()-1 ? str.charAt(i+1) : 0x0;
            if (!Character.isUpperCase(c)) {
                sb.append(c);
                continue;
            }
            if (Character.isUpperCase(c)) {
                sb.append("_");
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
        
    }
    
    private static String underscoreToField(String str, boolean firstUpper) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = i == 0 ? Character.toUpperCase(str.charAt(i)) : str.charAt(i);
            char next = i!=str.length()-1 ? str.charAt(i+1) : 0x0;
            if (c != '_') {
                sb.append(i == 0 && !firstUpper ? Character.toLowerCase(c) : c );
            }
            if (c == '_' && next != 0x0) {
                sb.append(Character.toUpperCase(next));
                i++;
            }
        }
        return sb.toString();
        
    }
    
    private Method findMethod(Class<?> clazz, String methodName) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)) {
              return method;
            }
        }
        return null;
    } 
    
    private String tryDetectProperty(String methodName) {
        String fieldName = fieldToUnderscore(methodName);
        String prop = propData.properties.getProperty(fieldName, null);
        if (prop == null) {
            fieldName = removeGetOrIs(methodName);
            prop = propData.properties.getProperty(fieldName, null);
            if (prop == null) {
                fieldName = methodName;
                prop = propData.properties.getProperty(fieldName, null);
            }
        }
        return prop;
    }
    
    
    /**
     * This method will try auto detect the properties in the config file
     * and load it automatically into the object.
     * 
     * If the getter in object is getExampleMethod, it will search for:
     * example_method
     *
     * @param cl The class that will be used to populate from config
     * @return Will return an populated object
     */
    public <T extends Object> T load(Class<T> cl) {
        
        if (propData == null || propData.properties == null) {
            logger.error("Config not initialized, please call initConfig() first!");
        }
        
        Object obj = null;
        try {
            obj = cl.newInstance();
        } catch(Exception ex) {
            logger.debug("Failed to innitialize object.", ex);
            return null;
        }
        Enumeration e = propData.properties.propertyNames();
        
        while (e.hasMoreElements()) {
            String name = "";
            try {
                String key = (String) e.nextElement();
                name = "set"+underscoreToField(key, true);

                Method m = findMethod(obj.getClass(), name);
                if (m != null) {
                    String value = propData.properties.getProperty(key);
                    Class type = m.getParameterTypes()[0];
                    if (type.equals(Long.class)) {
                        m.invoke(obj, Long.parseLong(value));
                    } else if (type.equals(Integer.class)) {
                        m.invoke(obj, value == null || value.isEmpty() ? null : Integer.parseInt(value));
                    } else if (type.equals(Double.class)) {
                        m.invoke(obj, value == null || value.isEmpty() ? null : Double.parseDouble(value));
                    } else if (type.equals(Float.class)) {
                        m.invoke(obj, value == null || value.isEmpty() ? null : Float.parseFloat(value));
                    } else if (type.equals(String.class)) {
                        m.invoke(obj, value);
                    } else if (type.equals(Boolean.class)) {
                        m.invoke(obj, value != null && (value.equals("1") || value.toLowerCase().equals("true")));
                    } else {
                        logger.debug("Unknow Parameter Type: " + m.getParameterTypes()[0].toString());
                        m.invoke(obj, value);
                    }       
                }
            } catch (Exception ex) {
                logger.error("Failed to call method: " + name, ex);
            }
        }
        return cl.cast(obj);
    }
    
    public String getConfigPath() {
        return propData.configPath;
    }
    
    public boolean fileExists() {
        return propData != null && propData.properties != null;
    }
    
    private <T> T parseObjectFromString(String s, Class<T> clazz) {
        try {
            return clazz.getConstructor(new Class[] {String.class }).newInstance(s);
        } catch (Exception ex) {
            logger.error("Failed to cast config object to " + clazz.getSimpleName());
        }
        return null;
    }
    
    
    public String getProperty(String key, String defaultValue) {
        if (propData == null || propData.properties == null) {
            logger.error("Config not initialized, please call initConfig() first!");
            return defaultValue;
        }
        
        return propData.properties.getProperty(key, defaultValue);
    }
    
    public <T> T getProperty(String key, String defaultValue, Class<T> clazz) {
        return parseObjectFromString(getProperty(key, defaultValue), clazz);
    }
    
    public String getProperty(String key) throws PropertyNotFoundException{
        if (propData == null || propData.properties == null) {
            logger.error("Config not initialized, please call initConfig() first!");
            throw new PropertyNotFoundException("Config not initialized, please call initConfig() first!");
        }
        
        String value = propData.properties.getProperty(key, null);
        if (value == null) {
            logger.warn("\"" + key + "\" Property not found in file \"" + propData.configPath + "\"");
            throw new PropertyNotFoundException("\"" + key + "\" Property not found in file \"" + propData.configPath + "\"");
        }
        return value;
    }
    
    public <T> T getProperty(String key, Class<T> clazz) throws PropertyNotFoundException {
        return parseObjectFromString(getProperty(key), clazz);
    }
    
}
