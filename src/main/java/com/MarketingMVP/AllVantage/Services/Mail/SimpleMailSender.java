package com.MarketingMVP.AllVantage.Services.Mail;



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

    public void sendAuthMailToClient(Client client, String password, String link){
        try{
            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setText(ClientAuthMailTemplate.createAuthenticationMailTemplate(client,password,link),true);
            helper.setTo(client.getEmail());
            helper.setSubject("Account Confirmation");

            javaMailSender.send(message);

        }catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendAuthMailToEmployee(Employee employee, String password, String link){
        try{
            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setText(EmployeeAuthMailTemplate.createAuthenticationMailTemplate(employee, password, link),true);
            helper.setTo(employee.getEmail());
            helper.setSubject("Account Confirmation");

            javaMailSender.send(message);

        }catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}

