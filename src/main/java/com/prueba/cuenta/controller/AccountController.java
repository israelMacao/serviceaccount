package com.prueba.cuenta.controller;

import com.prueba.cuenta.dto.AccountDTO;
import com.prueba.cuenta.entity.Account;
import com.prueba.cuenta.service.AccountService;
import com.prueba.cuenta.utils.ApiResponseClient;
import com.prueba.cuenta.utils.ResponseProcess;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/cuentas")
@Tag(name = "Account Controller", description = "API para la gestión de cuentas")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Operation(summary = "Crear una nueva cuenta", description = "Crea una nueva cuenta con los datos proporcionados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cuenta creada exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping
    public Mono<ResponseEntity<ApiResponseClient<Account>>> createAccount(@Valid @RequestBody AccountDTO accountDTO) {
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
                    ApiResponseClient<Account> errorResponse = new ApiResponseClient<>(null, responseProcess);
                    return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

    @Operation(summary = "Obtener todas las cuentas", description = "Obtiene una lista de todas las cuentas registradas")
    @GetMapping
    public Mono<ResponseEntity<ApiResponseClient<List<Account>>>> getAllAccounts() {
        return accountService.getAllAccounts()
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK))
                .onErrorResume(e -> {
                    ResponseProcess responseProcess = new ResponseProcess("1", e.getMessage(), "ERROR");
                    ApiResponseClient<List<Account>> errorResponse = new ApiResponseClient<>(null, responseProcess);
                    return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

    @Operation(summary = "Obtener una cuenta por ID", description = "Obtiene una cuenta específica por su número de cuenta")
    @GetMapping("/{numeroCuenta}")
    public Mono<ResponseEntity<ApiResponseClient<Account>>> getAccountById(@PathVariable Integer numeroCuenta) {
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
                    ApiResponseClient<Account> errorResponse = new ApiResponseClient<>(null, responseProcess);
                    return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

    @Operation(summary = "Actualizar una cuenta", description = "Actualiza una cuenta existente con los datos proporcionados")
    @PutMapping("/{numeroCuenta}")
    public Mono<ResponseEntity<ApiResponseClient<Account>>> updateAccount(
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
                    ApiResponseClient<Account> errorResponse = new ApiResponseClient<>(null, responseProcess);
                    return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

}