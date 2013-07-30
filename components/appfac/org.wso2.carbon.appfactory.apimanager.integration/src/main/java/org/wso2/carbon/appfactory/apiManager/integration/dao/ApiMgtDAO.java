package org.wso2.carbon.appfactory.apiManager.integration.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.dto.API;

/**
 * 
 * This class handles the API-M database access related activities
 *
 */
public class ApiMgtDAO {

	private static final Log log = LogFactory.getLog(ApiMgtDAO.class);
	
	//Constant that holds the API-M datasource jndi name
	private static final String DATA_SOURCE_NAME = "jdbc/WSO2AM_DB";
	
	private static ApiMgtDAO apiMgtDAO = new ApiMgtDAO();

	
	public static ApiMgtDAO getInstance() {
		return apiMgtDAO;
	}
	
	/**
	 * Read the basic API information from the database and return a list of API objects
	 * @param userName username
	 * @param appKey application key
	 * @return List of API objects
	 * @throws AppFactoryException
	 */
	public List<API> getBasicAPIInfo(String userName, String appKey) throws AppFactoryException {
		List<API> apis = new ArrayList<API>();
		Connection conn = null;
	        try {
	        conn = getConnection();
			String query = "SELECT   API.API_PROVIDER AS API_PROVIDER,"
									+ " API.API_NAME AS API_NAME,"
									+ " API.API_VERSION AS API_VERSION" 
									+ " FROM AM_API AS API"
									+ " JOIN" 
									+ " (" 
										+ " SELECT AMSN.API_ID"
										+ " FROM AM_SUBSCRIPTION AS AMSN" 
										+ " JOIN" 
										+ " ("
												+ " SELECT AMA.APPLICATION_ID" 
												+ " FROM AM_SUBSCRIBER AS AMS"
												+ " JOIN AM_APPLICATION AS AMA"
												+ " ON AMS.SUBSCRIBER_ID = AMA.SUBSCRIBER_ID"
												+ " WHERE AMS.USER_ID=? AND AMA.NAME=?" 
										+ ") AS AppSubscriber"
										+ " ON AppSubscriber.APPLICATION_ID = AMSN.APPLICATION_ID"
					+ " )AS APIIds" 
					+ " ON APIIds.API_ID= API.API_ID";

			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, userName);
			ps.setString(2, appKey);
			ResultSet rs = ps.executeQuery();
			API api;
			while (rs.next()) {
				api = new API();
				api.setApiProvider(rs.getString("API_PROVIDER"));
				api.setApiName(rs.getString("API_NAME"));
				api.setApiVersion(rs.getString("API_VERSION"));
				apis.add(api);
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					log.error("Error while rolling back the failed operation", e);
				}
			}
			throw new AppFactoryException("Error occured while retriving basic API information "+e.getCause(),e);

		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				log.error("Error occured while connection close.");
			}
		}
		return apis;
	}

	/**
	 * Get the connection
	 * @return Connection
	 * @throws SQLException
	 */
	 private static Connection getConnection() throws SQLException {
	    return lookupDataSource().getConnection();
	 }
	 
	 /**
	  * Do a jndi lookup and find the Datasource for the given jndi name
	  * @return DataSource
	  */
	 private static DataSource lookupDataSource() {
        try {
            return (DataSource) InitialContext.doLookup(DATA_SOURCE_NAME);
        } catch (Exception e) {
            throw new RuntimeException("Error in looking up data source: " + DATA_SOURCE_NAME , e);
        }
    }
}
