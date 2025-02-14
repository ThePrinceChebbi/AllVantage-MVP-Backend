package com.MarketingMVP.AllVantage.Mail;


import com.MarketingMVP.AllVantage.Entities.ContactMail.ContactMail;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Client;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
public class SimpleMailSender{

    private final JavaMailSender javaMailSender;

    public SimpleMailSender(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }


   public void sendClientMail(ContactMail contactMail, String subject){

        try{

            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setText(ContactMailTemplate.createClientMailTemplate(contactMail),true);
            helper.setTo(contactMail.getEmail());
            helper.setSubject(subject);

            javaMailSender.send(message);

        }catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }
    public void sendAuthMailToEmployee(Employee employee, String subject, String link){

        try{

            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setText(EmployeeAuthMailTemplate.createAuthenticationMailTemplate(employee,link),true);
            helper.setTo(employee.getEmail());
            helper.setSubject(subject);

            javaMailSender.send(message);

        }catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }
    public void sendAuthMailToClient(Client client, String subject, String link){

        try{

            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setText(ClientAuthMailTemplate.createAuthenticationMailTemplate(client,link),true);
            helper.setTo(client.getEmail());
            helper.setSubject(subject);

            javaMailSender.send(message);

        }catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }
}

