package com.algaworks.algasensors.device.management.api.controller;

import com.algaworks.algasensors.device.management.api.model.SensorInput;
import com.algaworks.algasensors.device.management.api.model.SensorOutput;
import com.algaworks.algasensors.device.management.common.IdGenerator;
import com.algaworks.algasensors.device.management.domain.model.Sensor;
import com.algaworks.algasensors.device.management.domain.model.SensorId;
import com.algaworks.algasensors.device.management.domain.repository.SensorRepository;
import io.hypersistence.tsid.TSID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {

    private final SensorRepository sensorRepository;


    @GetMapping
    public Page<SensorOutput> search(@PageableDefault final Pageable pageable) {
        final Page<Sensor> sensors = this.sensorRepository.findAll(pageable);
        return sensors.map(this::convertToModel);
    }

    @GetMapping("{sensorId}")
    public SensorOutput getOne(@PathVariable final TSID sensorId) {
        final Sensor sensor = this.sensorRepository.findById(new SensorId(sensorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return this.convertToModel(sensor);
    }

    @PutMapping("{sensorId}")
    public SensorOutput update(@PathVariable final TSID sensorId, @RequestBody SensorInput input) {
        final Sensor sensor = this.sensorRepository.findById(new SensorId(sensorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        sensor.setName(input.name());
        sensor.setLocation(input.location());
        sensor.setProtocol(input.protocol());
        sensor.setModel(input.model());
        return this.convertToModel(this.sensorRepository.save(sensor));
    }

    @DeleteMapping("{sensorId}")
    public ResponseEntity<Void> delete(@PathVariable final TSID sensorId) {
        final Sensor sensor = this.sensorRepository.findById(new SensorId(sensorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        this.sensorRepository.delete(sensor);

        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SensorOutput crate(@RequestBody SensorInput input) {
        final Sensor sensor = Sensor
                .builder()
                .id(new SensorId(IdGenerator.generateTSID()))
                .name(input.name())
                .ip(input.ip())
                .location(input.location())
                .protocol(input.protocol())
                .model(input.model())
                .enabled(false)
                .build();

        final Sensor sensorSaved = this.sensorRepository.saveAndFlush(sensor);

        return this.convertToModel(sensorSaved);
    }

    private SensorOutput convertToModel(Sensor sensorSaved) {
        return new SensorOutput(
                sensorSaved.getId().getValue(),
                sensorSaved.getName(),
                sensorSaved.getIp(),
                sensorSaved.getLocation(),
                sensorSaved.getProtocol(),
                sensorSaved.getModel(),
                sensorSaved.getEnabled());
    }

}
