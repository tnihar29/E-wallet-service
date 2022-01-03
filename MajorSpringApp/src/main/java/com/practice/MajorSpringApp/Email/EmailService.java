package com.practice.MajorSpringApp.Email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailParseException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailService {
    @Autowired
    JavaMailSender javaMailSender;
    @Autowired
    SimpleMailMessage simpleMailMessage;

    ObjectMapper objectMapper =new ObjectMapper();

    @KafkaListener(topics = {"email"},groupId = "major")
    public void sendEmail(String msg) throws JsonProcessingException {
        EmailRequest emailRequest = objectMapper.readValue(msg,EmailRequest.class);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "utf-8");
            String htmlMsg = emailRequest.getMsg();
            mimeMessage.setContent(htmlMsg, "text/html");
            helper.setTo(emailRequest.getTo());
            helper.setSubject("No Reply - Transaction Update");
            helper.setFrom("***");
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new MailParseException(e);
        }
    }
}
