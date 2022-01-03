package com.practice.MajorSpringApp.Transaction;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TransactionRequest {
    private String fromUser,toUser,purpose;
    private double amount;
}
