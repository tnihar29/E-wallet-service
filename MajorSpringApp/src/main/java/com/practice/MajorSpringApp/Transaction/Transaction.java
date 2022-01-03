package com.practice.MajorSpringApp.Transaction;

import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="internalId")
    private long id;
    private String externalId = UUID.randomUUID().toString();
    private String fromUser,toUser,purpose,txnDateTime;
    private double amount;
    private String status;
}
