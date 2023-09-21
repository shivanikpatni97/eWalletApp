package com.ewallet.walletservice.repository;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.ewallet.walletservice.models.Wallet;

@Repository
public class WalletCacheRepository {

	private static final String WALLET_PREFIX = "WALLET::";
	
	@Autowired
	RedisTemplate<String, Object> redisTemplate;
	private String getKey(String userId) {
		return WALLET_PREFIX + userId;
	}
	
	public void saveWalletByUserId(Wallet wallet) {
		if(wallet == null || wallet.getUserId() == null || wallet.getUserId().equals("")) {
			return;
		}
		redisTemplate.opsForValue().set(getKey(wallet.getUserId()), wallet, Duration.ofMinutes(30));
	}
	
	public Wallet getWalletByUserId(String userId) {
		if(userId == null || userId.equals("")) {
			return null;
		}
		return (Wallet)redisTemplate.opsForValue().get(getKey(userId));
	}
}
