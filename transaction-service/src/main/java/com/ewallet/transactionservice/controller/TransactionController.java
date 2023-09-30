package com.ewallet.transactionservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ewallet.transactionservice.models.Transaction;
import com.ewallet.transactionservice.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
public class TransactionController {

	@Autowired
	TransactionService transactionService;

	@PostMapping("/transfer")
	public void createNewTransaction(@RequestBody Transaction transaction) {
		try {
			transactionService.createTransaction(transaction);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
}
