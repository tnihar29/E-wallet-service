package com.practice.MajorSpringApp.User;

import com.practice.MajorSpringApp.Wallet.Wallet;
import com.practice.MajorSpringApp.Wallet.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    WalletRepository walletRepository;

    public void create(User user){
        userRepository.save(user);
        Wallet wallet = Wallet.builder().userId(user.getUserId()).balance(100.0).build();
        walletRepository.save(wallet);
    }
    public User get(String uid){
        return userRepository.findByUserId(uid)
                .orElseThrow(() -> new NoSuchElementException(String.format("%s not found",uid)));
    }
}
