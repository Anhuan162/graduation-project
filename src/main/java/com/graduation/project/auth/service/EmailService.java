package com.graduation.project.auth.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  @Value("${sendgrid.api-key}")
  private String sendGridApiKey;

  @Value("${sendgrid.from-email}")
  private String fromEmail;

  public void sendVerificationEmail(String to, String subject, String body) {
    Email from = new Email(fromEmail);
    Email toEmail = new Email(to);
    Content content = new Content("text/html", body);
    Mail mail = new Mail(from, subject, toEmail, content);

    SendGrid sg = new SendGrid(sendGridApiKey);
    Request request = new Request();

    try {
      request.setMethod(Method.POST);
      request.setEndpoint("mail/send");
      request.setBody(mail.build());
      Response response = sg.api(request);

      int statusCode = response.getStatusCode();
      System.out.println("SendGrid response status: " + statusCode);
      if (statusCode >= 400 ){
        throw new RuntimeException(
                "SendGrid error. Status: " + statusCode +
                ", body: " + response.getBody()
        );
      }
    } catch (IOException ex) {
      throw new RuntimeException("Failed to send email via SendGrid", ex);
    }
  }
}
