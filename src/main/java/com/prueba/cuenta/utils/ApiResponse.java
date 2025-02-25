package com.prueba.cuenta.utils;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private T details;
    private ResponseProcess responseProcess;

    public ApiResponse(T details, ResponseProcess responseProcess) {
        this.details = details;
        this.responseProcess = responseProcess;
    }
}
