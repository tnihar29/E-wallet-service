package com.practice.MajorSpringApp.User;

import lombok.*;
import org.springframework.web.bind.annotation.GetMapping;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String firstName;
    private String lastName;
    @Column(unique = true)
    private String mobile;
    @Column(unique = true,nullable = false)
    private String userId;
    @Column(unique = true,nullable = false)
    private String email;
}
