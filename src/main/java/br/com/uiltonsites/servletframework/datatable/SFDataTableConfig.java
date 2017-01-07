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

import br.com.uiltonsites.servletframework.http.SFServletContainer;
import br.com.uiltonsites.servletframework.utility.SFLogger;
import br.com.uiltonsites.servletframework.abstracts.SFMyLogger;
import br.com.uiltonsites.servletframework.utility.exceptions.SFParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Uilton Oliveira <uilton.dev@gmail.com>
 */
public class SFDataTableConfig extends SFMyLogger {

    protected Map<String, Object> tableKV = new LinkedHashMap<>();
    protected Map<String, Object> tableExtraKV = new LinkedHashMap<>();
    protected List<String> searchableFields = new ArrayList<>();

    protected String where;
    protected String queryTable;
    protected String orderBy;

    protected SFDataTableRequest dataTableRequest;
    protected SFDataTableResponse dataTableResponse;

    protected String dbJndi;

    public SFDataTableConfig(SFLogger logger) {
        super(logger);
    }
    
    protected SFDataTableResponse initResponse(SFDataTableRequest dataTableRequest) {
        return new SFDataTableResponse(dataTableRequest, queryTable);
    }
    
    protected SFDataTableRequest initRequest(SFServletContainer acoServletContainer) {
        return new SFDataTableRequest(acoServletContainer, this, logger);
    }

    public SFDataTableResponse createResponse(SFServletContainer acoServletContainer) throws SFParameterException {
        if (queryTable == null) {
            throw new SFParameterException("Missing required config, please use \".setQueryTable(table)\"", logger);
        }
        this.dataTableRequest = initRequest(acoServletContainer);
        this.dataTableResponse = initResponse(dataTableRequest);
        return dataTableResponse;
    }

    public SFDataTableResponse createResponse(SFServletContainer hsc, String dbJndi) throws SFParameterException {
        if (queryTable == null) {
            throw new SFParameterException("Missing required config, please use \".setQueryTable(table)\"", logger);
        }
        this.dbJndi = dbJndi;
        this.dataTableRequest = initRequest(hsc);
        this.dataTableResponse = initResponse(dataTableRequest);
        return dataTableResponse;
    }

    public List<String> getSearchableFields() {
        return searchableFields;
    }

    /**
     * Add fields that will respond to search in datatable
     *
     * @param searchableFields
     * @return
     */
    public SFDataTableConfig setSearchableFields(String... searchableFields) {
        this.searchableFields.addAll(Arrays.asList(searchableFields));
        return this;
    }

    /**
     * Add fields that will respond to search in datatable
     *
     * @param searchableFields
     * @return
     */
    public SFDataTableConfig setSearchableFields(List<String> searchableFields) {
        this.searchableFields = searchableFields;
        return this;
    }

    public boolean containsDatabaseColumn(String name) {
        return tableKV.get(name) != null;
    }

    public boolean containsExtraColumn(String name) {
        return tableExtraKV.get(name) != null;
    }

    /**
     * Use this method to add an column (that exist on database) with an title
     * that will appear in the datatable
     *
     * @param name
     * @param title
     * @return
     */
    public SFDataTableConfig insertDatabaseColumn(String name, String title) {
        tableKV.put(name, title);
        return this;
    }

    /**
     * Use this method to add an additional column on datatable (that does not
     * exist on database), like an Action for example.
     *
     * @param name
     * @param title
     * @return
     */
    public SFDataTableConfig insertExtraColumn(String name, String title) {
        tableExtraKV.put(name, title);
        return this;
    }

    /**
     * Use this method to add an hidden column (that exist on database)
     *
     * @param name
     * @return
     */
    public SFDataTableConfig insertDatabaseColumn(String name) {
        tableKV.put(name, null);
        return this;
    }

    /**
     * Use this method to add an column (that exist on database) and
     * add an datatable property on it.
     *
     * @param name
     * @param properties
     * @return
     */
    public SFDataTableConfig insertDatabaseColumn(String name, Map<String, String> properties) {
        tableKV.put(name, properties);
        return this;
    }

    /**
     * Use this method to add an column (that does not exist on database) and
     * add an datatable property on it.
     *
     * @param name
     * @param properties
     * @return
     */
    public SFDataTableConfig insertExtraColumn(String name, Map<String, String> properties) {
        tableExtraKV.put(name, properties);
        return this;
    }

    /**
     * Map with positions of columns and their names, hidden fields IS NOT considered (without titles)
     * @return 
     */
    public Map<Integer, String> getColumnIndexToName() {
        Map<Integer, String> columnIndexToName = new HashMap<>();
        int index = 0;
        for (Map.Entry<String, Object> entrySet : tableKV.entrySet()) {
            String key = entrySet.getKey();
            Object value = entrySet.getValue();
            if (value != null) {
                columnIndexToName.put(index, key);
                index++;
            }
        }
        return columnIndexToName;
    }

    /**
     * Map with positions of columns and their names, hidden fields IS considered (without titles)
     * @return 
     */
    public Map<Integer, String> getAllColumnIndexToName() {
        Map<Integer, String> columnIndexToName = new HashMap<>();
        int index = 0;
        for (Map.Entry<String, Object> entrySet : tableKV.entrySet()) {
            String key = entrySet.getKey();
            Object value = entrySet.getValue();
            columnIndexToName.put(index, key);
            index++;
        }
        return columnIndexToName;
    }

    public Map<String, Object> getTableKV() {
        return tableKV;
    }

    public Map<String, Object> getAllTablesKV() {
        Map tmp = new LinkedHashMap(tableKV);
        tmp.putAll(tableExtraKV);
        return tmp;
    }

    public String getColumns() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Object> entrySet : tableKV.entrySet()) {
            String key = entrySet.getKey();
            list.add(key);
        }
        return StringUtils.join(list, ",");
    }

    public String getEscapedColumns() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Object> entrySet : tableKV.entrySet()) {
            String key = entrySet.getKey();
            list.add("\"" + key + "\"");
        }
        return StringUtils.join(list, ",");
    }

    public List<String> getExtraColumns() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Object> entrySet : tableExtraKV.entrySet()) {
            String key = entrySet.getKey();
            list.add(key);
        }
        return list;
    }

    /**
     * @return the where
     */
    public String getWhere() {
        return where;
    }

    /**
     * @param where the where to set
     * @return
     */
    public SFDataTableConfig setWhere(String where) {
        this.where = where;
        return this;
    }

    /**
     * @return the orderBy
     */
    public String getOrderBy() {
        return orderBy;
    }

    /**
     * @param orderBy the orderBy to set
     * @return
     */
    public SFDataTableConfig setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public String getQueryTable() {
        return queryTable;
    }

    public SFDataTableConfig setQueryTable(String queryTable) {
        this.queryTable = queryTable;
        return this;
    }

    public SFDataTableRequest getDataTableRequest() {
        return dataTableRequest;
    }

    public SFDataTableResponse getDataTableResponse() {
        return dataTableResponse;
    }

    public String getDbJndi() {
        return dbJndi;
    }

}
