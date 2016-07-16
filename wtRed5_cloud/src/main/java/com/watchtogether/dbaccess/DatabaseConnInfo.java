package com.watchtogether.dbaccess;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConnInfo {

	private static DatabaseConnInfo connInfo = null;
	private String jdbcDriver = "";
	private String stats_jdbcConnection = "";
	private String users_jdbcConnection = "";
	private String content_jdbcConnection = "";
	private String jdbcUser = "";

	public String getJdbcUser() {
		return jdbcUser;
	}

	public String getJdbcPassword() {
		return jdbcPassword;
	}

	private String jdbcPassword = "";
	
	public String getJdbcDriver() {
		return jdbcDriver;
	}

	public String getStatsJdbcConnection() {
		return stats_jdbcConnection;
	}
	
	public String getUsersJdbcConnection() {
		return users_jdbcConnection;
	}
	
	public String getContentJdbcConnection() {
		return content_jdbcConnection;
	}

	public DatabaseConnInfo() {
		Properties props = new Properties();
		try {
			InputStream propsStream = this.getClass().getClassLoader()
					.getResourceAsStream("db.props");
			props.load(propsStream);
			jdbcDriver = props.getProperty("JDBCDriver");
			stats_jdbcConnection = props.getProperty("stats_JDBCConnection");
			users_jdbcConnection = props.getProperty("users_JDBCConnection");
			content_jdbcConnection = props.getProperty("content_JDBCConnection");
			jdbcUser = props.getProperty("Username");
			jdbcPassword = props.getProperty("Password");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static DatabaseConnInfo getDBConnInfoSingleton() {
		if (connInfo == null)
			connInfo = new DatabaseConnInfo();

		return connInfo;
	}
	
}
