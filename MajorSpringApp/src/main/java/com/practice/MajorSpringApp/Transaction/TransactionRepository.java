package com.practice.MajorSpringApp.Transaction;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TransactionRepository extends CrudRepository<Transaction,Long> {
    Optional<Transaction> findByExternalId(String transactionId);
}
