package com.graduation.project.auth.service;

import com.sendgrid.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
  @Autowired private JavaMailSender javaMailSender;

  // Lấy email người gửi từ file cấu hình (chính là email Gmail của bạn)
  @Value("${spring.mail.username}")
  private String fromEmail;

  public void sendVerificationEmail(String to, String subject, String body) {
    try {
      // Tạo một mail tin nhắn dạng HTML
      MimeMessage message = javaMailSender.createMimeMessage();

      // Helper giúp set các thuộc tính dễ hơn, true nghĩa là có hỗ trợ Multipart (đính kèm file nếu
      // cần)
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject(subject);

      // true ở đây quan trọng: báo cho nó biết body là mã HTML
      helper.setText(body, true);

      // Gửi mail
      javaMailSender.send(message);
      System.out.println("Email sent successfully to: " + to);

    } catch (MessagingException e) {
      throw new RuntimeException("Failed to send email via Gmail SMTP", e);
    }
  }
  //  @Value("${sendgrid.api-key}")
  //  private String sendGridApiKey;
  //
  //  @Value("${sendgrid.from-email}")
  //  private String fromEmail;
  //
  //  public void sendVerificationEmail(String to, String subject, String body) {
  //    Email from = new Email(fromEmail);
  //    Email toEmail = new Email(to);
  //    Content content = new Content("text/html", body);
  //    Mail mail = new Mail(from, subject, toEmail, content);
  //
  //    SendGrid sg = new SendGrid(sendGridApiKey);
  //    Request request = new Request();
  //
  //    try {
  //      request.setMethod(Method.POST);
  //      request.setEndpoint("mail/send");
  //      request.setBody(mail.build());
  //      Response response = sg.api(request);
  //      System.out.println("SendGrid response status: " + response.getStatusCode());
  //    } catch (IOException ex) {
  //      throw new RuntimeException("Failed to send email via SendGrid", ex);
  //    }
  //  }
}
