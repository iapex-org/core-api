package com.iapex.institution.DTO;


public class TokenDTO {
	
	    private String token;
	    private Long userId;
	    private boolean loggedOut;

	    public String getToken() { return token; }
	    public void setToken(String token) { this.token = token; }

	    public Long getUserId() { return userId; }
	    public void setUserId(Long userId) { this.userId = userId; }

	    public boolean isLoggedOut() { return loggedOut; }
	    public void setLoggedOut(boolean loggedOut) { this.loggedOut = loggedOut; }

		
	}

