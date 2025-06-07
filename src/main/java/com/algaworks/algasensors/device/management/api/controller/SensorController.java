package com.algaworks.algasensors.device.management.api.controller;

import com.algaworks.algasensors.device.management.api.client.SensorMonitoringClient;
import com.algaworks.algasensors.device.management.api.model.SensorDetailOutput;
import com.algaworks.algasensors.device.management.api.model.SensorInput;
import com.algaworks.algasensors.device.management.api.model.SensorMonitoringOuput;
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
    private final SensorMonitoringClient sensorMonitoringClient;


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

    @GetMapping("{sensorId}/detail")
    public SensorDetailOutput getOneWithDetail(@PathVariable TSID sensorId) {
        Sensor sensor = sensorRepository.findById(new SensorId(sensorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        SensorMonitoringOuput monitoringOuput = sensorMonitoringClient.getDetail(sensorId);
        SensorOutput sensorOutput = convertToModel(sensor);

        return SensorDetailOutput.builder()
                .monitoring(monitoringOuput)
                .sensor(sensorOutput)
                .build();
    }


    @PutMapping("{sensorId}")
    public SensorOutput update(
            @PathVariable final TSID sensorId,
            @RequestBody final SensorInput input) {
        final Sensor sensor = this.sensorRepository.findById(new SensorId(sensorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        sensor.setName(input.name());
        sensor.setLocation(input.location());
        sensor.setProtocol(input.protocol());
        sensor.setModel(input.model());
        return this.convertToModel(this.sensorRepository.save(sensor));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SensorOutput crate(@RequestBody final SensorInput input) {
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

    @DeleteMapping("/{sensorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final TSID sensorId) {
        Sensor sensor = sensorRepository.findById(new SensorId(sensorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        sensorRepository.delete(sensor);

        sensorMonitoringClient.disableMonitoring(sensorId);
    }

    @PutMapping("/{sensorId}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable(@PathVariable TSID sensorId) {
        Sensor sensor = sensorRepository.findById(new SensorId(sensorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        sensor.setEnabled(true);
        sensorRepository.save(sensor);

        sensorMonitoringClient.enableMonitoring(sensorId);
    }

    @DeleteMapping("/{sensorId}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable final TSID sensorId) {
        Sensor sensor = sensorRepository.findById(new SensorId(sensorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        sensor.setEnabled(false);
        sensorRepository.save(sensor);

        sensorMonitoringClient.disableMonitoring(sensorId);
    }

    private SensorOutput convertToModel(final Sensor sensorSaved) {
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
