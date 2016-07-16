package com.watchtogether.server.cloud.database.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Entity representing a document uploaded by a user and converted
 * 
 * @author Bogdan Solomon
 * 
 */
@Entity
@Table(name = "APPLICATION_USER_DOCUMENTS")
public class DocumentObject {

	@Transient
	private static final int hashCode = 17;
	@Transient
	private static final int oddMulti = 37;
	
	@Id
	@Column(name = "file_path")
	private String filePath = "";

	@Column(name = "file_description")
	private String fileDescription = "";

	@Column(name = "file_status")
	private String status = "";

	@Column(name = "user_id")
	private String uid = "";

	@Column(name = "privacy_view")
	@Enumerated(EnumType.ORDINAL)
	private DocumentPrivacy privacy;
	
	@Column(name = "width")
	private int width = -1;

	@Column(name = "height")
	private int height = -1;

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String fileName) {
		this.filePath = fileName;
	}

	public String getFileDescription() {
		return fileDescription;
	}

	public void setFileDescription(String fileDescription) {
		this.fileDescription = fileDescription;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public DocumentPrivacy getPrivacy() {
		return privacy;
	}

	public void setPrivacy(DocumentPrivacy privacy) {
		this.privacy = privacy;
	}
	
	@Override
	public String toString() {
		return this.filePath + "|" + this.fileDescription + "|" + this.uid
				+ "|" + this.height + "|" + this.width;
	}

	@Override
	public int hashCode() {
		int result = hashCode;
		result = oddMulti * result + filePath.hashCode();
		
		return result;
	}

	@Override
	public boolean equals(Object that) {
		if (that == null) {
			return false;
		}
		
		if (!(that instanceof DocumentObject)) {
			return false;
		}
		
		DocumentObject thatObj = (DocumentObject)that;
		
		return thatObj.getFilePath().equals(filePath);
	}
}
