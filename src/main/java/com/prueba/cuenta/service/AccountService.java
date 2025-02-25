package com.prueba.cuenta.service;

import com.prueba.cuenta.dto.AccountDTO;
import com.prueba.cuenta.entity.Account;
import com.prueba.cuenta.entity.AccountType;
import com.prueba.cuenta.exception.AccountBusinessException;
import com.prueba.cuenta.repository.AccountRepository;
import com.prueba.cuenta.service.client.ClientService;
import com.prueba.cuenta.utils.ApiResponse;
import com.prueba.cuenta.utils.ResponseProcess;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private static final String SUCCESS_CODE = "0";
    private static final String ERROR_CODE = "1";
    private static final String BAD_REQUEST_CODE = "400";
    private static final String SUCCESS_STATUS = "OK";
    private static final String ERROR_STATUS = "ERROR";

    private final AccountRepository accountRepository;
    private final ClientService clientService;

    @Transactional
    public Mono<ApiResponse<Account>> createAccount(AccountDTO accountDTO) {
        String uuid = MDC.get("uuid");
        log.info("[UUID: {}] Inicia proceso de creación de cuenta: {}", uuid, accountDTO.getNumeroCuenta());

        // Validaciones previas
        if (accountExists(Integer.valueOf(accountDTO.getNumeroCuenta()))) {
            return createErrorResponse("El número de cuenta ya existe", BAD_REQUEST_CODE);
        }

        if (!isValidAccountType(accountDTO.getTipoCuenta())) {
            return createErrorResponse("El tipo de cuenta debe ser AHORROS o CORRIENTE", BAD_REQUEST_CODE);
        }

        return clientService.getClientIdByIdentification(accountDTO.getIdentificacion())
                .flatMap(clientId -> {
                    Account account = createAccountEntity(accountDTO, clientId);
                    accountRepository.save(account);
                    log.info("Cuenta creada: {}", account);
                    return createSuccessResponse(account, "Cuenta creada correctamente");
                })
                .onErrorResume(e -> {
                    log.error("Error al crear la cuenta: {}", e.getMessage());
                    return createErrorResponse(e.getMessage(), ERROR_CODE);
                });
    }

    public Mono<ApiResponse<List<Account>>> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        log.info("Obteniendo todas las cuentas: {} registros encontrados", accounts.size());
        return createSuccessResponse(accounts, "Consulta exitosa");
    }

    public Mono<ApiResponse<Account>> getAccountById(Integer accountNumber) {
        return Mono.fromCallable(() -> findAccountById(accountNumber))
                .map(account -> {
                    log.info("Cuenta encontrada: {}", account);
                    return createSuccessResponseWithData(account, "Cuenta encontrada correctamente");
                })
                .onErrorResume(e -> {
                    log.error("Error al buscar la cuenta: {}", e.getMessage());
                    return handleAccountFindError(e);
                });
    }

    @Transactional
    public Mono<ApiResponse<Account>> updateAccount(Integer accountNumber, AccountDTO accountDTO) {
        if (!isValidAccountType(accountDTO.getTipoCuenta())) {
            return createErrorResponse("El tipo de cuenta debe ser AHORROS o CORRIENTE", BAD_REQUEST_CODE);
        }

        return Mono.fromCallable(() -> {
                    Account account = findAccountByNumber(accountNumber);
                    updateAccountFields(account, accountDTO);
                    accountRepository.save(account);
                    log.info("Cuenta actualizada: {}", account);
                    return createSuccessResponseWithData(account, "Cuenta actualizada correctamente");
                })
                .onErrorResume(e -> {
                    log.error("Error al actualizar la cuenta: {}", e.getMessage());
                    return createErrorResponse(e.getMessage(), ERROR_CODE);
                });
    }

    // Métodos privados auxiliares
    private boolean accountExists(Integer accountNumber) {
        return accountRepository.findByNumeroCuenta(accountNumber) != null;
    }

    private boolean isValidAccountType(String accountType) {
        return accountType != null &&
                (accountType.equals(AccountType.AHORROS.toString()) ||
                        accountType.equals(AccountType.CORRIENTE.toString()));
    }

    private Account createAccountEntity(AccountDTO dto, Long clientId) {
        Account account = new Account();
        account.setNumeroCuenta(Integer.valueOf(dto.getNumeroCuenta()));
        account.setTipoCuenta(AccountType.valueOf(dto.getTipoCuenta()));
        account.setSaldo(dto.getSaldoInicial());
        account.setStatus(Boolean.parseBoolean(dto.getEstado()));
        account.setClienteId(clientId);
        return account;
    }

    private Account findAccountById(Integer accountNumber) {
        return accountRepository.findById(accountNumber)
                .orElseThrow(() -> new AccountBusinessException("Cuenta no encontrada: " + accountNumber));
    }

    private Account findAccountByNumber(Integer accountNumber) {
        Account account = accountRepository.findByNumeroCuenta(accountNumber);
        if (account == null) {
            throw new AccountBusinessException("Cuenta no encontrada: " + accountNumber);
        }
        return account;
    }

    private void updateAccountFields(Account account, AccountDTO dto) {
        if (dto.getTipoCuenta() != null) {
            account.setTipoCuenta(AccountType.valueOf(dto.getTipoCuenta()));
        }
        if (dto.getEstado() != null) {
            account.setStatus(Boolean.parseBoolean(dto.getEstado()));
        }
    }

    private <T> Mono<ApiResponse<T>> createSuccessResponse(T data, String message) {
        ResponseProcess responseProcess = new ResponseProcess(SUCCESS_CODE, message, SUCCESS_STATUS);
        return Mono.just(new ApiResponse<>(data, responseProcess));
    }

    private <T> ApiResponse<T> createSuccessResponseWithData(T data, String message) {
        ResponseProcess responseProcess = new ResponseProcess(SUCCESS_CODE, message, SUCCESS_STATUS);
        return new ApiResponse<>(data, responseProcess);
    }

    private <T> Mono<ApiResponse<T>> createErrorResponse(String errorMessage, String errorCode) {
        ResponseProcess responseProcess = new ResponseProcess(errorCode, errorMessage, ERROR_STATUS);
        return Mono.just(new ApiResponse<>(null, responseProcess));
    }

    private <T> Mono<ApiResponse<T>> handleAccountFindError(Throwable e) {
        if (e instanceof AccountBusinessException) {
            return createErrorResponse(e.getMessage(), ERROR_CODE);
        }
        return createErrorResponse("Error interno al buscar la cuenta", ERROR_CODE);
    }
}