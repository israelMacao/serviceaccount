package com.prueba.cuenta.dto;

import com.prueba.cuenta.entity.AccountType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountDTO {
    @NotBlank (message = "El número de cuenta no puede ser nulo")
    @Pattern(regexp = "^[0-9]+$", message = "El número de cuenta solo debe contener números")
    @Size(max = 10, message = "El campo 'numeroCuenta' no puede tener más de 10 dígitos")
    private String numeroCuenta;
    @NotBlank(message = "La tipoCuenta del cliente no puede estar vacía")
    private String tipoCuenta;
    @NotNull(message = "El saldo inicial no puede ser nulo")
    @DecimalMin(value = "0.0", inclusive = true, message = "El saldo inicial debe ser mayor o igual a 0")
    @Digits(integer = 15, fraction = 2, message = "El valor debe tener como máximo 15 dígitos enteros y 2 decimales")
    private BigDecimal saldoInicial;
    @NotBlank(message = "El estado no puede ser vacio")
    @Pattern(regexp = "true|false", message = "El campo estado debe ser 'true' o 'false'")
    private String estado;
    @NotBlank(message = "La identificación del cliente no puede estar vacía")
    @Pattern(regexp = "^[0-9]{10}$", message = "Identificación debe tener 10 dígitos")
    private String identificacion;
}
