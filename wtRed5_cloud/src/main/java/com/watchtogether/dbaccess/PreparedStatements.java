package com.watchtogether.dbaccess;

public class PreparedStatements {
	
	private static String userAddedPrepStatement = "INSERT IGNORE INTO APPLICATION_USERS "
		+ "(user_id) VALUES (?)";

	private static String userAddedActivityPrepStatement = "INSERT INTO APPLICATION_USERS_ACTIVITY "
			+ "(user_id, time_added, active) VALUES (?,?,true)";
	
	private static String userJoined = "INSERT INTO APPLICATION_USE "
		+ "(user_id, app_start_time, single_session_id) VALUES (? , ?, ?)";
	
	private static String userLeft = "UPDATE APPLICATION_USE "
		+ "SET app_end_time = ? WHERE user_id = ? AND single_session_id= ?";
	
	private static String createNewSession = "INSERT IGNORE INTO SESSIONS "
			+ "(session_id, session_start, client_ip, host_port, rt_latency) VALUES (?,?,?,?,?)";
	
	private static String destroySession = "UPDATE SESSIONS "
			+ "SET session_end = ? WHERE session_id = ?";
	
	private static String updateLatency = "UPDATE SESSIONS "
		+ "SET rt_latency = ? WHERE session_id = ?";

	private static String addSessionUser = "INSERT INTO SESSION_USERS "
		+ "(session_id, user_id, join_time) VALUES (?,?,?) "
		+ "ON DUPLICATE KEY UPDATE join_time = NOW()";
	
	private static String removeSessionUser = "UPDATE SESSION_USERS "
		+ "SET leave_time = ? WHERE  session_id =? AND user_id = ?";
	
	private static String addSessionAction = "INSERT INTO SESSION_ACTIONS "
		+ "(action_type, action_param, session_id, user_id) VALUES (?,?,?,?)";
	
	private static String addVideoInfo = "INSERT INTO YT_VIDEO "
		+ "(video_id, video_category, video_keywords, video_name) VALUES (?,?,?,?) "+
		" ON DUPLICATE KEY UPDATE video_category=? , video_keywords=? , video_name=?";

	private static String userInfoUpdate = "UPDATE APPLICATION_USERS "
		+ "SET age = ? , sex= ? , location =? "
		+" WHERE user_id = ?";
	
	private static String userNameUpdate = "INSERT INTO APPLICATION_USERS "
		+" (user_name, user_id) VALUES (?,?) "
		+" ON DUPLICATE KEY UPDATE user_name = ?";
	
	private static String userMediaAdd = "INSERT INTO APPLICATION_USER_CONFIG "
		+" (user_id, mediaApi) VALUES (?,?)";
	
	private static String userMediaRemove = "DELETE FROM APPLICATION_USER_CONFIG "
		+" WHERE user_id = ? AND mediaApi = ?";
	
	private static String userMediaSelect = "SELECT * FROM APPLICATION_USER_CONFIG "
		+" WHERE user_id = ?";
	
	private static String userDocumentAdd = "INSERT INTO APPLICATION_USER_DOCUMENTS "
		+" (user_id, file_path, file_description, file_status, width, height, privacy_view) VALUES (?,?,?,?,?,?,?) " 
		+" ON DUPLICATE KEY UPDATE file_status = ?, width= ?, height = ?";
	
	private static String userDocumentRemove = "DELETE FROM APPLICATION_USER_DOCUMENTS "
		+" WHERE file_path = ?";
	
	private static String userDocumentSelect = "SELECT d.user_id, d.file_path, " +
			"d.file_description, d.file_status, d.width, d.height " +
			"FROM APPLICATION_USER_DOCUMENTS d " +
			"WHERE d.user_id = ? and d.privacy_view = ?";

	private static String userDocumentSearch = "SELECT d.user_id, d.file_path, " +
			"d.file_description, d.file_status, d.width, d.height " +
			"FROM APPLICATION_USER_DOCUMENTS d "+ 
			"WHERE (d.file_path LIKE ? OR d.file_description LIKE ?) " +
			"and d.privacy_view = ?";
	
	public static String getUserDocumentAdd() {
		return userDocumentAdd;
	}

	public static String getUserDocumentRemove() {
		return userDocumentRemove;
	}

	public static String getUserDocumentSelect() {
		return userDocumentSelect;
	}

	public static String getUserDocumentSearch() {
		return userDocumentSearch;
	}

	public static String getUserMediaAdd() {
		return userMediaAdd;
	}

	public static String getUserMediaRemove() {
		return userMediaRemove;
	}

	public static String getUserMediaSelect() {
		return userMediaSelect;
	}

	public static String getCreateNewSession() {
		return createNewSession;
	}

	public static String getDestroySession() {
		return destroySession;
	}

	public static String getAddSessionUser() {
		return addSessionUser;
	}

	public static String getRemoveSessionUser() {
		return removeSessionUser;
	}

	public static String getAddSessionAction() {
		return addSessionAction;
	}

	public static String getUserLeft() {
		return userLeft;
	}

	public static String getUserJoined() {
		return userJoined;
	}

	public static String getAddVideoInfo() {
		return addVideoInfo;
	}

	public static String getUserInfoUpdate() {
		return userInfoUpdate;
	}

	public static String getUserNameUpdate() {
		return userNameUpdate;
	}
	
	public static String getUserAddedActivityPrepStatement() {
		return userAddedActivityPrepStatement;
	}
	
	
	public static String getUserAddedPrepStatement() {
		return userAddedPrepStatement;
	}

	public static String getUpdateLatency() {
		return updateLatency;
	}
}
