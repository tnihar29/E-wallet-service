package com.practice.MajorSpringApp.Email;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailRequest {
    private String to;
    private String msg;
}
