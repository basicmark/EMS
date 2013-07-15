package io.github.basicmark.ems;

public class EMSChatResponse {
	private boolean cancel;
	private String message;

	public EMSChatResponse(boolean cancel, String message) {
		this.cancel = cancel;
		this.message = message;
	}
	
	public boolean isCanceled() {
		return cancel;
	}
	
	public String getMessage() {
		return message;
	}
}
