package com.trongthang.survivaloverhaul.mechanics.temperature;

public enum TemperatureState {
    FREEZING(0),
    COLD(1),
    NORMAL(2),
    WARM(3),
    HOT(4);

    private final int id;

    TemperatureState(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static TemperatureState fromId(int id) {
        for (TemperatureState state : values()) {
            if (state.id == id)
                return state;
        }
        return NORMAL;
    }
}
