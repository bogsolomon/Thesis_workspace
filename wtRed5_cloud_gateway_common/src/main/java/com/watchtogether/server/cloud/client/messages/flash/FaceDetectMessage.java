package com.watchtogether.server.cloud.client.messages.flash;

public class FaceDetectMessage implements IFlashMessage {

	private int detectValue;
	
	public FaceDetectMessage(int detectValue) {
		this.detectValue = detectValue;
	}

	@Override
	public String getClientMethodName() {
		return "notifyFaceDetect";
	}

	public int getDetectValue() {
		return detectValue;
	}

}
