package com.nikookinn.librarymanagement.service;

public interface EmailService {
    void sendLoanConfirmation(String toEmail, String memberName, String bookTitle, String dueDate);
    void sendOverdueReminder(String toEmail, String memberName, String bookTitle, String dueDate);
}
