package com.bishop.synthetic_human.conf;

import com.bishop.synthetic_human.service.CommandService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.*;

@Configuration
public class MetricsConfig {

    @Bean
    public Gauge queueSizeGauge(CommandService svc, MeterRegistry registry) {
        return Gauge.builder("android.queue.size", svc, s ->
                s.getCommonExecutor().getQueue().size()
        ).register(registry);
    }
}
