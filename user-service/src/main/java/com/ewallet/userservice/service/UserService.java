package com.ewallet.userservice.service;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.ewallet.userservice.models.User;
import com.ewallet.userservice.repository.UserCacheRepository;
import com.ewallet.userservice.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserService {

	private static final String TOPIC_CREATE_USER = "CREATE_USER";

	@Value("${useraccount.defaultbalance}")
	private int defaultBalance;
	
	@Autowired
	UserRepository userRepository;

	@Autowired 
	UserCacheRepository userCacheRepository;

	@Autowired
	KafkaTemplate<String, String> kafkaTemplate;
	
	@Autowired
	ObjectMapper objectMapper;

	public void createUser(User user) {

		//1. Create a user in db and store in cache
		user = userRepository.save(user);
		userCacheRepository.saveUserById(user);

		//2. create wallet for the new user
		try {
		JSONObject walletCreateRequest = new JSONObject();
		walletCreateRequest.put("userId", user.getUserId());
		walletCreateRequest.put("balance", defaultBalance);
		kafkaTemplate.send(TOPIC_CREATE_USER,user.getUserId(),objectMapper.writeValueAsString(walletCreateRequest));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	public User getUserByUserid(String userId) {

		User user = userCacheRepository.getUserById(userId);

		if(user == null) {
			user = userRepository.findByUserId(userId)
					.orElseThrow();
			
			userCacheRepository.saveUserById(user);
		}
		
		return user;
	}
}
