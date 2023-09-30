package com.ewallet.transactionservice.service;

import java.util.UUID;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.ewallet.transactionservice.models.Transaction;
import com.ewallet.transactionservice.models.TransactionStatus;
import com.ewallet.transactionservice.repository.TransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TransactionService {

	private static final String TOPIC_TRANSACTION_INITIATED = "TRANSACTION_INITIATED";
	
	private static final String TOPIC_WALLET_UPDATED = "WALLET_UPDATED";
	
	private static final String TOPIC_TRANSACTION_COMPLETED = "TRANSACTION_COMPLETED";
	
	@Autowired
	KafkaTemplate<String, String> kafkaTemplate;
	
	@Autowired
	TransactionRepository transactionRepository;
	
	@Autowired
	ObjectMapper objectMapper;
	
	public void createTransaction(Transaction transaction) throws JsonProcessingException {
		
		Transaction newTransaction = new Transaction();
		newTransaction.setPayerId(transaction.getPayerId());
		newTransaction.setPayeeId(transaction.getPayeeId());
		newTransaction.setAmount(transaction.getAmount());
		newTransaction.setPurpose(transaction.getPurpose());
		newTransaction.setTransactionId(UUID.randomUUID().toString());
		newTransaction.setTransactionStatus(TransactionStatus.PENDING);
		
		newTransaction = transactionRepository.save(newTransaction);
		
		JSONObject walletUpdateRequest = new JSONObject();
		
		walletUpdateRequest.put("payerId", newTransaction.getPayerId());
		walletUpdateRequest.put("payeeId", newTransaction.getPayeeId());
		walletUpdateRequest.put("amount", newTransaction.getAmount());
		walletUpdateRequest.put("transactionId", newTransaction.getTransactionId());
		
		kafkaTemplate.send(TOPIC_TRANSACTION_INITIATED,newTransaction.getTransactionId()
				,objectMapper.writeValueAsString(walletUpdateRequest));
	}
	
	@KafkaListener(topics = TOPIC_WALLET_UPDATED, groupId = "transaction_group")
	public void transactionCompleted(String message) throws  JsonProcessingException {
		
		JSONObject transactionCompleteRequest = objectMapper.readValue(message, JSONObject.class);
		
		String transactionId = transactionCompleteRequest.get("transactionId").toString();
		String transactionStatus = transactionCompleteRequest.get("transactionStatus").toString();
		
		//1.Update my transaction with given transaction status
		transactionRepository.updateTransactionStatus(transactionId, TransactionStatus.valueOf(transactionStatus));
		
		//2. send a message to notification service
		Transaction updatedTransaction = transactionRepository.findByTransactionId(transactionId)
				.orElseThrow();
		
		String payerId = updatedTransaction.getPayerId();
		String payeeId = updatedTransaction.getPayeeId();
		int amount = updatedTransaction.getAmount();
		
		JSONObject payerNotificationRequest = new JSONObject();
		payerNotificationRequest.put("userId", payerId);
		payerNotificationRequest.put("emailMessage", String.format("Hey %s, your transfer of amount %d with transaction id %s is %s", payerId,amount, transactionId, transactionStatus));
		
		kafkaTemplate.send(TOPIC_TRANSACTION_COMPLETED, objectMapper.writeValueAsString(payerNotificationRequest));
		
		if(transactionStatus.equals("SUCCESS")) {
			JSONObject payeeNotificationRequest = new JSONObject();
			
			payeeNotificationRequest.put("userId", payeeId);
			payeeNotificationRequest.put("emailMessage", String.format("Hey %s, you have received an amount of %d with transaction id %s from %s, bass ab Shivani ko duao me yaad rakhna!", payeeId,amount, transactionId,payerId));
		
			kafkaTemplate.send(TOPIC_TRANSACTION_COMPLETED,objectMapper.writeValueAsString(payeeNotificationRequest));
		}
	}
}
