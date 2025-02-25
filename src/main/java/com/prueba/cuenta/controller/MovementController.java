package com.prueba.cuenta.controller;

import com.prueba.cuenta.dto.MovementDTO;
import com.prueba.cuenta.dto.MovementReportDTO;
import com.prueba.cuenta.entity.Movement;
import com.prueba.cuenta.service.MovementService;
import com.prueba.cuenta.utils.ApiResponse;
import com.prueba.cuenta.utils.ResponseProcess;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/movimientos")
public class MovementController {

    @Autowired
    private MovementService movementService;

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<Movement>>> createMovement(@Valid @RequestBody MovementDTO movementDTO) {
        return movementService.createMovement(movementDTO)
                .map(response -> {
                    if (response.getResponseProcess() != null && !"0".equals(response.getResponseProcess().getCode())) {
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }
                    return new ResponseEntity<>(response, HttpStatus.CREATED);
                })
                .onErrorResume(e -> {
                    ApiResponse<Movement> errorResponse = new ApiResponse<>(null, new ResponseProcess("1", e.getMessage(), "ERROR"));
                    return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

    @GetMapping("/reporte")
    public Mono<ResponseEntity<ApiResponse<List<MovementReportDTO>>>> generateReport(
            @RequestParam Integer cuentaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return movementService.generateReport(cuentaId, startDate, endDate)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(e -> {
                    ApiResponse<List<MovementReportDTO>> errorResponse = new ApiResponse<>(null, new ResponseProcess("500", e.getMessage(), "ERROR"));
                    return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

    @GetMapping("/{cuentaId}")
    public Flux<ApiResponse<Movement>> getMovementsByAccount(@PathVariable Integer cuentaId) {
        return movementService.getMovementsByAccount(cuentaId)
                .onErrorResume(e -> {
                    ApiResponse<Movement> errorResponse = new ApiResponse<>(null, new ResponseProcess("1", e.getMessage(), "ERROR"));
                    return Flux.just(errorResponse);
                });
    }
}