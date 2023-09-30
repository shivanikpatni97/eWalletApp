package com.ewallet.transactionservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ewallet.transactionservice.models.Transaction;
import com.ewallet.transactionservice.models.TransactionStatus;

import jakarta.transaction.Transactional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

	@Transactional
	@Modifying
	@Query("UPDATE Transaction t SET t.transactionStatus = :transactionStatus WHERE t.transactionId = :transactionId")
	void updateTransactionStatus(String transactionId,TransactionStatus transactionStatus);
	
	Optional<Transaction> findByTransactionId(String transactionId);
}
