package com.prueba.cuenta.controller;

import com.prueba.cuenta.dto.AccountDTO;
import com.prueba.cuenta.entity.Account;
import com.prueba.cuenta.service.AccountService;
import com.prueba.cuenta.utils.ApiResponse;
import com.prueba.cuenta.utils.ResponseProcess;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/cuentas")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<Account>>> createAccount(@Valid @RequestBody AccountDTO accountDTO) {
        return accountService.createAccount(accountDTO)
                .map(response -> {
                    if (response.getResponseProcess() != null &&
                            !"0".equals(response.getResponseProcess().getCode())) {
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }
                    return new ResponseEntity<>(response, HttpStatus.CREATED);
                })
                .onErrorResume(e -> {
                    ResponseProcess responseProcess = new ResponseProcess("1", e.getMessage(), "ERROR");
                    ApiResponse<Account> errorResponse = new ApiResponse<>(null, responseProcess);
                    return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<Account>>>> getAllAccounts() {
        return accountService.getAllAccounts()
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK))
                .onErrorResume(e -> {
                    ResponseProcess responseProcess = new ResponseProcess("1", e.getMessage(), "ERROR");
                    ApiResponse<List<Account>> errorResponse = new ApiResponse<>(null, responseProcess);
                    return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

    @GetMapping("/{numeroCuenta}")
    public Mono<ResponseEntity<ApiResponse<Account>>> getAccountById(@PathVariable Integer numeroCuenta) {
        return accountService.getAccountById(numeroCuenta)
                .map(response -> {
                    if (response.getResponseProcess() != null &&
                            !"0".equals(response.getResponseProcess().getCode())) {
                        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                    }
                    return new ResponseEntity<>(response, HttpStatus.OK);
                })
                .onErrorResume(e -> {
                    ResponseProcess responseProcess = new ResponseProcess("1", e.getMessage(), "ERROR");
                    ApiResponse<Account> errorResponse = new ApiResponse<>(null, responseProcess);
                    return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

    @PutMapping("/{numeroCuenta}")
    public Mono<ResponseEntity<ApiResponse<Account>>> updateAccount(
            @PathVariable Integer numeroCuenta,
            @Valid @RequestBody AccountDTO accountDTO) {
        return accountService.updateAccount(numeroCuenta, accountDTO)
                .map(response -> {
                    if (response.getResponseProcess() != null &&
                            !"0".equals(response.getResponseProcess().getCode())) {
                        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                    }
                    return new ResponseEntity<>(response, HttpStatus.OK);
                })
                .onErrorResume(e -> {
                    ResponseProcess responseProcess = new ResponseProcess("1", e.getMessage(), "ERROR");
                    ApiResponse<Account> errorResponse = new ApiResponse<>(null, responseProcess);
                    return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

}