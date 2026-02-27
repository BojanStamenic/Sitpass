package com.svtKvt.sitpass.dto;

public enum RatingCategory {
    EQUIPMENT("equipment"),
    STAFF("staff"),
    HYGIENE("hygiene"),
    SPACE("space");

    private final String dbField;

    RatingCategory(String dbField) {
        this.dbField = dbField;
    }

    public String dbField() {
        return dbField;
    }
}
