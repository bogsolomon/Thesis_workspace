package com.watchtogether.dbaccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.scope.IScope;
import org.slf4j.Logger;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

public class DBConnInterface {
	
	private Logger logger = null;
	
	private DatabaseConnInfo info = null;
	
	private Connection statsconn = null;
	private Connection usersconn = null;
	private Connection contentconn = null;
	
	public DBConnInterface(IScope scope) {
		logger = Red5LoggerFactory.getLogger(DBConnInterface.class, scope.getContextPath());
		info = DatabaseConnInfo.getDBConnInfoSingleton();
		try {
			logger.info("Loading JDBC driver:"+info.getJdbcDriver());
			Class.forName(info.getJdbcDriver());
			statsconn = DriverManager.getConnection(info
					.getStatsJdbcConnection(), info.getJdbcUser(), info
					.getJdbcPassword());
			
			if (info.getStatsJdbcConnection().equals(info.getUsersJdbcConnection())) {
				usersconn = statsconn;
			} else {
				usersconn = DriverManager.getConnection(info
						.getUsersJdbcConnection(), info.getJdbcUser(), info
						.getJdbcPassword());
			}
			
			if (info.getStatsJdbcConnection().equals(info.getContentJdbcConnection())) {
				contentconn = statsconn;
			} else if (info.getUsersJdbcConnection().equals(info.getContentJdbcConnection())) {
				contentconn = usersconn;
			} else {
				contentconn = DriverManager.getConnection(info
						.getContentJdbcConnection(), info.getJdbcUser(), info
						.getJdbcPassword());
			}
			
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	private void userAddedApp(String user, Date time) {
		try {
			PreparedStatement addedAppPrepState = usersconn
					.prepareStatement(PreparedStatements
							.getUserAddedPrepStatement());
			addedAppPrepState.setString(1, user);
			addedAppPrepState.execute();
			
			addedAppPrepState = statsconn
					.prepareStatement(PreparedStatements
							.getUserAddedPrepStatement());
			addedAppPrepState.setString(1, user);
			addedAppPrepState.execute();
			
			addedAppPrepState = contentconn
					.prepareStatement(PreparedStatements
							.getUserAddedPrepStatement());
			addedAppPrepState.setString(1, user);
			addedAppPrepState.execute();
			
			addedAppPrepState = statsconn.prepareStatement(PreparedStatements
					.getUserAddedActivityPrepStatement());
			addedAppPrepState.setTimestamp(2, new java.sql.Timestamp(time
					.getTime()));
			addedAppPrepState.setString(1, user);
			addedAppPrepState.execute();
		} catch (SQLException sqlEx) {
			 String sqlState = sqlEx.getSQLState();
			 if (sqlState.startsWith("08")) {
				 logger.error("Got connection error, retrying");
				 userAddedAppRetry(user, time);
			 } else {
				 logger.error("Got database connection error which is unrecoverable");
				 sqlEx.printStackTrace();
			 }
		}
	}
	
	private void userAddedAppRetry(String user, Date time) {
		try {
			usersconn = DriverManager.getConnection(info
					.getUsersJdbcConnection(), info.getJdbcUser(), info
					.getJdbcPassword());
			
			PreparedStatement addedAppPrepState = usersconn
					.prepareStatement(PreparedStatements
							.getUserAddedPrepStatement());
			addedAppPrepState.setString(1, user);
			addedAppPrepState.execute();
			
			statsconn = DriverManager.getConnection(info
					.getStatsJdbcConnection(), info.getJdbcUser(), info
					.getJdbcPassword());
			
			addedAppPrepState = statsconn
					.prepareStatement(PreparedStatements
							.getUserAddedPrepStatement());
			addedAppPrepState.setString(1, user);
			addedAppPrepState.execute();
			
			contentconn = DriverManager.getConnection(info
					.getContentJdbcConnection(), info.getJdbcUser(), info
					.getJdbcPassword());
			
			addedAppPrepState = contentconn
					.prepareStatement(PreparedStatements
							.getUserAddedPrepStatement());
			addedAppPrepState.setString(1, user);
			addedAppPrepState.execute();
			
			addedAppPrepState = statsconn.prepareStatement(PreparedStatements
					.getUserAddedActivityPrepStatement());
			addedAppPrepState.setTimestamp(2, new java.sql.Timestamp(time
					.getTime()));
			addedAppPrepState.setString(1, user);
			addedAppPrepState.execute();
		} catch (SQLException sqlEx) {
			 logger.error("Got database connection error which is unrecoverable");
			 sqlEx.printStackTrace();
		}
	}

	public void userAddedMedia(String userId, String mediaApi) {
		try {
			userId = FacebookSigVerifier.hashUID(userId);
			
			PreparedStatement prepState = usersconn
				.prepareStatement(PreparedStatements.getUserMediaAdd());
			
			prepState.setString(1, userId);
			prepState.setString(2, mediaApi);
			prepState.execute();
		} catch (SQLException sqlEx) {
			 String sqlState = sqlEx.getSQLState();
			 if (sqlState.startsWith("08")) {
				 logger.error("Got connection error, retrying");
				 userAddedMediaRetry(userId, mediaApi);
			 } else if (sqlEx instanceof MySQLIntegrityConstraintViolationException){
				logger.error("Got constraint violation, adding user and retrying");
				userAddedApp(userId, new Date()); 
				userAddedMediaRetry(userId, mediaApi);
			 } else {
				 logger.error("Got database connection error which is unrecoverable");
				 sqlEx.printStackTrace();
			 }
		}
	}
	
	private void userAddedMediaRetry(String userId, String mediaApi) {
		try {
			usersconn = DriverManager.getConnection(info
					.getUsersJdbcConnection(), info.getJdbcUser(), info
					.getJdbcPassword());
			
			PreparedStatement prepState = usersconn
				.prepareStatement(PreparedStatements.getUserMediaAdd());
			
			prepState.setString(1, userId);
			prepState.setString(2, mediaApi);
			prepState.execute();
		}catch (SQLException sqlEx) {
			if (sqlEx instanceof MySQLIntegrityConstraintViolationException){
				logger.error("Got constraint violation, adding user and retrying");
				userAddedApp(userId, new Date()); 
				userAddedMediaRetry(userId, mediaApi);
			} else {
				logger.error("Got database connection error which is unrecoverable");
				sqlEx.printStackTrace();
			}
		}
	}
	
	public void userRemovedMedia(String userId, String mediaApi) {
		try {
			userId = FacebookSigVerifier.hashUID(userId);
			
			PreparedStatement prepState = usersconn
				.prepareStatement(PreparedStatements.getUserMediaRemove());
			
			prepState.setString(1, userId);
			prepState.setString(2, mediaApi);
			prepState.execute();
		} catch (SQLException sqlEx) {
			 String sqlState = sqlEx.getSQLState();
			 if (sqlState.startsWith("08")) {
				 logger.error("Got connection error, retrying");
				 userRemovedMediaRetry(userId, mediaApi);
			 } else if (sqlEx instanceof MySQLIntegrityConstraintViolationException){
				logger.error("Got constraint violation, adding user and retrying");
				userAddedApp(userId, new Date()); 
				userRemovedMediaRetry(userId, mediaApi);
			 } else {
				 logger.error("Got database connection error which is unrecoverable");
				 sqlEx.printStackTrace();
			 }
		}
	}
	
	private void userRemovedMediaRetry(String userId, String mediaApi) {
		try {
			usersconn = DriverManager.getConnection(info
					.getUsersJdbcConnection(), info.getJdbcUser(), info
					.getJdbcPassword());
			
			PreparedStatement prepState = usersconn
				.prepareStatement(PreparedStatements.getUserMediaRemove());
			
			prepState.setString(1, userId);
			prepState.setString(2, mediaApi);
			prepState.execute();
		}catch (SQLException sqlEx) {
			if (sqlEx instanceof MySQLIntegrityConstraintViolationException){
				logger.error("Got constraint violation, adding user and retrying");
				userAddedApp(userId, new Date()); 
				userAddedMediaRetry(userId, mediaApi);
			} else {
				logger.error("Got database connection error which is unrecoverable");
				sqlEx.printStackTrace();
			}
		}
	}
	
	public List<String> getUserMedia(String userId) {
		List<String> apis = new ArrayList<String>();
		
		try {
			userId = FacebookSigVerifier.hashUID(userId);
			
			PreparedStatement prepState = usersconn
				.prepareStatement(PreparedStatements.getUserMediaSelect());
			
			prepState.setString(1, userId);
			ResultSet rs = prepState.executeQuery();
			
			while (rs.next()) {
				apis.add(rs.getString(3));
			}
		} catch (SQLException sqlEx) {
			 String sqlState = sqlEx.getSQLState();
			 if (sqlState.startsWith("08")) {
				 logger.error("Got connection error, retrying");
				 apis = getUserMediaRetry(userId);
			 } else if (sqlEx instanceof MySQLIntegrityConstraintViolationException){
				logger.error("Got constraint violation, adding user and retrying");
				userAddedApp(userId, new Date()); 
				apis = getUserMediaRetry(userId);
			 } else {
				 logger.error("Got database connection error which is unrecoverable");
				 sqlEx.printStackTrace();
			 }
		}
		
		return apis;
	}
	
	private List<String> getUserMediaRetry(String userId) {
		List<String> apis = new ArrayList<String>();
		
		try {
			
			usersconn = DriverManager.getConnection(info
					.getUsersJdbcConnection(), info.getJdbcUser(), info
					.getJdbcPassword());
			
			PreparedStatement prepState = usersconn
				.prepareStatement(PreparedStatements.getUserMediaSelect());
			
			prepState.setString(1, userId);
			ResultSet rs = prepState.executeQuery();
			
			while (rs.next()) {
				apis.add(rs.getString(3));
			}
		}catch (SQLException sqlEx) {
			if (sqlEx instanceof MySQLIntegrityConstraintViolationException){
				logger.error("Got constraint violation, adding user and retrying");
				userAddedApp(userId, new Date()); 
				apis = getUserMediaRetry(userId);
			} else {
				logger.error("Got database connection error which is unrecoverable");
				sqlEx.printStackTrace();
			}
		}
		
		return apis;
	}
	
	public void userAddedDocument(String userId, String filePath, String fileDescription, String fileStatus,
			int privacy, int width, int height) {
		try {
			userId = FacebookSigVerifier.hashUID(userId);
			
			PreparedStatement prepState = contentconn
				.prepareStatement(PreparedStatements.getUserDocumentAdd());
			
			prepState.setString(1, userId);
			prepState.setString(2, filePath);
			prepState.setString(3, fileDescription);
			prepState.setString(4, fileStatus);
			prepState.setInt(5, width);
			prepState.setInt(6, height);
			prepState.setInt(7, privacy);
			prepState.setString(8, fileStatus);
			prepState.setInt(9, width);
			prepState.setInt(10, height);
			prepState.execute();
		} catch (SQLException sqlEx) {
			 String sqlState = sqlEx.getSQLState();
			 if (sqlState.startsWith("08")) {
				 logger.error("Got connection error, retrying");
				 userAddedDocumentRetry(userId, filePath, fileDescription, fileStatus, privacy, width, height);
			 } else if (sqlEx instanceof MySQLIntegrityConstraintViolationException){
				logger.error("Got constraint violation, adding user and retrying");
				userAddedApp(userId, new Date()); 
				userAddedDocumentRetry(userId, filePath, fileDescription, fileStatus, privacy, width, height);
			 } else {
				 logger.error("Got database connection error which is unrecoverable");
				 sqlEx.printStackTrace();
			 }
		}
	}
	
	private void userAddedDocumentRetry(String userId, String filePath, String fileDescription, String fileStatus,
			int privacy, int width, int height) {
		try {
			contentconn = DriverManager.getConnection(info
					.getContentJdbcConnection(), info.getJdbcUser(), info
					.getJdbcPassword());
			
			PreparedStatement prepState = contentconn
				.prepareStatement(PreparedStatements.getUserDocumentAdd());
		
			prepState.setString(1, userId);
			prepState.setString(2, filePath);
			prepState.setString(3, fileDescription);
			prepState.setString(4, fileStatus);
			prepState.setInt(5, width);
			prepState.setInt(6, height);
			prepState.setInt(7, privacy);
			prepState.setString(8, fileStatus);
			prepState.setInt(9, width);
			prepState.setInt(10, height);
			prepState.execute();
		}catch (SQLException sqlEx) {
			if (sqlEx instanceof MySQLIntegrityConstraintViolationException){
				logger.error("Got constraint violation, adding user and retrying");
				userAddedApp(userId, new Date()); 
				userAddedDocumentRetry(userId, filePath, fileDescription, fileStatus, privacy, width, height);
			} else {
				logger.error("Got database connection error which is unrecoverable");
				sqlEx.printStackTrace();
			}
		}
	}
	
	public void userRemovedDocument(String filePath) {
		try {
			PreparedStatement prepState = contentconn
				.prepareStatement(PreparedStatements.getUserDocumentRemove());
			
			prepState.setString(1, filePath);
			prepState.execute();
		} catch (SQLException sqlEx) {
			 String sqlState = sqlEx.getSQLState();
			 if (sqlState.startsWith("08")) {
				 logger.error("Got connection error, retrying");
				 userRemovedDocumentRetry(filePath);
			 } else {
				 logger.error("Got database connection error which is unrecoverable");
				 sqlEx.printStackTrace();
			 }
		}
	}
	
	public void userRemovedDocumentRetry(String filePath) {
		try {
			contentconn = DriverManager.getConnection(info
					.getContentJdbcConnection(), info.getJdbcUser(), info
					.getJdbcPassword());
			
			PreparedStatement prepState = contentconn
				.prepareStatement(PreparedStatements.getUserDocumentRemove());
			
			prepState.setString(1, filePath);
			prepState.execute();
		} catch (SQLException sqlEx) {
			 logger.error("Got database connection error which is unrecoverable");
			 sqlEx.printStackTrace(); 
		}
	}
	
	public List<DocumentObject> getUserDocuments(String userId, int privacy) {
		List<DocumentObject> docs = new ArrayList<DocumentObject>();
		
		try {
			userId = FacebookSigVerifier.hashUID(userId);
			
			PreparedStatement prepState = contentconn
				.prepareStatement(PreparedStatements.getUserDocumentSelect());
			
			prepState.setString(1, userId);
			prepState.setInt(2, privacy);
			ResultSet rs = prepState.executeQuery();
			
			while (rs.next()) {
				DocumentObject doc = new DocumentObject();
				doc.setUid(rs.getString(1));
				doc.setFileName(rs.getString(2));
				doc.setFileDescription(rs.getString(3));
				doc.setStatus(rs.getString(4));
				doc.setWidth(rs.getInt(5));
				doc.setHeight(rs.getInt(6));
				docs.add(doc);
			}
		} catch (SQLException sqlEx) {
			 String sqlState = sqlEx.getSQLState();
			 if (sqlState.startsWith("08")) {
				 logger.error("Got connection error, retrying");
				 docs = getUserDocumentsRetry(userId, privacy);
			 } else {
				 logger.error("Got database connection error which is unrecoverable");
				 sqlEx.printStackTrace();
			 }
		}
		
		return docs;
	}
	
	public List<DocumentObject> searchUserDocuments(String searchStr) {
		List<DocumentObject> docs = new ArrayList<DocumentObject>();
		
		try {
			contentconn = DriverManager.getConnection(info
					.getContentJdbcConnection(), info.getJdbcUser(), info
					.getJdbcPassword());
			
			PreparedStatement prepState = contentconn
				.prepareStatement(PreparedStatements.getUserDocumentSearch());
			
			prepState.setString(1, "%"+searchStr+"%");
			prepState.setString(2, "%"+searchStr+"%");
			prepState.setInt(3, 0);
			ResultSet rs = prepState.executeQuery();
			
			while (rs.next()) {
				DocumentObject doc = new DocumentObject();
				doc.setUid(rs.getString(1));
				doc.setFileName(rs.getString(2));
				doc.setFileDescription(rs.getString(3));
				doc.setStatus(rs.getString(4));
				doc.setWidth(rs.getInt(5));
				doc.setHeight(rs.getInt(6));
				docs.add(doc);
			}
		} catch (SQLException sqlEx) {
			 String sqlState = sqlEx.getSQLState();
			 if (sqlState.startsWith("08")) {
				 logger.error("Got connection error, retrying");
				 docs = searchUserDocumentsRetry(searchStr);
			 } else {
				 logger.error("Got database connection error which is unrecoverable");
				 sqlEx.printStackTrace();
			 }
		}
		
		return docs;
	}
	
	public List<DocumentObject> searchUserDocumentsRetry(String searchStr) {
		List<DocumentObject> docs = new ArrayList<DocumentObject>();
		
		try {
			PreparedStatement prepState = contentconn
				.prepareStatement(PreparedStatements.getUserDocumentSearch());
			
			prepState.setString(1, "%"+searchStr+"%");
			prepState.setString(2, "%"+searchStr+"%");
			prepState.setInt(3, 0);
			ResultSet rs = prepState.executeQuery();
			
			while (rs.next()) {
				DocumentObject doc = new DocumentObject();
				doc.setUid(rs.getString(1));
				doc.setFileName(rs.getString(2));
				doc.setFileDescription(rs.getString(3));
				doc.setStatus(rs.getString(4));
				doc.setWidth(rs.getInt(5));
				doc.setHeight(rs.getInt(6));
				docs.add(doc);
			}
		} catch (SQLException sqlEx) {
			 logger.error("Got database connection error which is unrecoverable");
			 sqlEx.printStackTrace(); 
		}
		
		return docs;
	}
	
	public List<DocumentObject> getUserDocumentsRetry(String userId, int privacy) {
		List<DocumentObject> docs = new ArrayList<DocumentObject>();
		
		try {
			contentconn = DriverManager.getConnection(info
					.getContentJdbcConnection(), info.getJdbcUser(), info
					.getJdbcPassword());
			
			userId = FacebookSigVerifier.hashUID(userId);
			
			PreparedStatement prepState = contentconn
				.prepareStatement(PreparedStatements.getUserDocumentSelect());
			
			prepState.setString(1, userId);
			prepState.setInt(2, privacy);
			ResultSet rs = prepState.executeQuery();
			
			while (rs.next()) {
				DocumentObject doc = new DocumentObject();
				doc.setUid(rs.getString(1));
				doc.setFileName(rs.getString(2));
				doc.setFileDescription(rs.getString(3));
				doc.setStatus(rs.getString(4));
				doc.setWidth(rs.getInt(5));
				doc.setHeight(rs.getInt(6));
				docs.add(doc);
			}
		} catch (SQLException sqlEx) {
			 logger.error("Got database connection error which is unrecoverable");
			 sqlEx.printStackTrace();
		}
		
		return docs;
	}
	
	public String userStartedApp(String userId, Date time, String ip, long rtDelay, String hostPort) {
		try {
			
			userId = FacebookSigVerifier.hashUID(userId);
			
			PreparedStatement prepState = statsconn
					.prepareStatement(PreparedStatements.getUserJoined());
			prepState.setTimestamp(2, new java.sql.Timestamp(time.getTime()));
			prepState.setString(1, userId);
			
			String sessionId = "SINGLE@"+UUID.randomUUID().toString();
			
			prepState.setString(3, sessionId);
			prepState.execute();
			
			prepState = statsconn
				.prepareStatement(PreparedStatements.getCreateNewSession());
			prepState.setTimestamp(2, new java.sql.Timestamp(time.getTime()));
			prepState.setString(1, sessionId);
			prepState.setString(3, ip);
			prepState.setString(4, hostPort);
			prepState.setLong(5, rtDelay);
			prepState.execute();
			return sessionId;
		} catch (SQLException sqlEx) {
			 String sqlState = sqlEx.getSQLState();
			 if (sqlState.startsWith("08")) {
				 logger.error("Got connection error, retrying");
				 return userStartedAppRetry(userId,time, ip, rtDelay, hostPort);
			 } else if (sqlEx instanceof MySQLIntegrityConstraintViolationException){
				logger.error("Got constraint violation, adding user and retrying");
				userAddedApp(userId, time); 
				return userStartedAppRetry(userId,time, ip, rtDelay, hostPort);
			 } else {
				 logger.error("Got database connection error which is unrecoverable");
				 sqlEx.printStackTrace();
			 }
		}
		
		return null;
	}
	
	public String userStartedAppRetry(String userId, Date time, String ip, long rtDelay, String hostPort) {
		try {
			statsconn = DriverManager.getConnection(info
					.getStatsJdbcConnection(), info.getJdbcUser(), info
					.getJdbcPassword());
			
			PreparedStatement prepState = statsconn
					.prepareStatement(PreparedStatements.getUserJoined());
			prepState.setTimestamp(2, new java.sql.Timestamp(time.getTime()));
			prepState.setString(1, userId);
			
			String sessionId = "SINGLE@"+UUID.randomUUID().toString();
			
			prepState.setString(3, sessionId);
			prepState.execute();
			
			prepState = statsconn
				.prepareStatement(PreparedStatements.getCreateNewSession());
			prepState.setTimestamp(2, new java.sql.Timestamp(time.getTime()));
			prepState.setString(1, sessionId);
			prepState.setString(3, ip);
			prepState.setString(4, hostPort);
			prepState.setLong(5, rtDelay);
			prepState.execute();
			return sessionId;
		} catch (SQLException sqlEx) {
			if (sqlEx instanceof MySQLIntegrityConstraintViolationException){
				logger.error("Got constraint violation, adding user and retrying");
				userAddedApp(userId, time); 
				return userStartedAppRetry(userId,time, ip, rtDelay, hostPort);
			} else {
				logger.error("Got database connection error which is unrecoverable");
				sqlEx.printStackTrace();
			}
		}
		
		return null;
	}
	
	public void userClosedApp(String userId, Date time,String sessionId) {
		try {
			userId = FacebookSigVerifier.hashUID(userId);
			
			PreparedStatement prepState = statsconn
					.prepareStatement(PreparedStatements.getUserLeft());
			prepState.setTimestamp(1, new java.sql.Timestamp(time.getTime()));
			prepState.setString(2, userId);
			prepState.setString(3, sessionId);
			prepState.execute();
			
			prepState = statsconn
					.prepareStatement(PreparedStatements.getDestroySession());
			prepState.setTimestamp(1, new java.sql.Timestamp(time.getTime()));
			prepState.setString(2, sessionId);
			prepState.execute();
		} catch (SQLException sqlEx) {
			 String sqlState = sqlEx.getSQLState();
			 if (sqlState.startsWith("08")) {
				 logger.error("Got connection error, retrying");
				 userClosedAppRetry(userId,time,sessionId);
			 }else {
				 logger.error("Got database connection error which is unrecoverable");
				 sqlEx.printStackTrace();
			 }
		}
	}
	
	public void userClosedAppRetry(String userId, Date time,String sessionId) {
		try {
			statsconn = DriverManager.getConnection(info
					.getStatsJdbcConnection(), info.getJdbcUser(), info
					.getJdbcPassword());
			
			PreparedStatement prepState = statsconn
					.prepareStatement(PreparedStatements.getUserLeft());
			prepState.setTimestamp(1, new java.sql.Timestamp(time.getTime()));
			prepState.setString(2, userId);
			prepState.setString(3, sessionId);
			prepState.execute();
			
			prepState = statsconn
					.prepareStatement(PreparedStatements.getDestroySession());
			prepState.setTimestamp(1, new java.sql.Timestamp(time.getTime()));
			prepState.setString(2, sessionId);
			prepState.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void createdNewSession(String sessionId, Date time, String userId) {
		try {
			userId = FacebookSigVerifier.hashUID(userId);
			
			PreparedStatement prepState = statsconn
					.prepareStatement(PreparedStatements.getCreateNewSession());
			prepState.setTimestamp(2, new java.sql.Timestamp(time.getTime()));
			prepState.setString(1, sessionId);
			prepState.setString(3, "");
			prepState.setString(4, "");
			prepState.setLong(5, 0);
			prepState.execute();
			
			prepState = statsconn.prepareStatement(PreparedStatements.getAddSessionUser());
			prepState.setString(1, sessionId);
			prepState.setString(2, userId);
			prepState.setTimestamp(3, new java.sql.Timestamp(time.getTime()));
			prepState.execute();
		} catch (SQLException sqlEx) {
			 String sqlState = sqlEx.getSQLState();
			 if (sqlState.startsWith("08")) {
				 logger.error("Got connection error, retrying");
				 createdNewSessionRetry(sessionId,time,userId);
			 }else {
				 logger.error("Got database connection error which is unrecoverable");
				 sqlEx.printStackTrace();
			 }
		}
	}
	
	public void createdNewSessionRetry(String sessionId, Date time, String userId) {
		try {
			statsconn = DriverManager.getConnection(info
					.getStatsJdbcConnection(), info.getJdbcUser(), info
					.getJdbcPassword());
			
			PreparedStatement prepState = statsconn
					.prepareStatement(PreparedStatements.getCreateNewSession());
			prepState.setTimestamp(2, new java.sql.Timestamp(time.getTime()));
			prepState.setString(1, sessionId);
			prepState.setString(3, "");
			prepState.setString(4, "");
			prepState.setLong(5, 0);
			prepState.execute();
			
			prepState = statsconn.prepareStatement(PreparedStatements.getAddSessionUser());
			prepState.setString(1, sessionId);
			prepState.setString(2, userId);
			prepState.setTimestamp(3, new java.sql.Timestamp(time.getTime()));
			prepState.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void destroySession(String sessionId, Date time,  String userId) {
		try {
			userId = FacebookSigVerifier.hashUID(userId);
			
			PreparedStatement prepState = statsconn
					.prepareStatement(PreparedStatements.getDestroySession());
			prepState.setTimestamp(1, new java.sql.Timestamp(time.getTime()));
			prepState.setString(2, sessionId);
			prepState.execute();
			
			prepState = statsconn.prepareStatement(PreparedStatements.getRemoveSessionUser());
			prepState.setString(2, sessionId);
			prepState.setString(3, userId);
			prepState.setTimestamp(1, new java.sql.Timestamp(time.getTime()));
			prepState.execute();
		} catch (SQLException sqlEx) {
			 String sqlState = sqlEx.getSQLState();
			 if (sqlState.startsWith("08")) {
				 logger.error("Got connection error, retrying");
				 destroySessionRetry(sessionId,time,userId);
			 }else {
				 logger.error("Got database connection error which is unrecoverable");
				 sqlEx.printStackTrace();
			 }
		}
	}
	
	public void destroySessionRetry(String sessionId, Date time,  String userId) {
		try {
			statsconn = DriverManager.getConnection(info
					.getStatsJdbcConnection(), info.getJdbcUser(), info
					.getJdbcPassword());
			
			PreparedStatement prepState = statsconn
					.prepareStatement(PreparedStatements.getDestroySession());
			prepState.setTimestamp(1, new java.sql.Timestamp(time.getTime()));
			prepState.setString(2, sessionId);
			prepState.execute();
			
			prepState = statsconn.prepareStatement(PreparedStatements.getRemoveSessionUser());
			prepState.setString(2, sessionId);
			prepState.setString(3, userId);
			prepState.setTimestamp(1, new java.sql.Timestamp(time.getTime()));
			prepState.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void updateLatency(String sessionId, long rtLatency) {
		try {
			PreparedStatement prepState = statsconn
					.prepareStatement(PreparedStatements.getUpdateLatency());
			prepState.setLong(1, rtLatency);
			prepState.setString(2, sessionId);
			prepState.execute();
		} catch (SQLException sqlEx) {
			 String sqlState = sqlEx.getSQLState();
			 if (sqlState.startsWith("08")) {
				 logger.error("Got connection error, retrying");
				 updateLatencyRetry(sessionId,rtLatency);
			 }else {
				 logger.error("Got database connection error which is unrecoverable");
				 sqlEx.printStackTrace();
			 }
		}
	}
	
	public void updateLatencyRetry(String sessionId, long rtLatency) {
		try {
			statsconn = DriverManager.getConnection(info
					.getStatsJdbcConnection(), info.getJdbcUser(), info
					.getJdbcPassword());
			
			PreparedStatement prepState = statsconn
					.prepareStatement(PreparedStatements.getUpdateLatency());
			prepState.setLong(1, rtLatency);
			prepState.setString(2, sessionId);
			prepState.execute();
		} catch (SQLException sqlEx) {
			sqlEx.printStackTrace();
		}
	}
	
	public void addSessionUser(String sessionId, Date time, String userId) {
		try {
			userId = FacebookSigVerifier.hashUID(userId);
			
			PreparedStatement prepState = statsconn.prepareStatement(PreparedStatements.getAddSessionUser());
			prepState.setString(1, sessionId);
			prepState.setString(2, userId);
			prepState.setTimestamp(3, new java.sql.Timestamp(time.getTime()));
			prepState.execute();
		} catch (SQLException sqlEx) {
			 String sqlState = sqlEx.getSQLState();
			 if (sqlState.startsWith("08")) {
				 logger.error("Got connection error, retrying");
				 addSessionUserRetry(sessionId,time,userId);
			 } else {
				 logger.error("Got database connection error which is unrecoverable");
				 sqlEx.printStackTrace();
			 }
		}
	}
	
	public void addSessionUserRetry(String sessionId, Date time, String userId) {
		try {
			statsconn = DriverManager.getConnection(info
					.getStatsJdbcConnection(), info.getJdbcUser(), info
					.getJdbcPassword());
			
			PreparedStatement prepState = statsconn.prepareStatement(PreparedStatements.getAddSessionUser());
			prepState.setString(1, sessionId);
			prepState.setString(2, userId);
			prepState.setTimestamp(3, new java.sql.Timestamp(time.getTime()));
			prepState.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void removeSessionUser(String sessionId, Date time, String userId) {
		try {
			userId = FacebookSigVerifier.hashUID(userId);
			
			PreparedStatement prepState = statsconn.prepareStatement(PreparedStatements.getRemoveSessionUser());
			prepState.setString(2, sessionId);
			prepState.setString(3, userId);
			prepState.setTimestamp(1, new java.sql.Timestamp(time.getTime()));
			prepState.execute();
		} catch (SQLException sqlEx) {
			 String sqlState = sqlEx.getSQLState();
			 if (sqlState.startsWith("08")) {
				 logger.error("Got connection error, retrying");
				 removeSessionUserRetry(sessionId,time,userId);
			 } else {
				 logger.error("Got database connection error which is unrecoverable");
				 sqlEx.printStackTrace();
			 }
		}
	}
	
	public void removeSessionUserRetry(String sessionId, Date time, String userId) {
		try {
			statsconn = DriverManager.getConnection(info
					.getStatsJdbcConnection(), info.getJdbcUser(), info
					.getJdbcPassword());
			
			PreparedStatement prepState = statsconn.prepareStatement(PreparedStatements.getRemoveSessionUser());
			prepState.setString(2, sessionId);
			prepState.setString(3, userId);
			prepState.setTimestamp(1, new java.sql.Timestamp(time.getTime()));
			prepState.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void actionPerformed(String reason, String actionParam, String sessionId, String userId) {
		try {
			userId = FacebookSigVerifier.hashUID(userId);
			
			if (actionParam.length() > 200)
				actionParam = actionParam.substring(0,199);
			
			PreparedStatement prepState = statsconn.prepareStatement(PreparedStatements.getAddSessionAction());
			prepState.setString(1, reason);
			prepState.setString(2, actionParam);
			prepState.setString(3, sessionId);
			prepState.setString(4, userId);
			prepState.execute();
		} catch (SQLException sqlEx) {
			 String sqlState = sqlEx.getSQLState();
			 if (sqlState.startsWith("08")) {
				 logger.error("Got connection error, retrying");
				 actionPerformedRetry(reason,actionParam,sessionId,userId);
			 } else {
				 logger.error("Got database connection error which is unrecoverable");
				 sqlEx.printStackTrace();
			 }
		}
	}
	
	public void actionPerformedRetry(String reason, String actionParam, String sessionId, String userId) {
		try {
			statsconn = DriverManager.getConnection(info
					.getStatsJdbcConnection(), info.getJdbcUser(), info
					.getJdbcPassword());
			
			PreparedStatement prepState = statsconn.prepareStatement(PreparedStatements.getAddSessionAction());
			prepState.setString(1, reason);
			prepState.setString(2, actionParam);
			prepState.setString(3, sessionId);
			prepState.setString(4, userId);
			prepState.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/*public void saveVideoInfo(String videoId, String videoCateg, String videoKeywords, String videoName) {
		try {
			//truncate keywords to 200 characters
			if (videoKeywords.length() > 200)
				videoKeywords = videoKeywords.substring(0,199);
			
			PreparedStatement prepState = conn.prepareStatement(PreparedStatements.getAddVideoInfo());
			prepState.setString(1, videoId);
			prepState.setString(2, videoCateg);
			prepState.setString(3, videoKeywords);
			prepState.setString(4, videoName);
			prepState.setString(5, videoCateg);
			prepState.setString(6, videoKeywords);
			prepState.setString(7, videoName);
			prepState.execute();
		} catch (SQLException sqlEx) {
			 String sqlState = sqlEx.getSQLState();
			 if (sqlState.startsWith("08")) {
				 logger.error("Got connection error, retrying");
				 saveVideoInfoRetry(videoId,videoCateg,videoKeywords,videoName);
			 } else {
				 logger.error("Got database connection error which is unrecoverable");
				 sqlEx.printStackTrace();
			 }
		}
	}
	
	public void saveVideoInfoRetry(String videoId, String videoCateg, String videoKeywords, String videoName) {
		try {
			conn = DriverManager.getConnection(info
					.getJdbcConnection(), info.getJdbcUser(), info
					.getJdbcPassword());
			
			PreparedStatement prepState = conn.prepareStatement(PreparedStatements.getAddVideoInfo());
			prepState.setString(1, videoId);
			prepState.setString(2, videoCateg);
			prepState.setString(3, videoKeywords);
			prepState.setString(4, videoName);
			prepState.setString(5, videoCateg);
			prepState.setString(6, videoKeywords);
			prepState.setString(7, videoName);
			prepState.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}*/
	
	public void updateUserMetrics(String uid, SexEnum sex, Integer age, String location, String name) {
		try {
			uid = FacebookSigVerifier.hashUID(uid);
			PreparedStatement prepState = usersconn.prepareStatement(PreparedStatements.getUserInfoUpdate());
			prepState.setInt(1, age);
			prepState.setInt(2, sex.ordinal());
			prepState.setString(3, location);
			prepState.setString(4, uid);
			prepState.execute();
			
			prepState = usersconn.prepareStatement(PreparedStatements.getUserNameUpdate());
			prepState.setString(1, name);
			prepState.setString(2, uid);
			prepState.setString(3, name);
			prepState.execute();
		} catch (SQLException sqlEx) {
			 String sqlState = sqlEx.getSQLState();
			 if (sqlState.startsWith("08")) {
				 logger.error("Got connection error, retrying");
				 updateUserMetricsRetry(uid,sex,age,location,name);
			 } else if (sqlEx instanceof MySQLIntegrityConstraintViolationException){
				logger.error("Got constraint violation, adding user and retrying");
				userAddedApp(uid, new Date()); 
				updateUserMetricsRetry(uid,sex,age,location,name);
			 } else {
				 logger.error("Got database connection error which is unrecoverable");
				 sqlEx.printStackTrace();
			 }
		}
	}
	
	public void updateUserMetricsRetry(String uid, SexEnum sex, Integer age, String location, String name) {
		try {
			usersconn = DriverManager.getConnection(info
					.getUsersJdbcConnection(), info.getJdbcUser(), info
					.getJdbcPassword());
						
			PreparedStatement prepState = usersconn.prepareStatement(PreparedStatements.getUserInfoUpdate());
			prepState.setInt(1, age);
			prepState.setInt(2, sex.ordinal());
			prepState.setString(3, location);
			prepState.setString(4, uid);
			prepState.execute();
			
			prepState = usersconn.prepareStatement(PreparedStatements.getUserNameUpdate());
			prepState.setString(1, name);
			prepState.setString(2, uid);
			prepState.setString(3, name);
			prepState.execute();
		} catch (SQLException sqlEx) {
			if (sqlEx instanceof MySQLIntegrityConstraintViolationException){
				logger.error("Got constraint violation, adding user and retrying");
				userAddedApp(uid, new Date()); 
				updateUserMetricsRetry(uid,sex,age,location,name);
			 } else {
				 logger.error("Got database connection error which is unrecoverable");
				 sqlEx.printStackTrace();
			 }
		}
	}
	
}
