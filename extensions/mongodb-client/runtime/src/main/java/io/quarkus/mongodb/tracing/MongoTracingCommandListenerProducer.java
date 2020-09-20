package io.quarkus.mongodb.tracing;

import javax.enterprise.inject.spi.CDI;

import com.mongodb.event.CommandListener;

import io.opentracing.Tracer;
import io.opentracing.contrib.mongo.common.TracingCommandListener;

public class MongoTracingCommandListenerProducer {

    public static CommandListener createMongoTracingCommandListener() {
        Tracer tracer = CDI.current().select(Tracer.class).get();
        return new TracingCommandListener.Builder(tracer).build();
    }

}
