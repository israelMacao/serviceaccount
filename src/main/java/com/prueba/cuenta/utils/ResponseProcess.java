package com.prueba.cuenta.utils;

import lombok.Data;

@Data
public class ResponseProcess {
    private String code;
    private String resultMessage;
    private String technicalMessage;

    public ResponseProcess(String code, String resultMessage, String technicalMessage) {
        this.code = code;
        this.resultMessage = resultMessage;
        this.technicalMessage = technicalMessage;
    }
}
