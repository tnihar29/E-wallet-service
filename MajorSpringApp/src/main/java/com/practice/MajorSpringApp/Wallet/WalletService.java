package com.practice.MajorSpringApp.Wallet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.MajorSpringApp.Transaction.TransactionStatus;
import com.practice.MajorSpringApp.Transaction.TransactionUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    WalletRepository walletRepository;
    @Autowired
    KafkaTemplate kafkaTemplate;
    @KafkaListener(topics = {"wallet"},groupId = "major")
    public void updateWallet(String msg) throws JsonProcessingException {
        WalletRequest walletRequest = objectMapper.readValue(msg,WalletRequest.class);
        Wallet fromWallet = walletRepository.findByUserId(walletRequest.getFromUser()).orElse(
                Wallet.builder().userId(walletRequest.getFromUser()).balance(0.0).build()
        );
        Wallet toWallet = walletRepository.findByUserId(walletRequest.getToUser()).orElse(
                Wallet.builder().userId(walletRequest.getToUser()).balance(0.0).build()
        );
        Double amount = walletRequest.getAmount();
        String transactionId = walletRequest.getTransactionId();

        if(fromWallet.getBalance()-amount<0.0){
            TransactionUpdateRequest transactionUpdateRequest = TransactionUpdateRequest.builder()
                    .transactionId(transactionId).status(TransactionStatus.REJECTED.toString()).build();
            kafkaTemplate.send("transaction","transaction",objectMapper.writeValueAsString(transactionUpdateRequest));
            return;
        }
        walletRepository.update(fromWallet.getUserId(), 0-amount);
        walletRepository.update(toWallet.getUserId(),amount);
        TransactionUpdateRequest transactionUpdateRequest = TransactionUpdateRequest.builder()
                .transactionId(transactionId).status(TransactionStatus.APPROVED.toString()).build();
        kafkaTemplate.send("transaction","transaction",objectMapper.writeValueAsString(transactionUpdateRequest));
    }
}
