package com.prueba.cuenta.dto;

import lombok.Data;

@Data
public class ClientResponseDTO {
    private ClientDetailsDTO details;
    private ResponseProcessDTO responseProcess;
}
