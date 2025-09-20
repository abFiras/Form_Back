package com.form.form_back.dto;

import lombok.Data;

@Data
public class FormStatsResponseDTO {
    private Long formId;
    private String formName;
    private Integer totalSubmissions;
    private Integer authenticatedSubmissions;
    private Integer anonymousSubmissions;
    private String status;
    private String createdAt;

    // Getters et setters
    public Long getFormId() { return formId; }
    public void setFormId(Long formId) { this.formId = formId; }

    public String getFormName() { return formName; }
    public void setFormName(String formName) { this.formName = formName; }

    public Integer getTotalSubmissions() { return totalSubmissions; }
    public void setTotalSubmissions(Integer totalSubmissions) { this.totalSubmissions = totalSubmissions; }

    public Integer getAuthenticatedSubmissions() { return authenticatedSubmissions; }
    public void setAuthenticatedSubmissions(Integer authenticatedSubmissions) { this.authenticatedSubmissions = authenticatedSubmissions; }

    public Integer getAnonymousSubmissions() { return anonymousSubmissions; }
    public void setAnonymousSubmissions(Integer anonymousSubmissions) { this.anonymousSubmissions = anonymousSubmissions; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}