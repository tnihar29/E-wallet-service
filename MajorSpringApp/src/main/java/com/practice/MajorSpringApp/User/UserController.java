package com.practice.MajorSpringApp.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    UserService userService;
    @GetMapping("/getUser/{userId}")
    public User getUser(@PathVariable("userId")String userId){
        return userService.get(userId);
    }
    @PostMapping("/user")
    public void createUser(@RequestBody User user){
        userService.create(user);
    }
}
