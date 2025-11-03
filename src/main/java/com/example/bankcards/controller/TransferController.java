package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.enums.TransferStatus;
import com.example.bankcards.service.transfer.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfer Management", description = "APIs for managing money transfers")
@SecurityRequirement(name = "bearerAuth")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Create money transfer between own cards")
    public ResponseEntity<TransferDto> createTransfer(@RequestBody TransferRequest request) {
        TransferDto transfer = transferService.createTransfer(request);
        return ResponseEntity.ok(transfer);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get current user's transfers")
    public ResponseEntity<Page<TransferDto>> getUserTransfers(
            @PageableDefault(size = 20, sort = "transferDate") Pageable pageable) {
        Page<TransferDto> transfers = transferService.getUserTransfers(pageable);
        return ResponseEntity.ok(transfers);
    }

    @GetMapping("/my/outgoing")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get outgoing transfers")
    public ResponseEntity<Page<TransferDto>> getOutgoingTransfers(
            @PageableDefault(size = 20, sort = "transferDate") Pageable pageable) {
        Page<TransferDto> transfers = transferService.getOutgoingTransfers(pageable);
        return ResponseEntity.ok(transfers);
    }

    @GetMapping("/my/incoming")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get incoming transfers")
    public ResponseEntity<Page<TransferDto>> getIncomingTransfers(
            @PageableDefault(size = 20, sort = "transferDate") Pageable pageable) {
        Page<TransferDto> transfers = transferService.getIncomingTransfers(pageable);
        return ResponseEntity.ok(transfers);
    }

    @GetMapping("/my/{transferId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') and @transferService.isTransferParticipant(#transferId)")
    @Operation(summary = "Get transfer by ID")
    public ResponseEntity<TransferDto> getTransferById(@PathVariable Long transferId) {
        TransferDto transfer = transferService.getTransferById(transferId);
        return ResponseEntity.ok(transfer);
    }

    @GetMapping("/my/statistics/outgoing")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get total outgoing amount for period")
    public ResponseEntity<Long> getTotalOutgoingAmount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Long totalAmount = transferService.getTotalOutgoingAmountForPeriod(startDate, endDate);
        return ResponseEntity.ok(totalAmount);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all transfers (ADMIN only)")
    public ResponseEntity<Page<TransferDto>> getAllTransfers(
            @PageableDefault(size = 20, sort = "transferDate") Pageable pageable) {
        Page<TransferDto> transfers = transferService.getAllTransfers(pageable);
        return ResponseEntity.ok(transfers);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get transfers by status (ADMIN only)")
    public ResponseEntity<Page<TransferDto>> getTransfersByStatus(
            @PathVariable TransferStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TransferDto> transfers = transferService.getTransfersByStatus(status, pageable);
        return ResponseEntity.ok(transfers);
    }

    @GetMapping("/period")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get transfers by period (ADMIN only)")
    public ResponseEntity<Page<TransferDto>> getTransfersByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TransferDto> transfers = transferService.getTransfersByPeriod(startDate, endDate, pageable);
        return ResponseEntity.ok(transfers);
    }

    @PatchMapping("/{transferId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update transfer status (ADMIN only)")
    public ResponseEntity<TransferDto> updateTransferStatus(
            @PathVariable Long transferId,
            @RequestParam TransferStatus status) {
        TransferDto updatedTransfer = transferService.updateTransferStatus(transferId, status);
        return ResponseEntity.ok(updatedTransfer);
    }

    @DeleteMapping("/{transferId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancel transfer (ADMIN only)")
    public ResponseEntity<Void> cancelTransfer(@PathVariable Long transferId) {
        transferService.cancelTransfer(transferId);
        return ResponseEntity.noContent().build();
    }
}
