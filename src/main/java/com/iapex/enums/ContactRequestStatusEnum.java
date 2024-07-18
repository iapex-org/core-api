package com.iapex.enums;

public enum ContactRequestStatusEnum {
    NUEVA("Nueva"),
    EN_REVISION("En revisión"),
    ENCONTRADA("Encontradao"),
    NO_ENCONTRADA("No encontrado");

    private final String displayName;

    ContactRequestStatusEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}