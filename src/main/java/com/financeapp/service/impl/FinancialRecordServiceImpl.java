package com.financeapp.service.impl;

import com.financeapp.dto.request.FinancialRecordRequest;
import com.financeapp.dto.response.FinancialRecordResponse;
import com.financeapp.exception.ResourceNotFoundException;
import com.financeapp.model.entity.FinancialRecord;
import com.financeapp.model.entity.User;
import com.financeapp.model.enums.RecordType;
import com.financeapp.repository.FinancialRecordRepository;
import com.financeapp.repository.UserRepository;
import com.financeapp.service.FinancialRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Implementation of {@link FinancialRecordService}.
 *
 * Key design decisions:
 *  - Soft delete: records are flagged `deleted = true`, never physically removed.
 *  - createdBy is fetched once and linked — no lazy-load surprises in JSON serialisation.
 *  - All reads use readOnly = true for Hibernate performance benefits.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialRecordServiceImpl implements FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository            userRepository;

    // -------------------------------------------------------
    // Create
    // -------------------------------------------------------

    @Override
    @Transactional
    public FinancialRecordResponse createRecord(FinancialRecordRequest request, String creatorEmail) {
        User creator = loadUserByEmail(creatorEmail);

        FinancialRecord record = FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory().trim())
                .date(request.getDate())
                .notes(request.getNotes())
                .createdBy(creator)
                .deleted(false)
                .build();

        FinancialRecord saved = recordRepository.save(record);
        log.info("Financial record created: id={}, type={}, amount={}, by={}",
                saved.getId(), saved.getType(), saved.getAmount(), creatorEmail);
        return mapToResponse(saved);
    }

    // -------------------------------------------------------
    // Update
    // -------------------------------------------------------

    @Override
    @Transactional
    public FinancialRecordResponse updateRecord(Long id, FinancialRecordRequest request) {
        FinancialRecord record = findActiveRecordOrThrow(id);

        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory().trim());
        record.setDate(request.getDate());
        record.setNotes(request.getNotes());

        FinancialRecord updated = recordRepository.save(record);
        log.info("Financial record updated: id={}", id);
        return mapToResponse(updated);
    }

    // -------------------------------------------------------
    // Soft Delete
    // -------------------------------------------------------

    @Override
    @Transactional
    public void deleteRecord(Long id) {
        FinancialRecord record = findActiveRecordOrThrow(id);
        record.setDeleted(true);
        recordRepository.save(record);
        log.info("Financial record soft-deleted: id={}", id);
    }

    // -------------------------------------------------------
    // Read — single
    // -------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public FinancialRecordResponse getRecordById(Long id) {
        return mapToResponse(findActiveRecordOrThrow(id));
    }

    // -------------------------------------------------------
    // Read — paginated list with optional filters
    // -------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Page<FinancialRecordResponse> getRecords(
            RecordType type,
            String     category,
            LocalDate  from,
            LocalDate  to,
            Pageable   pageable
    ) {
        return recordRepository
                .findAllWithFilters(type, category, from, to, pageable)
                .map(this::mapToResponse);
    }

    // -------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------

    /**
     * Finds a non-deleted record or throws 404.
     */
    private FinancialRecord findActiveRecordOrThrow(Long id) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("FinancialRecord", "id", id));
        if (record.isDeleted()) {
            throw new ResourceNotFoundException("FinancialRecord", "id", id);
        }
        return record;
    }

    private User loadUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + email));
    }

    /**
     * Maps a FinancialRecord entity to its response DTO.
     * Flattens createdBy into name/email fields to avoid nested JSON.
     */
    public FinancialRecordResponse mapToResponse(FinancialRecord record) {
        return FinancialRecordResponse.builder()
                .id(record.getId())
                .amount(record.getAmount())
                .type(record.getType())
                .category(record.getCategory())
                .date(record.getDate())
                .notes(record.getNotes())
                .createdByName(record.getCreatedBy().getName())
                .createdByEmail(record.getCreatedBy().getEmail())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
