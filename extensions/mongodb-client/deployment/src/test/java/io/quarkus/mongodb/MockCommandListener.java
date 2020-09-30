package io.quarkus.mongodb;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.event.CommandListener;
import com.mongodb.event.CommandStartedEvent;

public class MockCommandListener implements CommandListener {

    public static final List<String> events = new ArrayList<>();

    @Override
    public void commandStarted(CommandStartedEvent startedEvent) {
        events.add(startedEvent.getCommandName());
    }

}