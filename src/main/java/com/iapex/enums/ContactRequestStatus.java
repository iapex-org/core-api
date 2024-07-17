package com.iapex.enums;

public enum ContactRequestStatus {
    NUEVA("Nueva"),
    EN_REVISION("En revisión"),
    ENCONTRADA("Encontrada"),
    NO_ENCONTRADA("No encontrada");

    private final String displayName;

    ContactRequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}