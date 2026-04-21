package com.smartcampus.exception;

public class SensorNotFoundException extends RuntimeException {
    public SensorNotFoundException(String sensorId) {
        super("Sensor not found: " + sensorId);
    }
}
