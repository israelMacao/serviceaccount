package com.prueba.cuenta.dto;

import com.prueba.cuenta.entity.MovementType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MovementDTO {

    @NotNull(message = "La fecha es obligatoria")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Formato de fecha inválido (yyyy-MM-dd)")
    private String fecha;
    @NotNull(message = "El valor no puede ser nulo")
    @Digits(integer = 15, fraction = 2, message = "El valor debe tener como máximo 15 dígitos enteros y 2 decimales")
    private BigDecimal valor;
    @NotBlank(message = "El número de cuenta no puede ser nulo")
    @Pattern(regexp = "^[0-9]+$", message = "El número de cuenta solo debe contener números")
    @Size(max = 10, message = "El campo 'numeroCuenta' no puede tener más de 10 dígitos")
    private String cuentaId;
}
