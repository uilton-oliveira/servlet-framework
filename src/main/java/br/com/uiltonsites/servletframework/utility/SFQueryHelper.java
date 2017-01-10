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
import br.com.uiltonsites.servletframework.interfaces.SFQueryRow;
import br.com.uiltonsites.servletframework.utility.exceptions.SFQueryHelperException;
import br.com.uiltonsites.servletframework.utility.exceptions.SFLoadConfigException;
import java.math.BigDecimal;
import java.sql.*;
import javax.sql.*;
import javax.sql.rowset.CachedRowSet;
import javax.naming.*;
import java.net.URL;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.rowset.RowSetProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;

/**
 * Class that will manager connections to the database, using an connection pool
 * from the server, in case you do not set the jndi name, it will use the
 * file database.properties / jndi_name
 *
 * @author Uilton Oliveira - uilton.dev@gmail.com
 */
public class SFQueryHelper extends SFMyLogger {

    private String jndiName = null;
    private static Map<String, DataSource> dataSource = new HashMap<>();
    protected Pattern pattern = Pattern.compile("^.*(?=(?:SELECT)|(?:UPDATE)|(?:DELETE))");
    Matcher matcher;
    private static final Gson gsonpp = new GsonBuilder().setPrettyPrinting().create();

    protected StringBuilder insertBatch;
    protected String insertBatchBase;

    protected Connection globalConnection = null;

    protected Level logLevelQuery = Level.DEBUG;
    protected Level logLevelResult = Level.TRACE;

    public int resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
    public int resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;

    /**
     * Create new instance using the jndi_name configured in database.properties
     *
     * @param logger
     */
    public SFQueryHelper(SFLogger logger) {
        super(logger);
    }

    /**
     * Create new instance using custom jndi_name
     *
     * @param jndiName
     */
    public SFQueryHelper(String jndiName) {
        super();
        this.jndiName = jndiName;
    }

    /**
     * Create new instance using custom jndi_name
     *
     * @param jndiName
     * @param logger
     */
    public SFQueryHelper(String jndiName, SFLogger logger) {
        super(logger);
        this.jndiName = jndiName;
    }

    /**
     * Create new instance using the jndi_name configured in database.properties / jndi_name
     */
    public SFQueryHelper() {
        super();
    }

    public DataSource getDataSource() throws SFLoadConfigException {

        if (jndiName == null) {
            SFConfig cfg = new SFConfig(logger);
            cfg.loadConfig("database");
            jndiName = cfg.getProperty("jndi_name");
        }
        if (!dataSource.containsKey(jndiName)) {
            try {
                Context c = new InitialContext();
                dataSource.put(jndiName, (DataSource) c.lookup(jndiName));
            } catch (NamingException ex) {
                logger.error(ex);
                throw new SFLoadConfigException("Failed to find the jndi_name: \"" + jndiName + "\" on the server.", ex);
            }
        }
        return dataSource.get(jndiName);
    }

    public Connection getConnection() throws SFLoadConfigException, SQLException {
        if (globalConnection == null) {
            return getConnection(0);
        } else {
            return globalConnection;
        }
    }

    public Connection getConnection(int errCount) throws SFLoadConfigException, SQLException {
        DataSource ds = getDataSource();
        if (ds == null) {
            logger.error("Failed to get DataSource!");
            return null;
        }
        Connection cn;
        try {
            cn = ds.getConnection();
            if (cn == null) {
                logger.error("Failed to get Connection!");
                return null;
            }
        } catch (SQLException ex) {
            if (errCount == 0) {
                dataSource.remove(jndiName);
                return getConnection(++errCount);
            } else {
                throw ex;
            }
        }
        return cn;
    }

    protected CachedRowSet newCachedRowSet(ResultSet rs) throws SQLException {

        // used to avoid warning about proprietary api
        //CachedRowSet rowset = (CachedRowSet)Class.forName("com.​sun.​rowset.CachedRowSetImpl").newInstance();
        try {
            //CachedRowSet rowset = new CachedRowSetImpl();
            CachedRowSet rowset = RowSetProvider.newFactory().createCachedRowSet();
            rowset.populate(rs);
            return rowset;
        } catch (Exception ex) {
            logger.error("newCachedRowSet Unhandled Exception " + ex.getMessage(), ex);
            throw ex;
        }
    }

    public Level getLogLevelQuery() {
        return logLevelQuery;
    }

    public void setLogLevelQuery(Level logLevelQuery) {
        this.logLevelQuery = logLevelQuery;
    }

    public Level getLogLevelResult() {
        return logLevelResult;
    }

