package io.quarkus.mongodb;

import javax.enterprise.context.ApplicationScoped;

import com.mongodb.event.CommandListener;
import com.mongodb.event.CommandStartedEvent;

import io.quarkus.test.Mock;

@Mock
@ApplicationScoped
public class MockCommandListener implements CommandListener {

    private CommandStartedEvent commandStartedEvent;

    @Override
    public void commandStarted(CommandStartedEvent event) {
        this.commandStartedEvent = event;
    }

    public CommandStartedEvent getCommandStartedEvent() {
        return commandStartedEvent;
    }

}