package com.smartsolutions.smart_solutions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponseDTO {
    private boolean success;
    private String message;
    private Object data;

    public ApiResponseDTO(boolean success, String message) {
        this(success, message, null);
    }
}