    public void setLogLevelResult(Level logLevelResult) {
        this.logLevelResult = logLevelResult;
    }

    protected String escapeString(String in) {
        StringBuilder out = new StringBuilder();
        for (int i = 0, j = in.length(); i < j; i++) {
            char c = in.charAt(i);
            if (c == '\'') {
                out.append(c);
            }
            out.append(c);
        }
        return out.toString();
    }

    /**
     * from: https://stackoverflow.com/a/24734294
     *
     * @param parameter
     * @return
     */
    protected String formatParameter(Object parameter) {
        if (parameter == null) {
            return "NULL";
        } else {
            if (parameter instanceof String) {
                return "'" + escapeString((String) parameter) + "'";
            } else if (parameter instanceof Timestamp) {
                return "to_timestamp('" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS").
                        format(parameter) + "', 'mm/dd/yyyy hh24:mi:ss.ff3')";
            } else if (parameter instanceof Date) {
                return "to_date('" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").
                        format(parameter) + "', 'mm/dd/yyyy hh24:mi:ss')";
            } else if (parameter instanceof Boolean) {
                return ((Boolean) parameter) ? "1" : "0";
            } else if (parameter instanceof Object[]) {

                StringBuilder result = new StringBuilder("ARRAY[");
                for (Object obj : (Object[]) parameter) {

                    result.append(formatParameter(obj)).append(",");

                }
                if (result.toString().endsWith(",")) {
                    result.setLength(result.length() - 1);
                }
                result.append("]");

                return result.toString();

            } else {
                return parameter.toString();
            }
        }
    }

    /**
     * from: https://stackoverflow.com/a/24734294
     */
    protected String generateActualSql(String sqlQuery, Object... parameters) {
        String[] parts = sqlQuery.split("\\?");
        StringBuilder sb = new StringBuilder();

        // This might be wrong if some '?' are used as litteral '?'
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            sb.append(part);
            if (i < parameters.length) {
                sb.append(formatParameter(parameters[i]));
            }
        }

