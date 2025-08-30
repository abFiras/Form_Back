package com.form.form_back.dto;

import java.time.LocalDateTime;


public class LibraryFormDTO {
    private Long id;
    private Long originalFormId;
    private String name;
    private String description;
    private String origin;
    private String language;
    private Integer fieldCount;
    private Integer viewCount;
    private Integer downloadCount;
    private String sharedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String tags;

    // Constructeurs
    public LibraryFormDTO() {}

    public LibraryFormDTO(Long id, Long originalFormId, String name, String description,
                          String origin, String language, Integer fieldCount,
                          Integer viewCount, Integer downloadCount, String sharedBy,
                          LocalDateTime createdAt, LocalDateTime updatedAt, String tags) {
        this.id = id;
        this.originalFormId = originalFormId;
        this.name = name;
        this.description = description;
        this.origin = origin;
        this.language = language;
        this.fieldCount = fieldCount;
        this.viewCount = viewCount;
        this.downloadCount = downloadCount;
        this.sharedBy = sharedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.tags = tags;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOriginalFormId() {
        return originalFormId;
    }

    public void setOriginalFormId(Long originalFormId) {
        this.originalFormId = originalFormId;
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

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getFieldCount() {
        return fieldCount;
    }

    public void setFieldCount(Integer fieldCount) {
        this.fieldCount = fieldCount;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public String getSharedBy() {
        return sharedBy;
    }

    public void setSharedBy(String sharedBy) {
        this.sharedBy = sharedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    // Méthodes utilitaires
    public Integer getTotalPopularity() {
        return (viewCount != null ? viewCount : 0) + (downloadCount != null ? downloadCount : 0);
    }

    @Override
    public String toString() {
        return "LibraryFormDTO{" +
                "id=" + id +
                ", originalFormId=" + originalFormId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", origin='" + origin + '\'' +
                ", language='" + language + '\'' +
                ", fieldCount=" + fieldCount +
                ", viewCount=" + viewCount +
                ", downloadCount=" + downloadCount +
                ", sharedBy='" + sharedBy + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", tags='" + tags + '\'' +
                '}';
    }
}