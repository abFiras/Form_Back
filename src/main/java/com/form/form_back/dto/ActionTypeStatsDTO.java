package com.form.form_back.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionTypeStatsDTO {
    private String actionType;
    private String actionTypeLabel;
    private Long count;
    private String percentage;
    private String color;

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionTypeLabel() {
        return actionTypeLabel;
    }

    public void setActionTypeLabel(String actionTypeLabel) {
        this.actionTypeLabel = actionTypeLabel;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}