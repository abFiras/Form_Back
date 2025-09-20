package com.form.form_back.dto;

import com.form.form_back.Entity.Group;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data

public class GroupDTO {
    private Long id;
    private String name;
    private String description;
    private String color;
    private Boolean active;

    public GroupDTO(Group group) {
        if (group != null) {
            this.id = group.getId();
            this.name = group.getName();
            this.description = group.getDescription();
            this.color = group.getColor();
            this.active = group.getActive();
        }
    }

    public GroupDTO() {
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}