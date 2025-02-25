package com.prueba.cuenta.dto;

import lombok.Data;

@Data
public class ClientDetailsDTO {
    private Long id;
    private String nombre;
    private String genero;
    private int edad;
    private String identificacion;
    private String direccion;
    private String telefono;
    private String clienteId;
    private String contrasena;
    private boolean estado;
}
