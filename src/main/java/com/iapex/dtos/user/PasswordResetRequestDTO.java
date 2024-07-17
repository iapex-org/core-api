package com.iapex.dtos.user;

public class PasswordResetRequestDTO {

	private String verificationCode;
	private String newPassword;

	// Getters and setters
	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
}