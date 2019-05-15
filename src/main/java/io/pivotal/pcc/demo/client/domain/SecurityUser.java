package io.pivotal.pcc.demo.client.domain;

import java.util.Arrays;

public class SecurityUser {

	private String username;
	private String password;
	private String[] roles;

	public SecurityUser(String username, String password, Object[] roles) {
		this.username = username;
		this.password = password;
		this.roles = Arrays.copyOf(roles, roles.length, String[].class);
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String[] getRoles() {
		return roles;
	}

	@Override public String toString() {
		return "User{" +
			"username='" + username + '\'' +
			", password='" + password + '\'' +
			", roles=" + Arrays.toString(roles) +
			'}';
	}
}
