package com.algaworks.algasensors.device.management.api.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class RestClientConfig {

    @Bean
    public SensorMonitoringClient sensorMonitoringClient(final RestClientFactory factory) {
        final RestClient restClient = factory.temperatureMonitoringRestClient();

        final RestClientAdapter adapter = RestClientAdapter.create(restClient);
        final HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory.builderFor(adapter).build();

        return proxyFactory.createClient(SensorMonitoringClient.class);
    }
}
