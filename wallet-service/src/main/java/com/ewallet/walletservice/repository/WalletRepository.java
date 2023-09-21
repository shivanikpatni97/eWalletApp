package com.ewallet.walletservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ewallet.walletservice.models.Wallet;

import jakarta.transaction.Transactional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer>{

	Optional<Wallet> findByUserId(String userId);
	
//	@Transactional
//	@Modifying
//	@Query("UPDATE Wallet w SET w.balance = w.balance + : amount WHERE w.userId = :userId")
//	void updateWalletByAmount(String userId, int amount);
}
