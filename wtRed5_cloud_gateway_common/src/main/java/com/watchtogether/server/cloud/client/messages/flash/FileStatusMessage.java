package com.watchtogether.server.cloud.client.messages.flash;

/**
 * Message notifying a client about a change in the status of a message
 * 
 * @author Bogdan Solomon
 * 
 */
public class FileStatusMessage implements IFlashMessage {

	private String fileName;
	private String status;
	private String width;
	private String height;
	private String stepPartValue;
	private String stepFullValue;
	private String step;
	private String allSteps;

	public FileStatusMessage(String fileName, String status, String width,
			String height, String stepPartValue, String stepFullValue,
			String step, String allSteps) {
		super();
		this.fileName = fileName;
		this.status = status;
		this.width = width;
		this.height = height;
		this.stepPartValue = stepPartValue;
		this.stepFullValue = stepFullValue;
		this.step = step;
		this.allSteps = allSteps;
	}

	@Override
	public String getClientMethodName() {
		return "notifyFileStatus";
	}

	public String getFileName() {
		return fileName;
	}

	public String getStatus() {
		return status;
	}

	public String getWidth() {
		return width;
	}

	public String getHeight() {
		return height;
	}

	public String getStepPartValue() {
		return stepPartValue;
	}

	public String getStepFullValue() {
		return stepFullValue;
	}

	public String getStep() {
		return step;
	}

	public String getAllSteps() {
		return allSteps;
	}

}
