package io.quarkus.mongodb;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.mongodb.client.MongoClient;
import com.mongodb.event.CommandStartedEvent;

import io.quarkus.test.QuarkusUnitTest;

public class MongoCommandListenerTest extends MongoTestBase {

    @Inject
    MongoClient client;

    @Inject
    MockCommandListener listener;

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(
                    () -> ShrinkWrap.create(JavaArchive.class).addClasses(MongoTestBase.class, MockCommandListener.class))
            .withConfigurationResource("default-mongoclient.properties");

    @AfterEach
    void cleanup() {
        if (client != null) {
            client.close();
        }
    }

    @Test
    void testClientInitialization() {
        assertThat(client.listDatabaseNames().first()).isNotEmpty();
        assertThat(listener).isNotNull();
        CommandStartedEvent startedEvent = listener.getCommandStartedEvent();
        assertThat(startedEvent).isNotNull();
        assertThat(startedEvent.getCommandName()).isEqualTo("listDatabases");
    }

}
