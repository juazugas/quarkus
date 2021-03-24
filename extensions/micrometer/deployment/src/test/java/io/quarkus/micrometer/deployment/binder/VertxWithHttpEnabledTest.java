package io.quarkus.micrometer.deployment.binder;

import static io.restassured.RestAssured.when;

import java.net.URL;
import java.util.Map;
import java.util.regex.Pattern;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.micrometer.runtime.binder.HttpBinderConfiguration;
import io.quarkus.micrometer.runtime.binder.vertx.VertxMeterBinderAdapter;
import io.quarkus.micrometer.test.PingPongResource;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.http.TestHTTPResource;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpServerMetrics;

public class VertxWithHttpEnabledTest {
    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withConfigurationResource("test-logging.properties")
            .overrideConfigKey("quarkus.micrometer.binder-enabled-default", "false")
            .overrideConfigKey("quarkus.micrometer.binder.http-client.enabled", "true")
            .overrideConfigKey("quarkus.micrometer.binder.http-server.enabled", "true")
            .overrideConfigKey("quarkus.micrometer.binder.http-server.ignore-patterns", "/http")
            .overrideConfigKey("quarkus.micrometer.binder.vertx.enabled", "true")
            .overrideConfigKey("quarkus.micrometer.binder.vertx.match-patterns", "/one=/two")
            .overrideConfigKey("quarkus.micrometer.binder.vertx.ignore-patterns", "/two")
            .overrideConfigKey("pingpong/mp-rest/url", "${test.url}")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(PingPongResource.class, PingPongResource.PingPongRestClient.class));

    @TestHTTPResource
    URL url;

    @Inject
    Instance<VertxMeterBinderAdapter> vertxMeterBinderAdapterInstance;

    @Inject
    HttpBinderConfiguration httpBinderConfiguration;

    @Inject
    MeterRegistry registry;

    @Test
    public void testMetricFactoryCreatedMetrics() throws Exception {
        Assertions.assertTrue(httpBinderConfiguration.isClientEnabled());
        Assertions.assertTrue(httpBinderConfiguration.isServerEnabled());

        // Vertx Binder should exist
        Assertions.assertTrue(vertxMeterBinderAdapterInstance.isResolvable());
        VertxMeterBinderAdapter adapter = vertxMeterBinderAdapterInstance.get();

        HttpServerMetrics metrics = adapter.createHttpServerMetrics(new HttpServerOptions(), new SocketAddress() {
            @Override
            public String host() {
                return "a.b.c";
            }

            @Override
            public int port() {
                return 0;
            }

            @Override
            public String path() {
                return null;
            }
        });

        Assertions.assertNotNull(metrics);
        Assertions.assertTrue(httpBinderConfiguration.isServerEnabled());

        // prefer http-server.ignore-patterns
        Assertions.assertEquals(1, httpBinderConfiguration.getServerIgnorePatterns().size());
        Pattern p = httpBinderConfiguration.getServerIgnorePatterns().get(0);
        Assertions.assertTrue(p.matcher("/http").matches());

        // Use vertx.match-patterns (http-server version is missing)
        Assertions.assertEquals(1, httpBinderConfiguration.getServerMatchPatterns().size());
        Map.Entry<Pattern, String> entry = httpBinderConfiguration.getServerMatchPatterns().entrySet().iterator().next();
        Assertions.assertTrue(entry.getKey().matcher("/one").matches());
        Assertions.assertEquals("/two", entry.getValue());

        // If you invoke requests, http server and client meters should be registered

        when().get("/ping/one").then().statusCode(200);
        when().get("/ping/two").then().statusCode(200);
        when().get("/ping/three").then().statusCode(200);

        // For server: /ping/{message} and /pong/{message}.
        Assertions.assertNotEquals(0, registry.find("http.server.requests").timers().size());
        // For client: /pong/{message}
        Assertions.assertNotEquals(0, registry.find("http.client.requests").timers().size());
    }
}
