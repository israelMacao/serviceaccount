package com.prueba.cuenta.controller;

import com.prueba.cuenta.dto.MovementDTO;
import com.prueba.cuenta.dto.MovementReportDTO;
import com.prueba.cuenta.entity.Movement;
import com.prueba.cuenta.service.MovementService;
import com.prueba.cuenta.utils.ApiResponseClient;
import com.prueba.cuenta.utils.ResponseProcess;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Movement Controller", description = "API para la gestión de movimientos")
public class MovementController {

    @Autowired
    private MovementService movementService;

    @Operation(summary = "Crea un registro de movimiento de una cuenta", description = "Realiza depositos o retiros de una cuenta existente con los datos proporcionados")
    @PostMapping
    public Mono<ResponseEntity<ApiResponseClient<Movement>>> createMovement(@Valid @RequestBody MovementDTO movementDTO) {
        return movementService.createMovement(movementDTO)
                .map(response -> {
                    if (response.getResponseProcess() != null && !"0".equals(response.getResponseProcess().getCode())) {
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }
                    return new ResponseEntity<>(response, HttpStatus.CREATED);
                })
                .onErrorResume(e -> {
                    ApiResponseClient<Movement> errorResponse = new ApiResponseClient<>(null, new ResponseProcess("1", e.getMessage(), "ERROR"));
                    return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

    @Operation(summary = "Reporte de movimientos", description = "Devuelve el reporte de movimientos de una cuenta existente con fecha inicio y fecha fin")
    @GetMapping("/reporte")
    public Mono<ResponseEntity<ApiResponseClient<List<MovementReportDTO>>>> generateReport(
            @RequestParam Integer cuentaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return movementService.generateReport(cuentaId, startDate, endDate)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(e -> {
                    ApiResponseClient<List<MovementReportDTO>> errorResponse = new ApiResponseClient<>(null, new ResponseProcess("500", e.getMessage(), "ERROR"));
                    return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

    @Operation(summary = "Movimientos de una cuenta", description = "Realiza la consulta de todos los movimientos de una cuenta")
    @GetMapping("/{cuentaId}")
    public Flux<ApiResponseClient<Movement>> getMovementsByAccount(@PathVariable Integer cuentaId) {
        return movementService.getMovementsByAccount(cuentaId)
                .onErrorResume(e -> {
                    ApiResponseClient<Movement> errorResponse = new ApiResponseClient<>(null, new ResponseProcess("1", e.getMessage(), "ERROR"));
                    return Flux.just(errorResponse);
                });
    }
}