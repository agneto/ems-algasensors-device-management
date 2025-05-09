package com.algaworks.algasensors.device.management.api.model;

import io.hypersistence.tsid.TSID;

public record SensorOutput(
        TSID id,
        String name,
        String ip,
        String location,
        String protocol,
        String model,
        Boolean enabled
) {
}
