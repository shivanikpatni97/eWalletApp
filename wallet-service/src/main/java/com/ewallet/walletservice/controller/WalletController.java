package com.ewallet.walletservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.ewallet.walletservice.models.Wallet;
import com.ewallet.walletservice.service.WalletService;

@RestController
public class WalletController {

	@Autowired
	WalletService walletService;
	
	@GetMapping("/wallet/{userId}")
	public Wallet getWalletByUserId(@PathVariable String userId) {
		return walletService.getWalletByUserId(userId);
	}
}
