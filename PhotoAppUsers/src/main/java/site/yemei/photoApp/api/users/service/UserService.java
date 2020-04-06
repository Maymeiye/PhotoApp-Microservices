package site.yemei.photoApp.api.users.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import site.yemei.photoApp.api.users.shared.UserDto;

public interface UserService extends UserDetailsService{
	UserDto CreateUser(UserDto userDetails);
	UserDto getUserDetailsByEmail(String email);
	UserDto getUserByUserId(String userId);

}
