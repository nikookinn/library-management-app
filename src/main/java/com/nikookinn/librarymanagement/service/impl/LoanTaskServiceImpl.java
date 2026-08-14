package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.repository.LoanRepository;
import com.nikookinn.librarymanagement.service.EmailService;
import com.nikookinn.librarymanagement.entity.Loan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanTaskServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(LoanTaskServiceImpl.class);
    private final LoanRepository loanRepository;
    private final EmailService emailService;

    public LoanTaskServiceImpl(LoanRepository loanRepository, EmailService emailService) {
        this.loanRepository = loanRepository;
        this.emailService = emailService;
    }

    /**
     * Automatically marks active loans as OVERDUE if the due date has passed
     * and sends async email reminders.
     * Runs every day at midnight.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processOverdueLoans() {
        log.info("Starting scheduled task to mark overdue loans...");
        
        LocalDateTime now = LocalDateTime.now();
        
        // Find loans that are about to be marked overdue
        List<Loan> overdueLoans = loanRepository.findOverdueLoans(now, PageRequest.of(0, 100));
        
        int updatedCount = loanRepository.markOverdueLoans(now);
        log.info("Finished marking overdue loans. Total updated: {}", updatedCount);

        // Send async email reminders
        for (Loan loan : overdueLoans) {
            emailService.sendOverdueReminder(
                    loan.getMember().getEmail(),
                    loan.getMember().getFirstName() + " " + loan.getMember().getLastName(),
                    loan.getBook().getTitle(),
                    loan.getDueDate().toString()
            );
        }
    }
}
