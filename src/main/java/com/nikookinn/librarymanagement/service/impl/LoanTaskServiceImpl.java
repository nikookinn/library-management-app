package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.repository.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LoanTaskServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(LoanTaskServiceImpl.class);
    private final LoanRepository loanRepository;

    public LoanTaskServiceImpl(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    /**
     * Automatically marks active loans as OVERDUE if the due date has passed.
     * Runs every day at midnight.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processOverdueLoans() {
        log.info("Starting scheduled task to mark overdue loans...");
        int updatedCount = loanRepository.markOverdueLoans(LocalDateTime.now());
        log.info("Finished marking overdue loans. Total updated: {}", updatedCount);
    }
}
