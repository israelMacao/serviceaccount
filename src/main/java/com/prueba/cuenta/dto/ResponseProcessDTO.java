package com.prueba.cuenta.dto;

import lombok.Data;

@Data
public class ResponseProcessDTO {
    private String code;
    private String resultMessage;
    private String technicalMessage;

    public ResponseProcessDTO(String code, String resultMessage, String technicalMessage) {
        this.code = code;
        this.resultMessage = resultMessage;
        this.technicalMessage = technicalMessage;
    }

}
