package com.practice.MajorSpringApp.Wallet;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class WalletRequest {
    private String fromUser,toUser,transactionId;
    private double amount;
}
