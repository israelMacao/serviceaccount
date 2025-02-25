package com.prueba.cuenta.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
@Entity
@Table(name = "cuenta")
public class Account {
    @Id
    @Column(name = "numerocuenta", nullable = false)
    private Integer numeroCuenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipocuenta", nullable = false)
    private AccountType tipoCuenta;

    @Column(name = "saldoinicial", nullable = false)
    private BigDecimal saldo;

    @Column(name = "estado",nullable = false)
    private boolean status;

    @Column(name = "clienteid", nullable = false)
    private Long clienteId;

}

