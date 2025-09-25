package com.form.form_back.dto;

public class ActionTypeOptionDTO {
    private String value;
    private String label;
    private String color;

    public ActionTypeOptionDTO(String value, String label, String color) {
        this.value = value;
        this.label = label;
        this.color = color;
    }

    // getters/setters
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
