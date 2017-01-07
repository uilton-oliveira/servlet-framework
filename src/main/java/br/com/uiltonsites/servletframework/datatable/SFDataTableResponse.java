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

import br.com.uiltonsites.servletframework.utility.exceptions.SFLoadConfigException;
import br.com.uiltonsites.servletframework.utility.SFLogger;
import br.com.uiltonsites.servletframework.utility.SFQueryHelper;
import br.com.uiltonsites.servletframework.utility.exceptions.SFQueryHelperException;
import com.google.gson.Gson;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Uilton Oliveira <uilton.dev@gmail.com>
 */
public class SFDataTableResponse {

    public interface ParseData {

        String parse(long rowId, String key, String value);
    }

    protected final String draw;
    protected String recordsTotal;
    protected String recordsFiltered;
    protected final List<Map<String, Object>> data = new ArrayList<>();
    protected HashMap<String, Object> dataLine;
    protected final SFDataTableConfig config;
    protected final String selectFromTable;
    protected final SFDataTableRequest dt;

    protected final SFLogger logger;

    public SFDataTableResponse(SFDataTableRequest dt, String selectFromTable) {
        this.logger = dt.logger;
        this.dt = dt;
        this.selectFromTable = selectFromTable;
        this.draw = dt.getDraw();
        this.config = dt.getConfig();
    }

    public void insertData(int rowId, Map<String, Object> data) {

        this.data.add(data);
    }

    public void newLine(long rowId) {
        dataLine = new HashMap<>();

        HashMap<String, Object> rowData = new HashMap<>();
        rowData.put("pkey", String.valueOf(rowId));

        dataLine.put("DT_RowId", "row_" + rowId);
        dataLine.put("DT_RowData", rowData);
    }

    public void put(String key, Object value) {
        dataLine.put(key, value);
    }

    public void endLine() {
        data.add(dataLine);
        dataLine = null;
    }

    public String output() {
        return output(null);
    }

    public String output(ParseData pr) {
        ResultSet rs = allResultSet();

        this.recordsTotal = String.valueOf(countResultSet(false));
        this.recordsFiltered = String.valueOf(countResultSet(true));
        if (rs != null) {
            try {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                rs.beforeFirst();
                while (rs.next()) {
                    long rowId = rs.getLong(dt.getAllColumnIndexToName().get(0));
                    newLine(rowId);

                    for (int column = 1; column <= colCount; column++) {
                        String name = meta.getColumnName(column);
                        if (config.containsDatabaseColumn(name)) {
                            String value = String.valueOf(rs.getObject(column));
                            put(name, pr != null ? pr.parse(rowId, name, value) : value);
                        }
                    }

                    List<String> extra = dt.getExtraFields();
                    for (String extraField : extra) {
                        put(extraField, pr != null ? pr.parse(rowId, extraField, "") : "");
                    }

                    endLine();
                }
            } catch (SQLException ex) {
                logger.error(ex);
            }
        }

        return new Gson().toJson(this);
    }
    
    public String countSelect() {
        return countSelect(false);
    }

    public String countSelect(boolean includeSearch) {
        boolean whereSet = false;
        String query = "SELECT count(0) as total FROM " + selectFromTable;
        if (config.getWhere() != null && !config.getWhere().isEmpty()) {
            query += " WHERE " + config.getWhere() + " ";
            whereSet = true;
        }

        // Search
        if (includeSearch && dt.getSearch() != null && !dt.getSearch().isEmpty()) {
            if (dt.getSearchableFields().size() >= 1) {
                boolean first = true;
                for (String field : dt.getSearchableFields()) {
                    query += (first || !whereSet) ? " WHERE " : " OR ";
                    first = false;
                    whereSet = true;
                    query += "lower(" +field + ") LIKE lower('%" + dt.getSearch() + "%')";
                }
            }
        }

        return query;
    }

    public String allSelect() {
        String query = "SELECT " + ((dt.getEscapedFields() != null) ? dt.getEscapedFields() : "*") + " FROM " + selectFromTable;

        // Search
        boolean whereSet = false;
        if (dt.getSearch() != null && !dt.getSearch().isEmpty()) {
            if (dt.getSearchableFields().size() >= 1) {
                boolean first = true;
                for (String field : dt.getSearchableFields()) {
                    query += first ? " WHERE " : " OR ";
                    first = false;
                    whereSet = true;
                    query += "lower(" +field + ") LIKE lower('%" + dt.getSearch() + "%')";
                }
            }
        }

        // Custom Where
        if (config.getWhere() != null && !config.getWhere().isEmpty()) {
            if (whereSet) {
                query += " AND " + config.getWhere() + " ";
            } else {
                query += " WHERE " + config.getWhere() + " ";
            }
        }

        // order
        if (dt.getOrder().size() >= 1 && dt.getColumnIndexToName().size() >= 1) {
            query += " ORDER BY ";
            boolean first = true;
            for (SFDataTableRequest.Order order : dt.getOrder()) {

                if (order.getColumn() > dt.getColumnIndexToName().size()) {
                    logger.debug("Failed to detect column name received from DataTable Javascript");
                    break;
                }

                if (!first) {
                    query += ", ";
                } else {
                    first = false;
                }

                query += dt.getColumnIndexToName().get(order.getColumn()) + " " + order.getDir();
            }
            query += " ";
        } else if (config.getOrderBy() != null && !config.getOrderBy().isEmpty()) {
            query += " ORDER BY " + config.getOrderBy() + " ";
        }

        // limit / offset
        if (dt.getLimit() >= 0 && dt.getOffset() >= 0) {
            query += " LIMIT " + dt.getLimit() + " OFFSET " + dt.getOffset();
        }

        return query;
    }

    public SFQueryHelper newQueryHelper() {
        if (config.getDbJndi() != null) {
            return new SFQueryHelper(config.getDbJndi(), logger);
        } else {
            return new SFQueryHelper(logger);
        }
    }
    
    public Long countResultSet() {
        return countResultSet(false);
    }

    public Long countResultSet(boolean includeSearch) {

        Long ret;

        try {
            SFQueryHelper helper = newQueryHelper();
            ret = helper.queryLong(countSelect(includeSearch));
        } catch (SFLoadConfigException | SFQueryHelperException ex) {
            ret = 0L;
            logger.error(ex);
        }

        return ret;
    }

    public ResultSet allResultSet() {

        ResultSet ret;

        try {
            SFQueryHelper helper = newQueryHelper();
            ret = helper.executeRows(allSelect());
            if (!ret.first()) {
                return null;
            }
            ret.beforeFirst();

        } catch (SFLoadConfigException | SFQueryHelperException | SQLException ex) {
            ret = null;
            logger.error(ex);
        }

        return ret;
    }

    public List<Map<String, Object>> getData() {
        return this.data;
    }

    public String getDraw() {
        return draw;
    }

    public String getRecordsTotal() {
        return recordsTotal;
    }

    public String setRecordsTotal(String recordsTotal) {
        return (this.recordsTotal = recordsTotal);
    }

    public String getRecordsFiltered() {
        return recordsFiltered;
    }

    public String setRecordsFiltered(String recordsFiltered) {
        return (this.recordsFiltered = recordsFiltered);
    }

}
