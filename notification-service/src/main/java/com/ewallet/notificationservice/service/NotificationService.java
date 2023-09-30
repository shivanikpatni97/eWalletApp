package com.ewallet.notificationservice.service;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NotificationService {

	private static final String TOPIC_TRANSACTION_COMPLETED = "TRANSACTION_COMPLETED";
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	JavaMailSenderImpl javaMailSender;
	
	@Autowired
	SimpleMailMessage simpleMailMessage;
	
	@KafkaListener(topics = TOPIC_TRANSACTION_COMPLETED, groupId = "notification_group")
	public void sendEmail(String message) throws JsonProcessingException {
		
		JSONObject notificationRequest = objectMapper.readValue(message, JSONObject.class);
		
		String userId = notificationRequest.get("userId").toString();
		String emailMessage = notificationRequest.get("emailMessage").toString();
		 
		JSONObject userObject = restTemplate.exchange("http://localhost:8085/user/" +userId,
				HttpMethod.GET,
				new HttpEntity<>(new HttpHeaders()),
				JSONObject.class).getBody();
		
		String email = userObject.get("email").toString();
		
		//pranav90111@gmail.com - payee
		//shivanikpatni@gmail.com - payer
		
		simpleMailMessage.setFrom(javaMailSender.getUsername());
		simpleMailMessage.setTo(email);
		simpleMailMessage.setSubject("Transaction Update from your love");
		simpleMailMessage.setText(emailMessage);
		
		javaMailSender.send(simpleMailMessage);
	}
}
