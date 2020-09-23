package io.quarkus.mongodb;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.mongodb.event.CommandListener;
import com.mongodb.event.CommandStartedEvent;

public class MockCommandListener implements CommandListener {

    private CommandStartedEvent commandStartedEvent;

    @Override
    public void commandStarted(CommandStartedEvent startedEvent) {
        this.commandStartedEvent = startedEvent;
        assertThat(startedEvent, notNullValue());
        assertThat(startedEvent.getCommandName(), anyOf(equalTo("listDatabases"), equalTo("endSessions")));
    }

    public CommandStartedEvent getCommandStartedEvent() {
        return commandStartedEvent;
    }

}