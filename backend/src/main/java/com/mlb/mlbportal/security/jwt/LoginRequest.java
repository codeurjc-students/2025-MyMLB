package com.mlb.mlbportal.security.jwt;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

	@NotBlank(message= "The username is required")
	private String username;

	@NotBlank(message= "The password is required")
	private String password;

	public LoginRequest() {}

	public LoginRequest(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public String toString() {
		return "LoginRequest [username=" + username + ", password=" + password + "]";
	}
}