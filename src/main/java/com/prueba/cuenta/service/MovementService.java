package com.prueba.cuenta.service;

import com.prueba.cuenta.dto.MovementDTO;
import com.prueba.cuenta.dto.MovementReportDTO;
import com.prueba.cuenta.entity.Account;
import com.prueba.cuenta.entity.Movement;
import com.prueba.cuenta.entity.MovementType;
import com.prueba.cuenta.exception.AccountBusinessException;
import com.prueba.cuenta.repository.AccountRepository;
import com.prueba.cuenta.repository.MovementRepository;
import com.prueba.cuenta.service.client.ClientService;
import com.prueba.cuenta.utils.ApiResponse;
import com.prueba.cuenta.utils.ResponseProcess;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovementService {

    private static final String SUCCESS_CODE = "0";
    private static final String ERROR_CODE = "1";
    private static final String SUCCESS_STATUS = "OK";
    private static final String ERROR_STATUS = "ERROR";
    private static final String SUCCESS_MESSAGE = "Operación exitosa";
    private static final String REPORT_SUCCESS_CODE = "200";
    private static final String REPORT_ERROR_CODE = "500";

    private final MovementRepository movementRepository;
    private final AccountRepository accountRepository;
    private final ClientService clientService;

    @Transactional
    public Mono<ApiResponse<Movement>> createMovement(MovementDTO movementDTO) {
        log.info("Creando movimiento con los datos: {}", movementDTO);

        return Mono.fromCallable(() -> {
                    Account account = findAccountById(Integer.valueOf(movementDTO.getCuentaId()));
                    BigDecimal newBalance = calculateNewBalance(account, movementDTO.getValor());
                    validateSufficientBalance(newBalance, movementDTO.getCuentaId());

                    Movement movement = createMovementEntity(movementDTO, account, newBalance);
                    updateAccountBalance(account, newBalance);

                    Movement savedMovement = movementRepository.save(movement);
                    return createSuccessResponse(savedMovement, "Movimiento creado correctamente");
                })
                .onErrorResume(e -> {
                    log.error("Error al crear el movimiento: {}", e.getMessage());
                    return Mono.just(createErrorResponse(e.getMessage()));
                });
    }

    public Flux<ApiResponse<Movement>> getAllMovements() {
        return Flux.fromIterable(movementRepository.findAll())
                .map(movement -> createSuccessResponse(movement, "Consulta exitosa"))
                .onErrorResume(e -> {
                    log.error("Error al obtener los movimientos: {}", e.getMessage());
                    return Flux.just(createErrorResponse(e.getMessage()));
                });
    }

    public Flux<ApiResponse<Movement>> getMovementsByAccount(Integer accountId) {
        return Flux.fromIterable(movementRepository.findByCuenta_NumeroCuenta(accountId))
                .map(movement -> createSuccessResponse(movement, "Consulta exitosa"))
                .onErrorResume(e -> {
                    log.error("Error al obtener los movimientos por cuenta: {}", e.getMessage());
                    return Flux.just(createErrorResponse(e.getMessage()));
                });
    }

    public Mono<ApiResponse<List<MovementReportDTO>>> generateReport(
            Integer accountId, LocalDate startDate, LocalDate endDate) {

        return Mono.fromCallable(() -> {
                    Account account = findAccountById(accountId);
                    return movementRepository.findByCuenta_NumeroCuentaAndFechaBetween(accountId, startDate, endDate);
                })
                .flatMapMany(Flux::fromIterable)
                .collectList()
                .flatMap(movements -> buildReportFromMovements(movements))
                .map(report -> {
                    ResponseProcess responseProcess = new ResponseProcess(
                            REPORT_SUCCESS_CODE, "Reporte generado exitosamente", SUCCESS_STATUS);
                    return new ApiResponse<>(report, responseProcess);
                })
                .onErrorResume(e -> {
                    log.error("Error al generar el reporte: {}", e.getMessage());
                    ResponseProcess responseProcess = new ResponseProcess(REPORT_ERROR_CODE, e.getMessage(), ERROR_STATUS);
                    return Mono.just(new ApiResponse<>(null, responseProcess));
                });
    }

    // Métodos privados de ayuda
    private Account findAccountById(Integer accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountBusinessException("Cuenta no encontrada"));
    }

    private BigDecimal calculateNewBalance(Account account, BigDecimal transactionAmount) {
        return account.getSaldo().add(transactionAmount);
    }

    private void validateSufficientBalance(BigDecimal newBalance, String accountId) {
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            log.error("Saldo no disponible para la cuenta: {}", accountId);
            throw new AccountBusinessException("Saldo no disponible");
        }
    }

    private Movement createMovementEntity(MovementDTO movementDTO, Account account, BigDecimal newBalance) {
        Movement movement = new Movement();
        movement.setId(MDC.get("uuid"));
        movement.setFecha(LocalDate.parse(movementDTO.getFecha()));
        movement.setTipoMovimiento(determineMovementType(movementDTO.getValor()));
        movement.setValor(movementDTO.getValor());
        movement.setCuenta(account);
        movement.setSaldo(newBalance);
        return movement;
    }

    private void updateAccountBalance(Account account, BigDecimal newBalance) {
        account.setSaldo(newBalance);
        accountRepository.save(account);
    }

    private MovementType determineMovementType(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) > 0 ? MovementType.DEPOSITO : MovementType.RETIRO;
    }

    private <T> ApiResponse<T> createSuccessResponse(T data, String message) {
        ResponseProcess responseProcess = new ResponseProcess(SUCCESS_CODE, message, SUCCESS_STATUS);
        return new ApiResponse<>(data, responseProcess);
    }

    private <T> ApiResponse<T> createErrorResponse(String errorMessage) {
        ResponseProcess responseProcess = new ResponseProcess(ERROR_CODE, errorMessage, ERROR_STATUS);
        return new ApiResponse<>(null, responseProcess);
    }

    private Mono<List<MovementReportDTO>> buildReportFromMovements(List<Movement> movements) {
        if (movements.isEmpty()) {
            return Mono.just(List.of());
        }

        return clientService.getClientName(movements.get(0).getCuenta().getClienteId())
                .map(clientName -> movements.stream()
                        .map(movement -> mapToReportDTO(movement, clientName))
                        .collect(Collectors.toList()));
    }

    private MovementReportDTO mapToReportDTO(Movement movement, String clientName) {
        MovementReportDTO dto = new MovementReportDTO();
        dto.setFecha(movement.getFecha());
        dto.setCliente(clientName);
        dto.setNumeroCuenta(movement.getCuenta().getNumeroCuenta().toString());
        dto.setTipo(movement.getCuenta().getTipoCuenta().toString());
        dto.setSaldoInicial(movement.getSaldo().subtract(movement.getValor()));
        dto.setEstado(movement.getCuenta().isStatus());
        dto.setMovimiento(movement.getValor());
        dto.setSaldoDisponible(movement.getSaldo());
        return dto;
    }
}