        return sb.toString().replaceAll("\\s+", " ");
    }

    protected PreparedStatement setStatement(PreparedStatement stmt, Connection con,  String sql, Object... parameters) throws SFLoadConfigException, SQLException, SFQueryHelperException {
        int numParameters = StringUtils.countMatches(sql, "?");
        if (parameters.length != numParameters) {
            throw new SFQueryHelperException("Numero de parametros na query incorretos, detectado " + numParameters + " '?' e foi passado " + parameters.length + " parametro(s)");
        }
        int i = 1;
        for (Object param : parameters) {
            if (param == null) {
                stmt.setNull(i, java.sql.Types.NULL);
            } else if (param instanceof String) {
                stmt.setString(i, (String) param);
            } else if (param instanceof Integer) {
                stmt.setInt(i, (Integer) param);
            } else if (param instanceof Float) {
                stmt.setFloat(i, (Float) param);
            } else if (param instanceof Double) {
                stmt.setDouble(i, (Double) param);
            } else if (param instanceof Byte) {
                stmt.setByte(i, (Byte) param);
            } else if (param instanceof byte[]) {
                stmt.setBytes(i, (byte[]) param);
            } else if (param instanceof Date) {
                stmt.setDate(i, (Date) param);
            } else if (param instanceof Long) {
                stmt.setLong(i, (Long) param);
            } else if (param instanceof Boolean) {
                stmt.setBoolean(i, (Boolean) param);
            } else if (param instanceof Short) {
                stmt.setShort(i, (Short) param);
            } else if (param instanceof Time) {
                stmt.setTime(i, (Time) param);
            } else if (param instanceof Timestamp) {
                stmt.setTimestamp(i, (Timestamp) param);
            } else if (param instanceof URL) {
                stmt.setURL(i, (URL) param);
            } else if (param instanceof Long[]) {
                Array array = con.createArrayOf("int8", (Object[])param);
                stmt.setArray(i, array);
            } else if (param instanceof Integer[]) {
                Array array = con.createArrayOf("int4", (Object[])param);
                stmt.setArray(i, array);
            } else if (param instanceof Short[]) {
                Array array = con.createArrayOf("int2", (Object[])param);
                stmt.setArray(i, array);
            } else if (param instanceof String[]) {
                Array array = con.createArrayOf("varchar", (Object[])param);
                stmt.setArray(i, array);
            } else if (param instanceof Float[]) {
                Array array = con.createArrayOf("float4", (Object[])param);
                stmt.setArray(i, array);
            } else if (param instanceof Double[]) {
                Array array = con.createArrayOf("float8", (Object[])param);
                stmt.setArray(i, array);
            } else if (param instanceof Date[]) {
                Array array = con.createArrayOf("date", (Object[])param);
                stmt.setArray(i, array);
            } else if (param instanceof Timestamp[]) {
                Array array = con.createArrayOf("timestamp", (Object[])param);
                stmt.setArray(i, array);
            } else if (param instanceof Time[]) {
                Array array = con.createArrayOf("time", (Object[])param);
                stmt.setArray(i, array);
            } else if (param instanceof Byte[]) {
                Array array = con.createArrayOf("bit", (Object[])param);
                stmt.setArray(i, array);
            } else if (param instanceof Short[][]) {
                Array array = con.createArrayOf("int2", (Object[])param);
                stmt.setArray(i, array);
            } else if (param instanceof Boolean[]) {
                Array array = con.createArrayOf("bool", (Object[])param);
                stmt.setArray(i, array);
            } else if (param instanceof Long[][]) {
                Array array = con.createArrayOf("int8", (Object[][])param);
                stmt.setArray(i, array);
            } else if (param instanceof Integer[][]) {
                Array array = con.createArrayOf("int4", (Object[][])param);
                stmt.setArray(i, array);
            } else if (param instanceof String[][]) {
                Array array = con.createArrayOf("varchar", (Object[][])param);
                stmt.setArray(i, array);
            } else if (param instanceof Float[][]) {
                Array array = con.createArrayOf("float4", (Object[][])param);
                stmt.setArray(i, array);
            } else if (param instanceof Double[][]) {
                Array array = con.createArrayOf("float8", (Object[][])param);
                stmt.setArray(i, array);
            } else if (param instanceof Date[][]) {
                Array array = con.createArrayOf("date", (Object[][])param);
                stmt.setArray(i, array);
            } else if (param instanceof Timestamp[][]) {
                Array array = con.createArrayOf("timestamp", (Object[][])param);
                stmt.setArray(i, array);
            } else if (param instanceof Time[][]) {
                Array array = con.createArrayOf("time", (Object[][])param);
                stmt.setArray(i, array);
            } else if (param instanceof Byte[][]) {
                Array array = con.createArrayOf("bit", (Object[][])param);
                stmt.setArray(i, array);
            } else if (param instanceof Boolean[][]) {
                Array array = con.createArrayOf("bool", (Object[][])param);
                stmt.setArray(i, array);
            } else if (param instanceof Long[][][]) {
                Array array = con.createArrayOf("int8", (Object[][][])param);
                stmt.setArray(i, array);
            } else if (param instanceof Integer[][][]) {
                Array array = con.createArrayOf("int4", (Object[][][])param);
                stmt.setArray(i, array);
            } else if (param instanceof Short[][]) {
                Array array = con.createArrayOf("int2", (Object[])param);
                stmt.setArray(i, array);
            } else if (param instanceof String[][][]) {
                Array array = con.createArrayOf("varchar", (Object[][][])param);
                stmt.setArray(i, array);
            } else if (param instanceof Float[][][]) {
                Array array = con.createArrayOf("float4", (Object[][][])param);
                stmt.setArray(i, array);
            } else if (param instanceof Double[][][]) {
                Array array = con.createArrayOf("float8", (Object[][][])param);
                stmt.setArray(i, array);
            } else if (param instanceof Date[][][]) {
                Array array = con.createArrayOf("date", (Object[][][])param);
                stmt.setArray(i, array);
            } else if (param instanceof Timestamp[][][]) {
                Array array = con.createArrayOf("timestamp", (Object[][][])param);
                stmt.setArray(i, array);
            } else if (param instanceof Time[][][]) {
                Array array = con.createArrayOf("time", (Object[][][])param);
                stmt.setArray(i, array);
            } else if (param instanceof Byte[][][]) {
                Array array = con.createArrayOf("bit", (Object[][][])param);
                stmt.setArray(i, array);
            } else if (param instanceof Boolean[][][]) {
                Array array = con.createArrayOf("bool", (Object[][][])param);
                stmt.setArray(i, array);
            } else {
                stmt.setObject(i, param);
            }
            i++;
        }

        logger.log("QUERY: " + generateActualSql(sql, parameters), logLevelQuery);

        return stmt;

    }

