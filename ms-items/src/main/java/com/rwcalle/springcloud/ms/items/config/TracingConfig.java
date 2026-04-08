package com.rwcalle.springcloud.ms.items.config;

import brave.Tracing;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.brave.bridge.BraveCurrentTraceContext;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracingConfig {

    @Bean
    public Tracing braveTracing() {
        var currentTraceContext = ThreadLocalCurrentTraceContext.newBuilder()
                .addScopeDecorator(MDCScopeDecorator.get())
                .build();
        return Tracing.newBuilder()
                .currentTraceContext(currentTraceContext)
                .sampler(Sampler.ALWAYS_SAMPLE)
                .build();
    }

    @Bean
    public Tracer micrometerTracer(Tracing tracing) {
        BraveCurrentTraceContext braveCurrentTraceContext =
                new BraveCurrentTraceContext(tracing.currentTraceContext());
        return new BraveTracer(tracing.tracer(), braveCurrentTraceContext);
    }

    @Bean
    public DefaultTracingObservationHandler tracingObservationHandler(Tracer tracer) {
        return new DefaultTracingObservationHandler(tracer);
    }
}
