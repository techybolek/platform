/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.hive.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveContext;
import org.wso2.carbon.analytics.hive.HiveConstants;
import org.wso2.carbon.analytics.hive.Utils;
import org.wso2.carbon.analytics.hive.dto.QueryResult;
import org.wso2.carbon.analytics.hive.dto.QueryResultRow;
import org.wso2.carbon.analytics.hive.dto.ScriptResult;
import org.wso2.carbon.analytics.hive.exception.HiveExecutionException;
import org.wso2.carbon.analytics.hive.exception.RegistryAccessException;
import org.wso2.carbon.analytics.hive.extension.AbstractHiveAnalyzer;
import org.wso2.carbon.analytics.hive.annotation.AbstractHiveAnnotation;
import org.wso2.carbon.analytics.hive.multitenancy.HiveMultitenantUtil;
import org.wso2.carbon.analytics.hive.multitenancy.HiveRSSMetastoreManager;
import org.wso2.carbon.analytics.hive.service.HiveExecutorService;
import org.wso2.carbon.analytics.hive.util.RegistryAccessUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HiveExecutorServiceImpl implements HiveExecutorService {

    private static final Log log = LogFactory.getLog(HiveExecutorServiceImpl.class);

    private static boolean IS_PROFILING_ENABLED = false;

    private static DateFormat dateFormat = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
    private static int uriCounter = 0;
    //Used for round robin fetching of meta store uris in multiple remote meta store server scenario


    static {
        try {
            Class.forName("org.apache.hadoop.hive.jdbc.HiveDriver");
        } catch (ClassNotFoundException e) {
            log.fatal("Hive JDBC Driver not found in the class path. Hive query execution will" +
                      " fail..", e);
        }

        try {
            IS_PROFILING_ENABLED = Boolean.parseBoolean(System.getProperty(
                    HiveConstants.ENABLE_PROFILE_PROPERTY));
        } catch (Exception ignored) {
            // Ignore malformed switch values and defaults to false
        }
    }

    /**
     * @param script
     * @return The Resultset of all executed queries in the script
     * @throws HiveExecutionException
     */
    public QueryResult[] execute(String script) throws HiveExecutionException {
        String tenantDomain = PrivilegedCarbonContext.getCurrentContext().getTenantDomain(true);
        int tenantId = PrivilegedCarbonContext.getCurrentContext().getTenantId();
        if (Utils.canConnectToRSS() && HiveMultitenantUtil.isMultiTenantMode() &&  null != HiveRSSMetastoreManager.getInstance()) {
            HiveRSSMetastoreManager.getInstance().prepareRSSMetaStore(tenantDomain, tenantId);
        }
        if (script != null) {

            ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

            // Parse script to bind registry value
            try {
                script = RegistryAccessUtil.parseScriptForRegistryValues(script);
            } catch (RegistryAccessException e) {
                log.error("Error during parsing registry values ...", e);
                throw new HiveExecutionException(e.getExceptionMessage(), e);
            }

            ScriptCallable callable = new ScriptCallable(tenantId, script);

            Future<ScriptResult> future = singleThreadExecutor.submit(callable);

            ScriptResult result;
            try {
                result = future.get();
            } catch (InterruptedException e) {
                log.error("Query execution interrupted..", e);
                throw new HiveExecutionException("Query execution interrupted..", e);
            } catch (ExecutionException e) {
                log.error("Error during query execution..", e);
                throw new HiveExecutionException("Error during query execution..", e);
            }

            if (result != null) {
                if (result.getErrorMessage() != null) {
                    throw new HiveExecutionException(result.getErrorMessage());
                }

                removeUTFCharsFromValues(result);

                return result.getQueryResults();

            } else {
                throw new HiveExecutionException("Query returned a NULL result..");
            }

/*            int threadCount = 0;
            try {
                threadCount = Integer.parseInt(script);
            } catch (Exception e) {
                ScriptCallable callable = new ScriptCallable(script);
                ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

                Future<ScriptResult> future = singleThreadExecutor.submit(callable);

                ScriptResult result;
                try {
                    result = future.get();
                } catch (InterruptedException x) {
                    log.error("Query execution interrupted..", x);
                    throw new HiveExecutionException("Query execution interrupted..", x);
                } catch (ExecutionException z) {
                    log.error("Error during query execution..", z);
                    throw new HiveExecutionException("Error during query execution..", z);
                }
            }

            for (int i = 0; i < threadCount; i++) {
                ScriptCallable callable = new ScriptCallable(asScript);
                ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

                singleThreadExecutor.submit(callable);

            }*/

        }

        return null;

    }

    private void removeUTFCharsFromValues(ScriptResult result) {
        QueryResult[] queryResults = result.getQueryResults();
        for(QueryResult queryResult:queryResults){
            QueryResultRow[] resultRows = queryResult.getResultRows();
            for(QueryResultRow queryResultRow:resultRows){
                String[] columnValues = queryResultRow.getColumnValues();
                String[] columnValuesWithoutUTFChars = new String[columnValues.length];
                for (int i=0; i< columnValues.length;i++){
                    columnValuesWithoutUTFChars[i] =columnValues[i].replaceAll("[\\u0000]", "");
                }
                queryResultRow.setColumnValues(columnValuesWithoutUTFChars);
            }

        }
    }

    /**
     * @param script
     * @return The Resultset of all executed queries in the script
     * @throws HiveExecutionException
     */
    public QueryResult[] execute(int tenantId, String script) throws HiveExecutionException {
        if (script != null) {

            ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

            ScriptCallable callable = new ScriptCallable(tenantId, script);

            Future<ScriptResult> future = singleThreadExecutor.submit(callable);

            ScriptResult result;
            try {
                result = future.get();
            } catch (InterruptedException e) {
                log.error("Query execution interrupted..", e);
                throw new HiveExecutionException("Query execution interrupted..", e);
            } catch (ExecutionException e) {
                log.error("Error during query execution..", e);
                throw new HiveExecutionException("Error during query execution..", e);
            }

            if (result != null) {
                if (result.getErrorMessage() != null) {
                    throw new HiveExecutionException(result.getErrorMessage());
                }

                return result.getQueryResults();

            } else {
                throw new HiveExecutionException("Query returned a NULL result..");
            }

/*            int threadCount = 0;
            try {
                threadCount = Integer.parseInt(script);
            } catch (Exception e) {
                ScriptCallable callable = new ScriptCallable(script);
                ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

                Future<ScriptResult> future = singleThreadExecutor.submit(callable);

                ScriptResult result;
                try {
                    result = future.get();
                } catch (InterruptedException x) {
                    log.error("Query execution interrupted..", x);
                    throw new HiveExecutionException("Query execution interrupted..", x);
                } catch (ExecutionException z) {
                    log.error("Error during query execution..", z);
                    throw new HiveExecutionException("Error during query execution..", z);
                }
            }

            for (int i = 0; i < threadCount; i++) {
                ScriptCallable callable = new ScriptCallable(asScript);
                ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

                singleThreadExecutor.submit(callable);

            }*/

        }

        return null;

    }

    private class ScriptCallable implements Callable<ScriptResult> {

        private final String HIVE_META_STORE_LOCAL = "hive.metastore.local";
        private final String HIVE_META_STORE_URIS = "hive.metastore.uris";
        private final String LOCAL_META_STORE_URI = "jdbc:hive://";

        private String script;

        private int tenantId;

        public ScriptCallable(int tenantId, String script) {
            this.script = script;
            this.tenantId = tenantId;
        }

        public ScriptResult call() {

            HiveContext.startTenantFlow(tenantId);

            Connection con;
            try {
                con = getConnection();
            } catch (SQLException e) {
                log.error("Error getting connection to any listed remote Hive meta stores..", e);

                ScriptResult result = new ScriptResult();
                result.setErrorMessage("Error getting connection to any" +
                                       " listed remote Hive meta stores..." + e);
                return result;
            }

            Statement stmt;
            try {
                stmt = con.createStatement();
            } catch (SQLException e) {
                log.error("Error getting statement..", e);

                ScriptResult result = new ScriptResult();
                result.setErrorMessage("Error getting statement." + e.getMessage());
                return result;
            }

            try {

                Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
                Matcher regexMatcher = regex.matcher(script);
                String formattedScript = "";
                while (regexMatcher.find()) {
                    String temp = "";
                    if (regexMatcher.group(1) != null) {
                        // Add double-quoted string without the quotes
                        temp = regexMatcher.group(1).replaceAll(";", "%%");
                        if (temp.contains("%%")) {
                            temp = temp.replaceAll(" ", "");
                            temp = temp.replaceAll("\n", "");
                        }
                        temp = "\"" + temp + "\"";
                    } else if (regexMatcher.group(2) != null) {
                        // Add single-quoted string without the quotes
                        temp = regexMatcher.group(2).replaceAll(";", "%%");
                        if (temp.contains("%%")) {
                            temp = temp.replaceAll(" ", "");
                            temp = temp.replaceAll("\n", "");
                        }
                        temp = "\'" + temp + "\'";
                    } else {
                        temp = regexMatcher.group();
                    }
                    formattedScript += temp + " ";
                }


                String[] cmdLines = formattedScript.split(";\\r?\\n|;"); // Tokenize with ;[new-line]

                ScriptResult result = new ScriptResult();

                Date startDateTime = null;
                long startTime = 0;
                if (IS_PROFILING_ENABLED) {
                    startTime = System.currentTimeMillis();
                    startDateTime = new Date();
                }

                for (String cmdLine : cmdLines) {

                    String trimmedCmdLine = cmdLine.trim();
                    trimmedCmdLine = trimmedCmdLine.replaceAll(";", "");
                    trimmedCmdLine = trimmedCmdLine.replaceAll("%%", ";");
                    //Fixing some issues in the hive query due to /n/t
                    trimmedCmdLine = trimmedCmdLine.replaceAll("\n", " ");
                    trimmedCmdLine = trimmedCmdLine.replaceAll("\t", " ");

                    if ("".equals(trimmedCmdLine)) {
                        continue;
                    }

                    if (trimmedCmdLine.startsWith("class") ||
                            trimmedCmdLine.startsWith("CLASS")) { // Class analyzer for executing custom logic
                        String[] tokens = trimmedCmdLine.split("\\s+");
                        if (tokens != null && tokens.length >= 2) {
                            String className = tokens[1];

                            Class clazz = null;
                            try {
                                clazz = Class.forName(className, true,
                                        this.getClass().getClassLoader());
                            } catch (ClassNotFoundException e) {
                                log.error("Unable to find custom analyzer class..", e);
                            }

                            if (clazz != null) {
                                Object analyzer = null;
                                try {
                                    analyzer = clazz.newInstance();
                                } catch (InstantiationException e) {
                                    log.error("Unable to instantiate custom analyzer class..", e);
                                } catch (IllegalAccessException e) {
                                    log.error("Unable to instantiate custom analyzer class..", e);
                                }

                                if (analyzer instanceof AbstractHiveAnalyzer) {
                                    AbstractHiveAnalyzer hiveAnalyzer =
                                            (AbstractHiveAnalyzer) analyzer;
                                    hiveAnalyzer.execute();
                                } else {
                                    log.error("Custom analyzers should extend AbstractHiveAnalyzer..");
                                }
                            }
                        }
                    } else if (trimmedCmdLine.startsWith("@script.") ||
                            trimmedCmdLine.startsWith("@SCRIPT.")) {
                        HashMap<String, String> annotations = new HashMap<String, String>(); // annotation execution
                        String[] contentSet = trimmedCmdLine.split("@script.");
                        String txt = contentSet[1];

                        String re1 = ".*?";    // Non-greedy match on filler
                        String re2 = "(\\(.*\\))";    // Round Braces 1

                        Pattern p = Pattern.compile(re1 + re2, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                        Matcher m = p.matcher(txt);
                        if (m.find()) {
                            String rbraces1 = m.group(1);
                            int temp = rbraces1.trim().length();

                            String s = rbraces1.substring(1, temp - 1);

                            String[] variables = s.split(";");

                            for (String g : variables) {
                                String[] values = g.split("=");
                                String key = values[0];
                                String value = values[1].substring(1, values[1].length() - 1);
                                annotations.put(key, value);
                            }
                        }
                        String className = "org.wso2.carbon.analytics.hive.extension.annotation.testAnnotation";

                        Class clazz = null;
                        try {
                            clazz = Class.forName(className, true,
                                    this.getClass().getClassLoader());
                        } catch (ClassNotFoundException e) {
                            log.error("Unable to find custom annotation class..", e);
                        }

                        if (clazz != null) {
                            Object annotation = null;
                            try {
                                annotation = clazz.newInstance();
                            } catch (InstantiationException e) {
                                log.error("Unable to instantiate custom annotation class..", e);
                            } catch (IllegalAccessException e) {
                                log.error("Unable to instantiate custom annotation class..", e);
                            }

                            if (annotation instanceof AbstractHiveAnnotation) {
                                AbstractHiveAnnotation hiveAnnotation =
                                        (AbstractHiveAnnotation) annotation;
                                hiveAnnotation.init();
                            } else {
                                log.error("Custom annotations should extend AbstractHiveAnnotation..");
                            }
                        }
                    } else { // Normal hive query
                        QueryResult queryResult = new QueryResult();

                        queryResult.setQuery(trimmedCmdLine);

                        //Append the tenant ID to query
                        //trimmedCmdLine += Utils.TENANT_ID_SEPARATOR_CHAR_SEQ + tenantId;

                        ResultSet rs = stmt.executeQuery(trimmedCmdLine);
                        ResultSetMetaData metaData = rs.getMetaData();

                        int columnCount = metaData.getColumnCount();
                        List<String> columnsList = new ArrayList<String>();
                        for (int i = 1; i <= columnCount; i++) {
                            columnsList.add(metaData.getColumnName(i));
                        }

                        queryResult.setColumnNames(columnsList.toArray(new String[]{}));

                        List<QueryResultRow> results = new ArrayList<QueryResultRow>();
                        while (rs.next()) {
                            QueryResultRow resultRow = new QueryResultRow();

                            List<String> columnValues = new ArrayList<String>();

							int noOfColumns = rs.getMetaData().getColumnCount();

                            boolean isTombstone=true;
                            for(int k=1;k<=noOfColumns;k++){
                                 Object obj= rs.getObject(k);
                                if(obj != null){
                                    isTombstone =false;
                                    break;
                                }
                            }

                            if(!isTombstone){
                                Object resObject = rs.getObject(1);
		                        if (resObject.toString().contains("\t")) {
		                            columnValues = Arrays.asList(resObject.toString().split("\t"));
		                        } else {
		                            for (int i = 1; i <= columnCount; i++) {
		                                Object resObj = rs.getObject(i);
		                                if (null != resObj) {
		                                    columnValues.add(rs.getObject(i).toString());
		                                } else {
		                                    columnValues.add("");
		                                }
		                            }
		                        }
		                        resultRow.setColumnValues(columnValues.toArray(new String[]{}));

		                        results.add(resultRow);
							}

                        }

                        queryResult.setResultRows(results.toArray(new QueryResultRow[]{}));
                        result.addQueryResult(queryResult);
                    }

                }

                StringBuffer sb = new StringBuffer();
                if (IS_PROFILING_ENABLED) {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;

                    long seconds = duration / 1000;

                    long secondsInMinute = seconds % 60;
                    long minutesInHours = ((seconds % 3600) / 60);
                    long hoursInDays = ((seconds % 86400) / 3600);
                    long daysInYears = ((seconds % 2592000) / 86400);
                    long months = seconds / 2592000;

                    sb.append("Start Time : " + dateFormat.format(startDateTime) + "\n");
                    sb.append("End Time : " + dateFormat.format(new Date()) + "\n");
                    sb.append("Duration [MM/DD hh:mm:ss] : " + months + "/" + daysInYears + " " +
                            hoursInDays + ":" + minutesInHours + ":" + secondsInMinute + "\n");
                    sb.append("==========================================\n");

                    synchronized (this) {
                        File file = new File(CarbonUtils.getCarbonHome() + File.separator +
                                "analyzer-perf.txt");

                        try {
                            OutputStream stream = null;
                            try {
                                stream = new BufferedOutputStream(new FileOutputStream(file, true));
                                IOUtils.copy(IOUtils.toInputStream(sb.toString()), stream);
                            } finally {
                                IOUtils.closeQuietly(stream);
                            }
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }


                HiveContext.endTenantFlow();

                return result;


            } catch (SQLException e) {
                log.error("Error while executing Hive script.\n" + e.getMessage(), e);

                ScriptResult result = new ScriptResult();
                result.setErrorMessage("Error while executing Hive script." + e.getMessage());

                return result;
            } finally {
                if (null != con) {
                    try {
                        con.close();
                    } catch (SQLException e) {
                    }
                }
            }

        }

        private Connection getConnection() throws SQLException {
            HiveConf conf = HiveContext.getCurrentContext().getConf();
            boolean isLocalMetaStore = Boolean.parseBoolean(
                    conf.get(HIVE_META_STORE_LOCAL, "true"));

            String uri = LOCAL_META_STORE_URI;
            Connection con = null;
            if (!isLocalMetaStore) {
                String uriString = conf.get(HIVE_META_STORE_URIS);
                if (uriString != null) {
                    String[] uris = uriString.split(",");

                    if (uris.length == 0) {
                        log.warn("No remote URI's specified though remote Meta store is enabled." +
                                 " Defaulting to local meta store..");
                        uris = new String[1];
                        uris[0] = LOCAL_META_STORE_URI;
                    }

                    int noOfUris = uris.length;

                    uri = uris[(uriCounter++) % noOfUris];

                    boolean connectionSuccessful = false;
                    int tryCount = 0; // Number of uris already tried to connect
                    while (!connectionSuccessful) {
                        try {
                            con = DriverManager.getConnection(uri, null, null);
                        } catch (SQLException e) {
                            log.error("Error getting connection for meta store URI " + uri +
                                      " ..", e);

                            tryCount++;
                            if (uriCounter >= noOfUris && tryCount == noOfUris) {
                                uriCounter = 0; // Reset uri count if unsuccessful to prevent unbounded increment in the variable
                                throw e;
                            } else {
                                uri = uris[(uriCounter++) % noOfUris];
                                continue;

                            }
                        }

                        if (uriCounter >= noOfUris) {
                            uriCounter = 0; // Reset uri count if successful to prevent unbounded increment in the variable
                        }
                        connectionSuccessful = true;
                    }

                } else {
                    log.warn("No remote URI's specified though remote Meta store is enabled." +
                             " Defaulting to local meta store..");
                    try {
                        con = DriverManager.getConnection(LOCAL_META_STORE_URI, null, null);
                    } catch (SQLException e) {
                        log.error("Error getting connection..", e);

                        throw e;
                    }
                }
            } else {
                try {
                    con = DriverManager.getConnection(uri, null, null);
                } catch (SQLException e) {
                    log.error("Error getting connection..", e);

                    throw e;
                }

            }

            return con;

        }

    }

}
