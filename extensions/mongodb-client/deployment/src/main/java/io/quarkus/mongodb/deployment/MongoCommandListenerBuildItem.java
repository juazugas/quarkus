package io.quarkus.mongodb.deployment;

import java.util.function.Supplier;

import com.mongodb.event.CommandListener;

import io.quarkus.builder.item.MultiBuildItem;

public final class MongoCommandListenerBuildItem extends MultiBuildItem {
    private Supplier<CommandListener> commandListener;

    public MongoCommandListenerBuildItem(Supplier<CommandListener> commandListener) {
        this.commandListener = commandListener;
    }

    public Supplier<CommandListener> getCommandListener() {
        return commandListener;
    }
}
