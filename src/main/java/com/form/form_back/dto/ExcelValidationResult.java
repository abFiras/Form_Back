package com.form.form_back.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExcelValidationResult {
    private boolean isValid;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private List<Map<String, Object>> previewData = new ArrayList<>();
    private int totalRows;
    private List<String> headers = new ArrayList<>();

    public ExcelValidationResult() {}

    // Getters and Setters
    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
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

    public List<Map<String, Object>> getPreviewData() {
        return previewData;
    }

    public void setPreviewData(List<Map<String, Object>> previewData) {
        this.previewData = previewData;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }
}