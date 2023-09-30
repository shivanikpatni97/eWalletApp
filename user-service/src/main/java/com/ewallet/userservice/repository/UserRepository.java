package com.ewallet.userservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ewallet.userservice.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>{

	 Optional<User> findByUserId(String userID);
}
