/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.uiltonsites.servletframework.datatable;

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
public class SFOracleDataTableResponse extends SFDataTableResponse {
    
    public SFOracleDataTableResponse(SFDataTableRequest dt, String selectFromTable) {
        super(dt, selectFromTable);
    }

    @Override
    public String allSelect() {
        
        String orderBy = "";
        
        // order
        if (dt.getOrder().size() >= 1 && dt.getColumnIndexToName().size() >= 1) {
            orderBy += " ORDER BY ";
            boolean first = true;
            for (SFDataTableRequest.Order order : dt.getOrder()) {

                if (order.getColumn() > dt.getColumnIndexToName().size()) {
                    logger.debug("Failed to detect column name received from DataTable Javascript");
                    break;
                }

                if (!first) {
                    orderBy += ", ";
                } else {
                    first = false;
                }

                orderBy += dt.getColumnIndexToName().get(order.getColumn()) + " " + order.getDir();
            }
            orderBy += " ";
        } else if (config.getOrderBy() != null && !config.getOrderBy().isEmpty()) {
            orderBy += " ORDER BY " + config.getOrderBy() + " ";
        } else {
            orderBy += " ORDER BY 1";
        }
        
        
        String query = "SELECT " + ((dt.getEscapedFields() != null) ? dt.getEscapedFields() : "*") + ", row_number() over ("+orderBy+") rownumber FROM " + selectFromTable;

        // Search
        boolean whereSet = false;
        if (dt.getSearch() != null && !dt.getSearch().isEmpty()) {
            if (dt.getSearchableFields().size() >= 1) {
                boolean first = true;
                for (String field : dt.getSearchableFields()) {
                    query += first ? " WHERE " : " OR ";
                    first = false;
                    whereSet = true;
//                    query += field + " ILIKE '%" + dt.getSearch() + "%'";
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
            whereSet = true;
        }
        
        // limit / offset
        if (dt.getLimit() >= 0 && dt.getOffset() >= 0) {
            
            query = "SELECT * FROM (" + query + ") WHERE rownumber >= " + dt.getOffset() + " AND rownumber <= " + (dt.getOffset() + dt.getLimit());
        } 

        

        

        return query;
    }
    
    
}
