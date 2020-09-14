package io.quarkus.mongodb.runtime;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.event.CommandListener;
import com.mongodb.event.ConnectionPoolListener;

public class MongoClientSupport {

    private final List<String> codecProviders;
    private final List<String> bsonDiscriminators;
    private final List<ConnectionPoolListener> connectionPoolListeners;
    private final List<CommandListener> commandListeners;
    private final boolean disableSslSupport;

    public MongoClientSupport(List<String> codecProviders, List<String> bsonDiscriminators,
            List<ConnectionPoolListener> connectionPoolListeners, boolean disableSslSupport) {
        this.codecProviders = codecProviders;
        this.bsonDiscriminators = bsonDiscriminators;
        this.connectionPoolListeners = connectionPoolListeners;
        this.commandListeners = new ArrayList<>();
        this.disableSslSupport = disableSslSupport;
    }

    public MongoClientSupport(List<String> codecProviders, List<String> bsonDiscriminators,
            List<ConnectionPoolListener> connectionPoolListeners, List<CommandListener> commandListeners,
            boolean disableSslSupport) {
        this.codecProviders = codecProviders;
        this.bsonDiscriminators = bsonDiscriminators;
        this.connectionPoolListeners = connectionPoolListeners;
        this.commandListeners = commandListeners;
        this.disableSslSupport = disableSslSupport;
    }

    public List<String> getCodecProviders() {
        return codecProviders;
    }

    public List<String> getBsonDiscriminators() {
        return bsonDiscriminators;
    }

    public List<ConnectionPoolListener> getConnectionPoolListeners() {
        return connectionPoolListeners;
    }

    public List<CommandListener> getCommandListeners() {
        return commandListeners;
    }

    public void addCommandListener(CommandListener commandListener) {
        commandListeners.add(commandListener);
    }

    public boolean isDisableSslSupport() {
        return disableSslSupport;
    }
}
