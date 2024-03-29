package com.onyshchenko.psanalyzer.model;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum DeviceType {

    PS5("PS5"),
    PS4("PS4"),
    PS3("PS3"),
    PS2("PS2"),
    PSV("PS Vita"),
    PSP("PSP");

    private static final Map<String, DeviceType> map = new HashMap<>(values().length, 1);

    static {
        for (DeviceType deviceType : values()) {
            map.put(deviceType.deviceName, deviceType);
        }
    }

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "device_name")
    private final String deviceName;

    DeviceType(String deviceName) {
        this.deviceName = deviceName;
    }

    public static DeviceType of(String deviceName) {
        DeviceType result = map.get(deviceName);
        if (result == null) {
            throw new IllegalArgumentException("Invalid genre name [" + deviceName + "]");
        }
        return result;
    }
}
