package com.financeapp.config;

import com.financeapp.model.entity.FinancialRecord;
import com.financeapp.model.entity.User;
import com.financeapp.model.enums.RecordType;
import com.financeapp.model.enums.Role;
import com.financeapp.model.enums.UserStatus;
import com.financeapp.repository.FinancialRecordRepository;
import com.financeapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Seeds the database with initial users and financial records on first run.
 *
 * Safe to run multiple times — skips seeding if users already exist (idempotent).
 *
 * Default credentials seeded:
 *   admin@financeapp.com    / Admin@1234    (ADMIN)
 *   analyst@financeapp.com  / Analyst@1234  (ANALYST)
 *   viewer@financeapp.com   / Viewer@1234   (VIEWER)
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final UserRepository            userRepository;
    private final FinancialRecordRepository recordRepository;
    private final PasswordEncoder           passwordEncoder;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            // Idempotent guard — skip if any users already exist
            if (userRepository.count() > 0) {
                log.info("DataSeeder: database already populated, skipping seed.");
                return;
            }

            log.info("DataSeeder: seeding initial data...");

            // -----------------------------------------------
            // Seed Users
            // -----------------------------------------------
            User admin = userRepository.save(User.builder()
                    .name("System Admin")
                    .email("admin@financeapp.com")
                    .password(passwordEncoder.encode("Admin@1234"))
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build());

            userRepository.save(User.builder()
                    .name("Finance Analyst")
                    .email("analyst@financeapp.com")
                    .password(passwordEncoder.encode("Analyst@1234"))
                    .role(Role.ANALYST)
                    .status(UserStatus.ACTIVE)
                    .build());

            userRepository.save(User.builder()
                    .name("Dashboard Viewer")
                    .email("viewer@financeapp.com")
                    .password(passwordEncoder.encode("Viewer@1234"))
                    .role(Role.VIEWER)
                    .status(UserStatus.ACTIVE)
                    .build());

            // -----------------------------------------------
            // Seed Financial Records (3 months of data)
            //
            // NOTE: For months -2 and -1 we use withDayOfMonth() safely
            // (those months are fully in the past). For the CURRENT month
            // we only use today or minusDays() to guarantee no future dates.
            // -----------------------------------------------
            LocalDate today = LocalDate.now();

            List<FinancialRecord> records = List.of(

                // ── Month -2 ──────────────────────────────────────
                record(new BigDecimal("85000.00"), RecordType.INCOME,  "Salary",
                       today.minusMonths(2).withDayOfMonth(1),  "Monthly salary",         admin),
                record(new BigDecimal("12000.00"), RecordType.EXPENSE, "Rent",
                       today.minusMonths(2).withDayOfMonth(3),  "Office rent",            admin),
                record(new BigDecimal("3500.00"),  RecordType.EXPENSE, "Utilities",
                       today.minusMonths(2).withDayOfMonth(5),  "Electricity & internet", admin),
                record(new BigDecimal("25000.00"), RecordType.INCOME,  "Freelance",
                       today.minusMonths(2).withDayOfMonth(10), "Project payment",        admin),
                record(new BigDecimal("8000.00"),  RecordType.EXPENSE, "Marketing",
                       today.minusMonths(2).withDayOfMonth(15), "Social media ads",       admin),

                // ── Month -1 ──────────────────────────────────────
                record(new BigDecimal("85000.00"), RecordType.INCOME,  "Salary",
                       today.minusMonths(1).withDayOfMonth(1),  "Monthly salary",         admin),
                record(new BigDecimal("12000.00"), RecordType.EXPENSE, "Rent",
                       today.minusMonths(1).withDayOfMonth(3),  "Office rent",            admin),
                record(new BigDecimal("4200.00"),  RecordType.EXPENSE, "Software",
                       today.minusMonths(1).withDayOfMonth(7),  "SaaS subscriptions",    admin),
                record(new BigDecimal("18500.00"), RecordType.INCOME,  "Consulting",
                       today.minusMonths(1).withDayOfMonth(12), "Strategy consulting",    admin),
                record(new BigDecimal("6000.00"),  RecordType.EXPENSE, "Travel",
                       today.minusMonths(1).withDayOfMonth(20), "Client site visit",      admin),
                record(new BigDecimal("2800.00"),  RecordType.EXPENSE, "Utilities",
                       today.minusMonths(1).withDayOfMonth(25), "Internet & phone",       admin),

                // ── Current Month (safe: today / minusDays only) ───
                record(new BigDecimal("85000.00"), RecordType.INCOME,  "Salary",
                       today.withDayOfMonth(1), "Monthly salary",    admin),
                record(new BigDecimal("32000.00"), RecordType.INCOME,  "Product Sale",
                       today.minusDays(1),      "Q1 product revenue", admin),
                record(new BigDecimal("12000.00"), RecordType.EXPENSE, "Rent",
                       today.minusDays(1),      "Office rent",        admin),
                record(new BigDecimal("9500.00"),  RecordType.EXPENSE, "Marketing",
                       today,                   "Campaign spend",     admin),
                record(new BigDecimal("3000.00"),  RecordType.EXPENSE, "Training",
                       today,                   "Team upskilling",    admin)
            );

            recordRepository.saveAll(records);

            log.info("DataSeeder: seeded 3 users and {} financial records successfully.", records.size());
            log.info("=============================================================");
            log.info("Default Credentials:");
            log.info("  ADMIN    → admin@financeapp.com    / Admin@1234");
            log.info("  ANALYST  → analyst@financeapp.com  / Analyst@1234");
            log.info("  VIEWER   → viewer@financeapp.com   / Viewer@1234");
            log.info("Swagger UI → http://localhost:8080/api/swagger-ui.html");
            log.info("=============================================================");
        };
    }

    // -----------------------------------------------
    // Helper — builds a FinancialRecord entity
    // -----------------------------------------------
    private FinancialRecord record(BigDecimal amount, RecordType type,
                                    String category, LocalDate date,
                                    String notes, User createdBy) {
        return FinancialRecord.builder()
                .amount(amount)
                .type(type)
                .category(category)
                .date(date)
                .notes(notes)
                .createdBy(createdBy)
                .deleted(false)
                .build();
    }
}