//    public void commitToDatabase(CachedRowSet crs) throws QueryHelperException, LoadConfigException {
//        Connection conn = null;
//
//        try (
//                Connection con = getConnection();
//             ) {
//            
//            // propagate changes and close connection
//            crs.acceptChanges(conn);
//            
//        } catch (SyncProviderException ex) {
//             throw new QueryHelperException(ex, ex.getSQLState());
//        } catch (SQLException ex) {
//             throw new QueryHelperException(ex, ex.getSQLState());
//        }
//    }
    public String makeProcedure(String query, Object... values) {

        StringBuilder params = new StringBuilder(query);
        params.append("(");

        boolean first = true;
        for (Object value : values) {
            if (!first) {
                params.append(",");
            }
            params.append(formatParameter(value));
            first = false;

        }

        params.append(")");

        return params.toString();
    }

    public int execute(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = getConnection();
            stmt = con.prepareStatement(sql, resultSetType, resultSetConcurrency);

            setStatement(stmt, con, sql, parameters);

            int result = stmt.executeUpdate();
            logger.log("Query Result: " + result, logLevelResult);
            return result;
        } catch (SQLException ex) {
            logger.error("DB Error Code: " + ex.getSQLState(), ex);
            throw new SFQueryHelperException(ex, ex.getSQLState());
        } catch (SFLoadConfigException ex) {
            logger.error(ex);
            throw ex;
        } catch (Exception ex) {
            logger.error(ex);
            throw new SFQueryHelperException(ex);
        } finally {
            closeResources(con, stmt);
        }
    }

    public int execute(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return execute(sql, new Object[]{});
    }


    protected void silentlyCloseGlobalConnection() {
        if (globalConnection != null) {
            try {
                globalConnection.close();
            } catch (Exception ex) {
                logger.warn(ex);
            }
            globalConnection = null;
        }
    }

    /**
     * Create an unique connection to this object and disable the auto commit.
     *
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public void begin() throws SFLoadConfigException, SFQueryHelperException {

        try {

            globalConnection = getConnection(0);
            globalConnection.setAutoCommit(false);

        } catch (SQLException ex) {
            logger.error("DB Error Code: " + ex.getSQLState(), ex);
            throw new SFQueryHelperException(ex, ex.getSQLState());
        } catch (SFLoadConfigException ex) {
            logger.error(ex);
            throw ex;
        } catch (Exception ex) {
            logger.error(ex);
            throw new SFQueryHelperException(ex);
        }
    }

    /**
     * Create an new savepoint
     *
     * @param name
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException This exception will be throw in case begin() method was not called first, or
     * if theres an error to obtain an connection to database
     */
    public Savepoint setSavePoint(String name) throws SFLoadConfigException, SFQueryHelperException {

        if (globalConnection == null) {
            throw new SFQueryHelperException("No active connection, use begin() first.");
        }

        try {

            if (name != null) {
                return globalConnection.setSavepoint();
            } else {
                return globalConnection.setSavepoint(name);
            }

        } catch (SQLException ex) {
            logger.error("DB Error Code: " + ex.getSQLState(), ex);
            throw new SFQueryHelperException(ex, ex.getSQLState());
        } catch (SFLoadConfigException ex) {
            logger.error(ex);
            throw ex;
        } catch (Exception ex) {
            logger.error(ex);
            throw new SFQueryHelperException(ex);
        }
    }

    /**
     * Create an new savepoint
     *
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException This exception will be throw in case begin() method was not called first, or
     * if theres an error to obtain an connection to database
     */
    public Savepoint setSavePoint() throws SFLoadConfigException, SFQueryHelperException {
        return setSavePoint(null);
    }

    /**
     * Do the commit of the changes done after begin() was used, and release the connection.
     *
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException This exception will be throw in case begin() method was not called first, or
     * if theres an error to obtain an connection to database
     */
    public void commit() throws SFLoadConfigException, SFQueryHelperException {

        if (globalConnection == null) {
            throw new SFQueryHelperException("No active connection, use begin() first.");
        }

        try {

            globalConnection.commit();

        } catch (SQLException ex) {
            logger.error("DB Error Code: " + ex.getSQLState(), ex);
            throw new SFQueryHelperException(ex, ex.getSQLState());
        } catch (SFLoadConfigException ex) {
            logger.error(ex);
            throw ex;
        } catch (Exception ex) {
            logger.error(ex);
            throw new SFQueryHelperException(ex);
        } finally {
            silentlyCloseGlobalConnection();
        }
    }

    /**
     * Do rollback of all changes since begin(), using an savepoint
     *
     * @param savePoint
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException This exception will be throw in case begin() method was not called first, or
     * if theres an error to obtain an connection to database
     */
    public void rollback(Savepoint savePoint) throws SFLoadConfigException, SFQueryHelperException {

        if (globalConnection == null) {
            throw new SFQueryHelperException("No active connection, use begin() first.");
        }

        try {

            if (savePoint == null) {
                globalConnection.rollback();
            } else {
                globalConnection.rollback(savePoint);
            }

        } catch (SQLException ex) {
            logger.error("DB Error Code: " + ex.getSQLState(), ex);
            throw new SFQueryHelperException(ex, ex.getSQLState());
        } catch (SFLoadConfigException ex) {
            logger.error(ex);
            throw ex;
        } catch (Exception ex) {
            logger.error(ex);
            throw new SFQueryHelperException(ex);
        } finally {
            silentlyCloseGlobalConnection();
        }
    }

    /**
     * Do rollback of all changes since begin()
     *
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException This exception will be throw in case begin() method was not called first, or
     * if theres an error to obtain an connection to database
     */
    public void rollback() throws SFLoadConfigException, SFQueryHelperException {
        rollback(null);
    }

    /**
     * Release all resources open (actually, only the global connection used by begin(), if it exist)
     */
    public void close() {
        silentlyCloseGlobalConnection();
    }

    /**
     * Try to close the connection and PreparedStatement and ignore possible errors
     *
     * @param con
     * @param stmt
     */
    protected void closeResources(Connection con, PreparedStatement stmt) {
        if (con != null && globalConnection == null) {
            try {
                con.close();
            } catch (SQLException ex) {
                logger.warn(ex);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ex) {
                logger.warn(ex);
            }
        }
    }

    /**
     * Execute an query and return multiple lines with callback.
     *
     * @param sql
     * @param callback
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public boolean executeRowsCallback(String sql, SFQueryRow callback, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = getConnection();
            stmt = con.prepareStatement(sql, resultSetType, resultSetConcurrency);

            setStatement(stmt, con, sql, parameters);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    if (!callback.readRow(rs)) {
                        return false;
                    }

                }

            }
            return true;
        } catch (SQLException ex) {
            logger.error("DB Error Code: " + ex.getSQLState(), ex);
            throw new SFQueryHelperException(ex, ex.getSQLState());
        } catch (SFLoadConfigException ex) {
            logger.error(ex);
            throw ex;
        } catch (Exception ex) {
            logger.error(ex);
            throw new SFQueryHelperException(ex);
        } finally {
            closeResources(con, stmt);
        }
    }

    /**
     * Execute the query and return multiple lines.
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public CachedRowSet executeRows(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = getConnection();
            stmt = con.prepareStatement(sql, resultSetType, resultSetConcurrency);

            setStatement(stmt, con, sql, parameters);

            try (ResultSet rs = stmt.executeQuery()) {
                CachedRowSet rowset = newCachedRowSet(rs);
                logger.log("Query Result: " + toJson(rowset), logLevelResult);
                return rowset;
            }
        } catch (SQLException ex) {
            logger.error("DB Error Code: " + ex.getSQLState(), ex);
            throw new SFQueryHelperException(ex, ex.getSQLState());
        } catch (SFLoadConfigException ex) {
            logger.error(ex);
            throw ex;
        } catch (Exception ex) {
            logger.error(ex);
            throw new SFQueryHelperException(ex);
        } finally {
            closeResources(con, stmt);
        }
    }

    /**
     * Execute the query and return multiple lines.
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public CachedRowSet executeRows(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return executeRows(sql, new Object[]{});
    }

    /**
     * Transform an ResultSet in an Bean
     *
     * @param <T>
     * @param rs
     * @param clazz
     * @return
     * @throws SFQueryHelperException
     */
    public <T> T rowToBean(ResultSet rs, Class<T> clazz) throws SFQueryHelperException {
        try {
            BasicRowProcessor row = new BasicRowProcessor();
            return row.toBean(rs, clazz);
        } catch (SQLException ex) {
            logger.error("DB Error Code: " + ex.getSQLState(), ex);
            throw new SFQueryHelperException(ex, ex.getSQLState());
        }
    }

    /**
     * Transform an ResultSet in an list of Beans
     *
     * @param <T>
     * @param rs
     * @param clazz
     * @return
     * @throws SFQueryHelperException
     */
    public <T> List<T> rowsToBeanList(ResultSet rs, Class<T> clazz) throws SFQueryHelperException {
        try {
            BasicRowProcessor row = new BasicRowProcessor();
            return row.toBeanList(rs, clazz);
        } catch (SQLException ex) {
            logger.error("DB Error Code: " + ex.getSQLState(), ex);
            throw new SFQueryHelperException(ex, ex.getSQLState());
        }
    }

    /**
     * Execute an query and return an list of Beans
     *
     * @param clazz
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public <T> List<T> executeRowsHandled(Class<T> clazz, String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        CachedRowSet cachedRowSet = executeRows(sql, parameters);
        BasicRowProcessor row = new BasicRowProcessor();
        try {
            return row.toBeanList(cachedRowSet, clazz);
        } catch (SQLException ex) {
            logger.error("DB Error Code: " + ex.getSQLState(), ex);
            throw new SFQueryHelperException(ex, ex.getSQLState());
        } catch (SFLoadConfigException ex) {
            logger.error(ex);
            throw ex;
        } catch (Exception ex) {
            logger.error(ex);
            throw new SFQueryHelperException(ex);
        }
    }

    /**
     * Execute an query and return an list of Beans
     *
     * @param clazz
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public <T> List<T> executeRowsHandled(Class<T> clazz, String sql) throws SFLoadConfigException, SFQueryHelperException {
        return executeRowsHandled(clazz, sql, new Object[]{});
    }

    /**
     * Execute an query and return an list of Beans
     *
     * @param clazz
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public <T> T executeRowHandled(Class<T> clazz, String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        CachedRowSet cachedRowSet = executeRow(sql, parameters);
        BasicRowProcessor row = new BasicRowProcessor();
        try {
            if (cachedRowSet != null) {
                return row.toBean(cachedRowSet, clazz);
            } else {
                return null;
            }
        } catch (SQLException ex) {
            logger.error("DB Error Code: " + ex.getSQLState(), ex);
            throw new SFQueryHelperException(ex, ex.getSQLState());
        } catch (SFLoadConfigException ex) {
            logger.error(ex);
            throw ex;
        } catch (Exception ex) {
            logger.error(ex);
            throw new SFQueryHelperException(ex);
        }
    }

    /**
     * Execute an query and return an list of Beans
     *
     * @param clazz
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public <T> T executeRowHandled(Class<T> clazz, String sql) throws SFLoadConfigException, SFQueryHelperException {
        return executeRowHandled(clazz, sql, new Object[]{});
    }

    /**
     * Execute an query and force the return of only 1 line.
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public CachedRowSet executeRow(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = getConnection();
            stmt = con.prepareStatement(sql, resultSetType, resultSetConcurrency);

            stmt.setMaxRows(1);
            setStatement(stmt, con, sql, parameters);

            try (ResultSet rs = stmt.executeQuery()) {
                CachedRowSet rowset = newCachedRowSet(rs);
                logger.log("Query Result: " + toJson(rowset), logLevelResult);
                if (!rowset.next()) {
                    return null;
                } else {
                    return rowset;
                }
            }
        } catch (SQLException ex) {
            logger.error("DB Error Code: " + ex.getSQLState(), ex);
            throw new SFQueryHelperException(ex, ex.getSQLState());
        } catch (SFLoadConfigException ex) {
            logger.error(ex);
            throw ex;
        } catch (Exception ex) {
            logger.error(ex);
            throw new SFQueryHelperException(ex);
        } finally {
            closeResources(con, stmt);
        }
    }

    /**
     * Execute an query and force the return of only 1 line.
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public CachedRowSet executeRow(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return executeRow(sql, new Object[]{});
    }

    /**
     * Execute an query and return multiple lines, but return null in case nothing was found
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public CachedRowSet executeOrNull(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = getConnection();
            stmt = con.prepareStatement(sql, resultSetType, resultSetConcurrency);

            setStatement(stmt, con, sql, parameters);
            boolean b = stmt.execute();

            if (b) {
                try (ResultSet rs = stmt.getResultSet()) {
                    CachedRowSet rowset = newCachedRowSet(rs);
                    logger.log("Query Result: " + toJson(rowset), logLevelResult);
                    return rowset;
                }
            } else {
                return null;
            }
        } catch (SQLException ex) {
            logger.error("DB Error Code: " + ex.getSQLState(), ex);
            throw new SFQueryHelperException(ex, ex.getSQLState());
        } catch (SFLoadConfigException ex) {
            logger.error(ex);
            throw ex;
        } catch (Exception ex) {
            logger.error(ex);
            throw new SFQueryHelperException(ex);
        } finally {
            closeResources(con, stmt);
        }
    }

    /**
     * Execute an query and return multiple lines, but return null in case nothing was found
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public CachedRowSet executeOrNull(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return executeOrNull(sql, new Object[]{});
    }

    protected <T> T castObject(Object obj, Class<T> clazz) {

        // cast from Integer to Boolean
        if (obj instanceof Integer && clazz.isAssignableFrom(Boolean.class)) {
            return clazz.cast(((Integer) obj) > 0);
        } else if (obj instanceof BigDecimal && clazz.isAssignableFrom(Long.class)) {
            return clazz.cast(((BigDecimal) obj).longValue());
        } else {
            return clazz.cast(obj);
        }
    }

    /**
     * Execute an query and return the result as an list of the specified type
     *
     * @param <T>
     * @param sql
     * @param defaultValue
     * @param clazz
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public <T> List<T> queryObjects(String sql, T defaultValue, Class<T> clazz, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {

        try {
            CachedRowSet rs = executeRows(sql, parameters);
            List<T> list = new ArrayList<>();

            if (rs != null) {

                while (rs.next()) {

                    Object obj = rs.getObject(1);

                    if (obj == null) {
                        break;
                    } else {
                        list.add(castObject(obj, clazz));
                    }

                }

            }

            if (list.isEmpty() && defaultValue != null) {
                list.add(defaultValue);
            }
            return list;

        } catch (SQLException ex) {
            logger.error("DB Error Code: " + ex.getSQLState(), ex);
            throw new SFQueryHelperException(ex, ex.getSQLState());
        } catch (SFLoadConfigException ex) {
            logger.error(ex);
            throw ex;
        } catch (SFQueryHelperException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error(ex);
            throw new SFQueryHelperException(ex);
        }
    }

    /**
     * Execute an query and return the result as an list of the specified type
     *
     * @param <T>
     * @param sql
     * @param clazz
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public <T> List<T> queryObjects(String sql, Class<T> clazz, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObjects(sql, null, clazz, parameters);
    }

    /**
     * Executa a query e traz o resultado no tipo especificado.
     *
     * @param <T>
     * @param sql
     * @param defaultValue
     * @param clazz
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public <T> T queryObject(String sql, T defaultValue, Class<T> clazz, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        try {
            CachedRowSet rs = executeRow(sql, parameters);
            
            if (clazz == null) {
                return null;
            }
            
            if (rs == null || !rs.first()) {
                return defaultValue;
            }

            Object obj = rs.getObject(1);

            if (obj == null) {
                return defaultValue;
            } else {
                return castObject(obj, clazz);
            }
        } catch (SQLException ex) {
            logger.error("DB Error Code: " + ex.getSQLState(), ex);
            throw new SFQueryHelperException(ex, ex.getSQLState());
        } catch (SFLoadConfigException ex) {
            logger.error(ex);
            throw ex;
        } catch (SFQueryHelperException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error(ex);
            throw new SFQueryHelperException(ex);
        }
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param <T>
     * @param sql
     * @param clazz
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public <T> T queryObject(String sql, Class<T> clazz, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObject(sql, null, clazz, parameters);
    }

    /**
     * Executa a query e traz o resultado no tipo especificado.
     *
     * @param <T>
     * @param sql
     * @param clazz
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public <T> T queryObject(String sql, Class<T> clazz) throws SFLoadConfigException, SFQueryHelperException {
        return queryObject(sql, null, clazz, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public void queryVoid(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        queryObject(sql, null, null, parameters);
    }
    
    /**
     * Execute an query without expecting an return
     *
     * @param sql
     * @param parameters
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public void query(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        queryVoid(sql, parameters);
    }
    
    
    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public String queryString(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObject(sql, null, String.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<String> queryStrings(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObjects(sql, null, String.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public String queryString(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryString(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<String> queryStrings(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryStrings(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public Integer queryInt(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObject(sql, null, Integer.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<Integer> queryInts(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObjects(sql, null, Integer.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public Integer queryInt(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryInt(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<Integer> queryInts(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryInts(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public BigDecimal queryBigDecimal(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObject(sql, null, BigDecimal.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<BigDecimal> queryBigDecimals(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObjects(sql, null, BigDecimal.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public BigDecimal queryBigDecimal(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryBigDecimal(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<BigDecimal> queryBigDecimals(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryBigDecimals(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public Float queryFloat(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObject(sql, null, Float.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<Float> queryFloats(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObjects(sql, null, Float.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public Float queryFloat(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryFloat(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<Float> queryFloats(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryFloats(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public Double queryDouble(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObject(sql, null, Double.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<Double> queryDoubles(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObjects(sql, null, Double.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public Double queryDouble(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryDouble(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<Double> queryDoubles(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryDoubles(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public Boolean queryBoolean(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObject(sql, null, Boolean.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<Boolean> queryBooleans(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObjects(sql, null, Boolean.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public Boolean queryBoolean(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryBoolean(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<Boolean> queryBooleans(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryBooleans(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public Long queryLong(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObject(sql, null, Long.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<Long> queryLongs(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObjects(sql, null, Long.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public Long queryLong(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryLong(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<Long> queryLongs(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryLongs(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public Short queryShort(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObject(sql, null, Short.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<Short> queryShorts(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObjects(sql, null, Short.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public Short queryShort(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryShort(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<Short> queryShorts(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryShorts(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public Date queryDate(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObject(sql, null, Date.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<Date> queryDates(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObjects(sql, null, Date.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public Date queryDate(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryDate(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<Date> queryDates(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryDates(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public Time queryTime(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObject(sql, null, Time.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<Time> queryTimes(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObjects(sql, null, Time.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public Time queryTime(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryTime(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<Time> queryTimes(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryTimes(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public URL queryURL(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObject(sql, null, URL.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<URL> queryURLs(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObjects(sql, null, URL.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public URL queryURL(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryURL(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<URL> queryURLs(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryURLs(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public Byte queryByte(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObject(sql, null, Byte.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @param parameters
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<Byte> queryBytes(String sql, Object... parameters) throws SFLoadConfigException, SFQueryHelperException {
        return queryObjects(sql, null, Byte.class, parameters);
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public Byte queryByte(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryByte(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param sql
     * @return
     * @throws SFLoadConfigException
     * @throws SFQueryHelperException
     */
    public List<Byte> queryBytes(String sql) throws SFLoadConfigException, SFQueryHelperException {
        return queryBytes(sql, new Object[]{});
    }

    /**
     * Execute an query and return the result the specified type
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    protected List resultSetToArrayList(CachedRowSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        ArrayList list = new ArrayList();
        rs.beforeFirst();
        while (rs.next()) {
            HashMap row = new HashMap(columns);
            for (int i = 1; i <= columns; ++i) {
                row.put(md.getColumnName(i), rs.getString(i));
            }
            list.add(row);
        }
        rs.beforeFirst();

        return list;
    }

    protected String toJson(CachedRowSet crs) throws SQLException {
        return "\n" + gsonpp.toJson(resultSetToArrayList(crs));
    }

    public void createInsertBatch(String insertQuery) {

        if (insertBatch == null) {

            insertBatch = new StringBuilder(insertQuery);

        } else {
            // apaga tudo
            insertBatch.setLength(0);
            // insere o conteudo inicial
            insertBatch.append(insertQuery);
        }

        if (!insertBatch.toString().trim().toLowerCase().endsWith("values")) {
            insertBatch.append(" values ");
        }

        insertBatchBase = insertBatch.toString();
    }

    public void appendInsertBatch(Object... values) {
        if (insertBatch == null || insertBatch.length() == 0) {
            if (insertBatch != null && insertBatchBase != null && !insertBatchBase.isEmpty()) {
                insertBatch.append(insertBatchBase);
            } else {
                throw new InvalidParameterException("Voce precisa chamar o startInsertBatch() primeiro");
            }
        }

        insertBatch.append("(");

        boolean first = true;
        for (Object value : values) {
            if (!first) {
                insertBatch.append(",");
            }
            insertBatch.append(formatParameter(value));
            first = false;

        }

        insertBatch.append(")");
        insertBatch.append(",");
    }

    public void executeInsertBatch() throws SFLoadConfigException, SFQueryHelperException {
        String insertBatchStr = insertBatch.toString();
        if (insertBatch != null && insertBatchStr.length() > 0 && !insertBatchStr.trim().equals(insertBatchBase.trim())) {
            insertBatch.setLength(insertBatch.length() - 1);
            try {
                this.execute(insertBatch.toString());
            } finally {
                insertBatch.setLength(0);
            }
        }
    }
    
    public static <T> T[] toArray(List<T> list, Class<T> clazz) {
        T[] newInstance = (T[])java.lang.reflect.Array.newInstance(clazz, list.size());
        
        return (T[])list.toArray(newInstance);
    }
    
    public static <T> T[][] toArray2d(List<List<T>> list, Class<T> clazz) {
        T[][] newInstance = (T[][])java.lang.reflect.Array.newInstance(clazz, list.size(), 0);
        
        int i = 0;
        for (List<T> lst : list) {
            T[] arr = lst.toArray((T[])java.lang.reflect.Array.newInstance(clazz, lst.size()));
            newInstance[i++] = arr;
        }
        
        return newInstance;
    }

    


}
