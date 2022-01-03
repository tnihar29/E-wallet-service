package com.practice.MajorSpringApp.Wallet;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface WalletRepository extends CrudRepository<Wallet,Long> {
    Optional<Wallet> findByUserId(String userId);

    @Transactional
    @Modifying
    @Query("update Wallet w set w.balance=w.balance + :amount where w.userId=:userId")
    public void update(String userId,double amount);
}
