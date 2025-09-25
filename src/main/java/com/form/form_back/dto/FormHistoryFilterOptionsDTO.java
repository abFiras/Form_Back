package com.form.form_back.dto;


import java.util.List;

public class FormHistoryFilterOptionsDTO {
    private List<ActionTypeOptionDTO> actionTypes;
    private List<StatusOptionDTO> statusOptions;

    // getters/setters
    public List<ActionTypeOptionDTO> getActionTypes() { return actionTypes; }
    public void setActionTypes(List<ActionTypeOptionDTO> actionTypes) { this.actionTypes = actionTypes; }
    public List<StatusOptionDTO> getStatusOptions() { return statusOptions; }
    public void setStatusOptions(List<StatusOptionDTO> statusOptions) { this.statusOptions = statusOptions; }
}


