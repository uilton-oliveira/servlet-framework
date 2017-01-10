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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to navigate over JSON String / File
 *
 * @author Uilton Oliveira - uilton.dev@gmail.com
 */
public class SFJsonHandler {
    private final String json;
    private final Map<String, Object> jsonMap;
    private final Type type = new TypeToken<Map<String, Object>>(){}.getType();

    public SFJsonHandler(String json) {
        this.json = json;
        jsonMap = new Gson().fromJson(json, type);
    }
    
    public SFJsonHandler(Path jsonFile) throws IOException {
        this.json = readFileToString(jsonFile, "UTF-8");
        jsonMap = new Gson().fromJson(json, type);
    }
    
    private String readFileToString(Path filePath, String encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(filePath);
        return new String(encoded, encoding);
    }
        
    /**
     * This method will navigate into multidimensional json
     * Path example: "sdp/response/code"
     *
     * @param returnClass Result will return casted to this class.
     * @param path Keys to search in json.
     * @return Result Object
     */
    public <T> T getCustom(Class<T> returnClass, String path) {
        String[] pathArray = path.split("/");
        List<String> list = new ArrayList(Arrays.asList(pathArray));
        list.removeAll(Arrays.asList("", null));
        
        Map<String, Object> myMap = new HashMap(jsonMap);
        for (String key : list) {
            Object tmp = myMap.get(key);
            if (tmp instanceof Map || tmp instanceof HashMap) {
                myMap = (Map)tmp;
            } else {
                if (returnClass.isAssignableFrom(String.class)) {
                    return returnClass.cast(String.valueOf(tmp));
                } else if (returnClass.isAssignableFrom(Integer.class)) {
                    return returnClass.cast(Integer.valueOf(String.valueOf(tmp)));
                } else if (returnClass.isAssignableFrom(Double.class)) {
                    return returnClass.cast(Double.valueOf(String.valueOf(tmp)));
                } else if (returnClass.isAssignableFrom(Float.class)) {
                    return returnClass.cast(Float.valueOf(String.valueOf(tmp)));
                } else {
                    return returnClass.cast(tmp);
                }
            }
        }
        return returnClass.cast(myMap);
    }
    
    /**
     * This method will navigate into multidimensional json
     * Path example: "sdp/response/code"
     *
     * @param path Keys to search in json.
     * @return Result Object
     */
    public String getString(String path) {
        return getCustom(String.class, path);
    }
    
    /**
     * This method will navigate into multidimensional json
     * Path example: "sdp/response/code"
     *
     * @param path Keys to search in json.
     * @return Result Object
     */
    public int getInt(String path) {
        return getCustom(Integer.class, path);
    }
    
    /**
     * This method will navigate into multidimensional json
     * Path example: "sdp/response/code"
     *
     * @param path Keys to search in json.
     * @return Result Object
     */
    public Boolean getBoolean(String path) {
        return getCustom(Boolean.class, path);
    }
    
    /**
     * This method will navigate into multidimensional json
     * Path example: "sdp/response/code"
     *
     * @param path Keys to search in json.
     * @return Result Object
     */
    public Double getDouble(String path) {
        return getCustom(Double.class, path);
    }
    
    /**
     * This method will navigate into multidimensional json
     * Path example: "sdp/response/code"
     *
     * @param path Keys to search in json.
     * @return Result Object
     */
    public Float getFloat(String path) {
        return getCustom(Float.class, path);
    }
    
    /**
     * This method will navigate into multidimensional json
     * Path example: "sdp/response/code"
     *
     * @param path Keys to search in json.
     * @return Result Object
     */
    public Map getMap(String path) {
        return getCustom(HashMap.class, path);
    }
    
    /**
     * This method will navigate into multidimensional json
     * Path example: "sdp/response/code"
     *
     * @param path Keys to search in json.
     * @return Result Object
     */
    public List getList(String path) {
        return getCustom(ArrayList.class, path);
    }
    
    /**
     * This method will navigate into multidimensional json
     * Path example: "sdp/response/code"
     *
     * @param path Keys to search in json.
     * @return Result Object
     */
    public List<HashMap<String, Object>> getListMap(String path) {
        List<HashMap<String, Object>> list = getCustom(ArrayList.class, path);
        return list;
    }
    
    /**
     * This method will navigate into multidimensional json
     * Path example: "sdp/response/code"
     *
     * @param path Keys to search in json.
     * @return Result Object
     */
    public List<HashMap<String, String>> getListMapString(String path) {
        List<HashMap<String, String>> list = getCustom(ArrayList.class, path);
        return list;
    }
    
    /**
     * This method will navigate into multidimensional json
     * Path example: "sdp/response/code"
     *
     * @param path Keys to search in json.
     * @return Result Object
     */
    public List<HashMap<String, Integer>> getListMapInt(String path) {
        List<HashMap<String, Integer>> list = getCustom(ArrayList.class, path);
        return list;
    }
    
    /**
     * This method will navigate into multidimensional json
     * Path example: "sdp/response/code"
     *
     * @param path Keys to search in json.
     * @return Result Object
     */
    public List<HashMap<String, Boolean>> getListMapBoolean(String path) {
        List<HashMap<String, Boolean>> list = getCustom(ArrayList.class, path);
        return list;
    }
    
    /**
     * This method will navigate into multidimensional json
     * Path example: "sdp/response/code"
     *
     * @param path Keys to search in json.
     * @return Result Object
     */
    public List<HashMap<String, Double>> getListMapDouble(String path) {
        List<HashMap<String, Double>> list = getCustom(ArrayList.class, path);
        return list;
    }
    
    /**
     * This method will navigate into multidimensional json
     * Path example: "sdp/response/code"
     *
     * @param path Keys to search in json.
     * @return Result Object
     */
    public List<HashMap<String, Float>> getListMapFloat(String path) {
        List<HashMap<String, Float>> list = getCustom(ArrayList.class, path);
        return list;
    }
    
    @Override
    public String toString() {
        return this.json;
    }
    
    public Map<String, Object> getJsonMap() {
        return this.jsonMap;
    }
    
    
    
}
