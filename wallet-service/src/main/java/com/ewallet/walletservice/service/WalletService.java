package com.ewallet.walletservice.service;

import java.util.Arrays;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.ewallet.walletservice.models.Wallet;
import com.ewallet.walletservice.repository.WalletCacheRepository;
import com.ewallet.walletservice.repository.WalletRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WalletService {

	private static final String TOPIC_CREATE_USER = "CREATE_USER";

	private static final String TOPIC_TRANSACTION_INITIATED = "TRANSACTION_INITIATED";

	private static final String TOPIC_WALLET_UPDATED = "WALLET_UPDATED";

	@Autowired
	KafkaTemplate<String, String> kafkaTemplate;

	@Autowired
	WalletRepository walletRepository;

	@Autowired
	WalletCacheRepository walletCacheRepository;

	@Autowired
	ObjectMapper objectMapper;

	@KafkaListener(topics=TOPIC_CREATE_USER,groupId = "wallet_group")
	public void createWallet(String message) throws JsonProcessingException {

		JSONObject walletCreateRequest = objectMapper.readValue(message, JSONObject.class);

		String userId = walletCreateRequest.get("userId").toString();
		int balance = (Integer)walletCreateRequest.get("balance");

		Wallet newWallet = new Wallet();
		newWallet.setBalance(balance);
		newWallet.setUserId(userId);

		walletRepository.save(newWallet);
		walletCacheRepository.saveWalletByUserId(newWallet);
	}

	@KafkaListener(topics = TOPIC_TRANSACTION_INITIATED, groupId = "wallet_group")
	public void updateWalletForTransaction(String message) throws JsonProcessingException {

		JSONObject walletUpdateRequest = objectMapper.readValue(message, JSONObject.class);

		//Get Payer and Payee Wallet and check whether they exist or not

		String payerId = walletUpdateRequest.get("payerId").toString();
		String payeeId = walletUpdateRequest.get("payeeId").toString();
		int amount = (Integer) walletUpdateRequest.get("amount");
		String transactionId = walletUpdateRequest.get("transactionId").toString();

		Wallet payerWallet = walletCacheRepository.getWalletByUserId(payerId);
		if(payerWallet == null) {
			Optional<Wallet> payerWalletOptional = walletRepository.findByUserId(payerId);
			if(payerWalletOptional.isPresent()) {
				payerWallet = payerWalletOptional.get();
				walletCacheRepository.saveWalletByUserId(payerWallet);
			}
		}

		Wallet payeeWallet = walletCacheRepository.getWalletByUserId(payeeId);
		if(payeeWallet == null) {
			Optional<Wallet> payeeWalletOptional = walletRepository.findByUserId(payeeId);
			if(payeeWalletOptional.isPresent()) {
				payeeWallet = payeeWalletOptional.get();
				walletCacheRepository.saveWalletByUserId(payeeWallet);
			}
		}

		JSONObject transactionCompleteRequest = new JSONObject();
		transactionCompleteRequest.put("transactionId", transactionId);

		//Payer should have balance >= amount
		if(payerWallet == null|| payeeWallet == null || payerWallet.getBalance() < amount) {
			transactionCompleteRequest.put("transactionStatus", "FAILED");
		}
		//return transaction status as failed
		else {
			//Update both wallets
			payerWallet.setBalance(payerWallet.getBalance() - amount);
			payeeWallet.setBalance(payeeWallet.getBalance() + amount);

			walletRepository.saveAll(Arrays.asList(payerWallet,payeeWallet));
			//return transaction status as SUCCESS

			//Push a message to topic that is listened by transactionservice
			transactionCompleteRequest.put("transactionStatus", "SUCCESS");
		}

		//return through kafka template
		kafkaTemplate.send(TOPIC_WALLET_UPDATED, transactionId, objectMapper.writeValueAsString(transactionCompleteRequest));
	}

	public Wallet getWalletByUserId(String userId) {

		Wallet wallet = walletCacheRepository.getWalletByUserId(userId);
		if(wallet == null) {
			wallet= walletRepository.findByUserId(userId).orElseThrow();
			walletCacheRepository.saveWalletByUserId(wallet);
		}

		return wallet;
	}
}
