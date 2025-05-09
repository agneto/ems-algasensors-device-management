package com.algaworks.algasensors.device.management.api.model;

public record SensorInput(
        String name,
        String ip,
        String location,
        String protocol,
        String model) {
}
