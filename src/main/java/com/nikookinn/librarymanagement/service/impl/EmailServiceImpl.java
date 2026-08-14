package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailServiceImpl(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    @Async("taskExecutor")
    public void sendLoanConfirmation(String toEmail, String memberName, String bookTitle, String dueDate) {
        log.info("Preparing loan confirmation email for {}...", toEmail);
        try {
            Context context = new Context();
            context.setVariable("memberName", memberName);
            context.setVariable("bookTitle", bookTitle);
            context.setVariable("dueDate", dueDate);
            String html = templateEngine.process("email/loan-confirmation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("library@nikookinn.com");
            helper.setTo(toEmail);
            helper.setSubject("Loan Confirmation: " + bookTitle);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("LOAN CONFIRMATION EMAIL SENT to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send loan confirmation email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Override
    @Async("taskExecutor")
    public void sendOverdueReminder(String toEmail, String memberName, String bookTitle, String dueDate) {
        log.info("Preparing overdue reminder email for {}...", toEmail);
        try {
            Context context = new Context();
            context.setVariable("memberName", memberName);
            context.setVariable("bookTitle", bookTitle);
            context.setVariable("dueDate", dueDate);
            String html = templateEngine.process("email/overdue-reminder", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("library@nikookinn.com");
            helper.setTo(toEmail);
            helper.setSubject("Overdue Book Reminder: " + bookTitle);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("OVERDUE REMINDER EMAIL SENT to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send overdue reminder email to {}: {}", toEmail, e.getMessage());
        }
    }
}
