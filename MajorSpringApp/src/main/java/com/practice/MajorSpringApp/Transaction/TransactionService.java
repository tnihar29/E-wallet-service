package com.practice.MajorSpringApp.Transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.MajorSpringApp.Email.EmailRequest;
import com.practice.MajorSpringApp.User.User;
import com.practice.MajorSpringApp.Wallet.WalletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Date;
import java.util.UUID;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;
    private ObjectMapper objectMapper = new ObjectMapper();
    private RestTemplate restTemplate = new RestTemplate();
    public void createTransaction(TransactionRequest transactionRequest) throws JsonProcessingException {
        Transaction transaction = Transaction.builder().fromUser(transactionRequest.getFromUser())
                .toUser(transactionRequest.getToUser())
                .amount(transactionRequest.getAmount())
                .purpose(transactionRequest.getPurpose())
                .externalId(UUID.randomUUID().toString())
                .status(TransactionStatus.PENDING.toString())
                .txnDateTime(new Date().toString()).build();
        transactionRepository.save(transaction);

        WalletRequest walletRequest = WalletRequest.builder()
                .fromUser(transactionRequest.getFromUser()).toUser(transactionRequest.getToUser())
                .amount(transactionRequest.getAmount()).transactionId(transaction.getExternalId()).build();
        kafkaTemplate.send("wallet","wallet",this.objectMapper.writeValueAsString(walletRequest));
    }

    @KafkaListener(topics = {"transaction"},groupId = "major")
    public void updateStatus(String msg) throws JsonProcessingException {
        TransactionUpdateRequest transactionUpdateRequest = objectMapper.readValue(msg,TransactionUpdateRequest.class);
        Transaction transaction = transactionRepository.findByExternalId(transactionUpdateRequest.getTransactionId()).get();
        transaction.setStatus(TransactionStatus.valueOf(transactionUpdateRequest.getStatus().toUpperCase()).toString());
        transactionRepository.save(transaction);

        URI uri = URI.create("http://localhost:8080/getUser/"+transaction.getFromUser());
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity httpEntity = new HttpEntity(httpHeaders);
        User fromUser = restTemplate.exchange(uri, HttpMethod.GET,httpEntity,User.class).getBody();

        uri = URI.create("http://localhost:8080/getUser/"+transaction.getToUser());
        User toUser = restTemplate.exchange(uri, HttpMethod.GET,httpEntity,User.class).getBody();
        String message="";
        if(transactionUpdateRequest.getStatus().toUpperCase().equals("APPROVED")){
            String recMessage = String.format("Your wallet has been credited with Rs. %s from %s",transaction.getAmount(),fromUser.getUserId());
            message = String.format("Transaction Successful. Your wallet has been debited by %s. Amount successfully sent to %s"
                        ,transaction.getAmount(),toUser.getUserId());
            String htmlMsg="<!DOCTYPE html>  \n" +
                    "    <html>  \n" +
                    "    <head>   \n" +
                    "        <style>  \n" +
                    "             .name{\n" +
                    "                    font-size:20;\n" +
                    "                    color:black;\n" +
                    "                    font-family: 'Times New Roman', Times, serif;\n" +
                    "                }\n" +
                    "            .msg{\n" +
                    "                    font-size:20;\n" +
                    "                    color:black;\n" +
                    "                    font-family: 'Times New Roman', Times, serif;\n" +
                    "                    font-weight: bold;\n" +
                    "                }\n" +
                    "        </style>  \n" +
                    "    </head>  \n" +
                    "    <body>  \n" +
                    "        <div class='name'> Hello "+toUser.getFirstName()+" "+toUser.getLastName()+" </div><br>\n" +
                    "        <div class='msg'>"+recMessage+"</div><br>\n" +
                    "        <br><br><br>\n" +
                    "        <div>Regards</div>\n" +
                    "        <div>Spring Team</div><br><br>\n" +
                    "        <div>In case of any queries please contact us at nihartiwari.dps@gmail.com </div>\n" +
                    "    </body>  \n" +
                    "    </html> ";
            EmailRequest emailRequest = new EmailRequest(toUser.getEmail(),htmlMsg);
            kafkaTemplate.send("email","email",objectMapper.writeValueAsString(emailRequest));
        }
        else if(transactionUpdateRequest.getStatus().toUpperCase().equals("REJECTED")){
            message = String.format("Transaction Failed. Insufficient Funds. Please recharge");
        }

        String htmlMsg="<!DOCTYPE html>  \n" +
                "    <html>  \n" +
                "    <head>   \n" +
                "        <style>  \n" +
                "             .name{\n" +
                "                    font-size:20;\n" +
                "                    color:black;\n" +
                "                    font-family: 'Times New Roman', Times, serif;\n" +
                "                }\n" +
                "            .msg{\n" +
                "                    font-size:20;\n" +
                "                    color:black;\n" +
                "                    font-family: 'Times New Roman', Times, serif;\n" +
                "                    font-weight: bold;\n" +
                "                }\n" +
                "        </style>  \n" +
                "    </head>  \n" +
                "    <body>  \n" +
                "        <div class='name'> Hello "+fromUser.getFirstName()+" "+fromUser.getLastName()+" </div><br>\n" +
                "        <div class='msg'>"+message+"</div><br>\n" +
                "        <br><br><br>\n" +
                "        <div>Regards</div>\n" +
                "        <div>Spring Team</div><br><br>\n" +
                "        <div>In case of any queries please contact us at nihartiwari.dps@gmail.com </div>\n" +
                "    </body>  \n" +
                "    </html> ";
        EmailRequest emailRequest = new EmailRequest(fromUser.getEmail(),htmlMsg);
        kafkaTemplate.send("email","email",objectMapper.writeValueAsString(emailRequest));
    }
}
