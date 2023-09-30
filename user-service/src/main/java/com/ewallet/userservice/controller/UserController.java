package com.ewallet.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ewallet.userservice.models.User;
import com.ewallet.userservice.service.UserService;

@RestController
public class UserController {

	@Autowired
	UserService userService;

	@PostMapping("/user")
	public void creteNewUser(@RequestBody User user) {
		userService.createUser(user);
	}

	@GetMapping("/user/{userId}")
	public User getUser(@PathVariable String userId) {
		return userService.getUserByUserid(userId);
	}
}
