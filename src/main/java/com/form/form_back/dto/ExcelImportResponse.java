package com.form.form_back.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DTO pour la réponse d'importation Excel
 */
public class ExcelImportResponse {
    private boolean success;
    private String message;
    private ExternalListDTO data;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    public ExcelImportResponse() {}

    public ExcelImportResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ExternalListDTO getData() {
        return data;
    }

    public void setData(ExternalListDTO data) {
        this.data = data;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}

/**
 * DTO pour le résultat de validation Excel
 */
