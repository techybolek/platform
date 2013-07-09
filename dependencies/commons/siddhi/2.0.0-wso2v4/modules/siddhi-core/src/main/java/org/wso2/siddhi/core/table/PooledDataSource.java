package org.wso2.siddhi.core.table;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class PooledDataSource implements SiddhiDataSource {

    private DataSource dataSource;
    private String type;
    private String dataSourceName;

    public PooledDataSource(String driverClassName, String url, String username, String password, String type, String dataSourceName) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        this.type = type;
        this.dataSourceName = dataSourceName;
        this.dataSource = setupDataSource(driverClassName, url, username, password);
    }


    private DataSource setupDataSource(String driverClass, String connectURI, String username, String password) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class.forName(driverClass).newInstance();
        GenericObjectPool connectionPool = new GenericObjectPool(null);
        connectionPool.setMaxActive(10);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, username, password);
        new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);
        PoolingDataSource poolingDataSource = new PoolingDataSource(connectionPool);
        return poolingDataSource;
    }


    @Override
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        return dataSource.getConnection();
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getName() {
        return dataSourceName;
    }
}
