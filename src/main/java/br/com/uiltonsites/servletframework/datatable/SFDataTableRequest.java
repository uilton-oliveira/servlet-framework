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
package br.com.uiltonsites.servletframework.datatable;

import br.com.uiltonsites.servletframework.abstracts.SFMyLogger;
import br.com.uiltonsites.servletframework.http.SFServletContainer;
import br.com.uiltonsites.servletframework.utility.SFLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Uilton Oliveira - uilton.dev@gmail.com
 */
public class SFDataTableRequest extends SFMyLogger {
    private int limit;
    private int offset;
    private String draw;
    private Map<Integer, String> columnIndexToName = new HashMap<>();
    private Map<Integer, String> allColumnIndexToName = new HashMap<>();
    private List<Order> orders = new ArrayList<>();
    private String search;
    private List<String> searchableFields = new ArrayList<>();
    private SFDataTableConfig config;
    private String fields;
    private String escapedFields;
    private List<String> extraFields;

    public class Order {
        private int column;
        private String dir;

        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
        }

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }
        
    }

    public SFDataTableRequest(SFServletContainer c, SFDataTableConfig dtc, SFLogger logger) {
        super(logger);
        this.config = dtc;
        this.limit = c.getParameterInt("length", -1);
        this.offset = c.getParameterInt("start", -1);
        this.draw = c.getParameterString("draw", "0");
        this.searchableFields = dtc.getSearchableFields();
        
        this.search = c.getParameter("search[value]", "");
        this.fields = dtc.getColumns();
        this.escapedFields = dtc.getEscapedColumns();
        this.extraFields = dtc.getExtraColumns();
        
        String dir;
        int i = 0;
        while ((dir = c.getParameter("order["+i+"][dir]")) != null) {
            Order tmpOrder = new Order();
            tmpOrder.setDir(dir);
            tmpOrder.setColumn(c.getParameterInt("order["+i+"][column]", -1));
            if (tmpOrder.getColumn() != -1) {
                orders.add(tmpOrder);
            }
            i++;
        }
        
        // substituido pelo dtc.getColumnIndexToName() por ser mais seguro contra
        // sql injection
//        String data;
//        i = 0;
//        while ((data = c.getParameter("columns["+i+"][data]")) != null) {
//            this.columnIndexToName.put(i, data);
//            i++;
//        }
        this.columnIndexToName = dtc.getColumnIndexToName();
        this.allColumnIndexToName = dtc.getAllColumnIndexToName();
        
    }

    public SFDataTableConfig getConfig() {
        return config;
    }

    public List<String> getExtraFields() {
        return extraFields;
    }
    
    
    public String getDraw() {
        return draw;
    }
    
    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    /**
     * Map with positions of columns and their names, hidden fields IS NOT considered (without titles)
     * @return 
     */
    public Map<Integer, String> getColumnIndexToName() {
        return columnIndexToName;
    }
    
    /**
     * Map with positions of columns and their names, hidden fields IS considered (without titles)
     * @return 
     */
    public Map<Integer, String> getAllColumnIndexToName() {
        return allColumnIndexToName;
    }

    public List<Order> getOrder() {
        return orders;
    }

    public String getSearch() {
        return search;
    }

    public String getFields() {
        return fields;
    }

    public String getEscapedFields() {
        return escapedFields;
    }
    
    /**
     * @return the searchableFields
     */
    public List<String> getSearchableFields() {
        return searchableFields;
    }

}
