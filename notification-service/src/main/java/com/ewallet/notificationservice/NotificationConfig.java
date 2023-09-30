package com.ewallet.notificationservice;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableKafka
public class NotificationConfig {

//	@Value("${mail.transport.protocol}")
//    private String protocol;
//
//	@Value("${mail.from.email}")
//    private String from;
	
	@Bean
	ConsumerFactory<String, String> getConsumerFactory(){
		Map<String,Object> properties = new HashMap<>();

		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

		return new DefaultKafkaConsumerFactory<>(properties);
	}

	@Bean
	ConcurrentKafkaListenerContainerFactory<String,String> concurrentKafkaListenerContainerFactory(){
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(getConsumerFactory());
		return factory;
	}

	@Bean
	ObjectMapper getObjectMapper() {
		return new ObjectMapper();
	}
	
	@Bean
	RestTemplate restTemplate(){
		return new RestTemplate();
	}
	
	@Bean
	JavaMailSenderImpl javaMailSender() {
		JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
		javaMailSender.setHost("smtp.gmail.com");
		//smtp.mail.yahoo.com
		javaMailSender.setPort(587);
		javaMailSender.setUsername("shivaniewallet@gmail.com");
		javaMailSender.setPassword("fbzl eeed kfuo jnhl");
		
		Properties properties = javaMailSender.getJavaMailProperties();
		properties.put("mail.smtp.starttls.enable", true);
//		properties.put("mail.transport.protocol", protocol);
//		properties.put("mail.smtp.auth", "true");
//		properties.put("mail.from.email", from);
//		properties.put("mail.debug", "true");
		
//		Authenticator auth = new Authenticator() {
//            public PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication("shivanipatniwork@gmail.com", "Qazxcv!23");
//            }
//        };
//        
//        Session session = Session.getInstance(properties, auth);
//        javaMailSender.setSession(session);
        
		return javaMailSender;
	}
	
	@Bean
	SimpleMailMessage simpleMailMessage() {
		return new SimpleMailMessage();
	}
}
