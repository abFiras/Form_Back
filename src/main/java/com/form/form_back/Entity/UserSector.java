package com.form.form_back.Entity;

public enum UserSector {
    COMPTABILITE("Comptabilité"),
    RH("Ressources Humaines"),
    IT("Informatique"),
    COMMERCIAL("Commercial"),
    PRODUCTION("Production"),
    DIRECTION("Direction"),
    MARKETING("Marketing"),
    ACHATS("Achats"),
    QUALITE("Qualité"),
    MAINTENANCE("Maintenance");

    private final String displayName;

    UserSector(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}