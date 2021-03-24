package io.quarkus.config.yaml.runtime;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

import io.smallrye.config.AbstractLocationConfigSourceLoader;
import io.smallrye.config.source.yaml.YamlConfigSource;

public class ApplicationYamlConfigSourceLoader extends AbstractLocationConfigSourceLoader {
    @Override
    protected String[] getFileExtensions() {
        return new String[] {
                "yaml",
                "yml"
        };

    }

    @Override
    protected ConfigSource loadConfigSource(final URL url, final int ordinal) throws IOException {
        return new YamlConfigSource(url, ordinal);
    }

    public static class InClassPath extends ApplicationYamlConfigSourceLoader implements ConfigSourceProvider {
        @Override
        protected ConfigSource loadConfigSource(final URL url, final int ordinal) throws IOException {
            return super.loadConfigSource(url, 255);
        }

        @Override
        public List<ConfigSource> getConfigSources(final ClassLoader classLoader) {
            List<ConfigSource> configSources = new ArrayList<>();
            configSources.addAll(loadConfigSources("application.yaml", classLoader));
            configSources.addAll(loadConfigSources("application.yml", classLoader));
            return configSources;
        }

        @Override
        protected List<ConfigSource> tryFileSystem(final URI uri) {
            return new ArrayList<>();
        }
    }

    public static class InFileSystem extends ApplicationYamlConfigSourceLoader implements ConfigSourceProvider {
        @Override
        protected ConfigSource loadConfigSource(final URL url, final int ordinal) throws IOException {
            return super.loadConfigSource(url, 265);
        }

        @Override
        public List<ConfigSource> getConfigSources(final ClassLoader classLoader) {
            List<ConfigSource> configSources = new ArrayList<>();
            configSources.addAll(loadConfigSources("config/application.yaml", classLoader));
            configSources.addAll(loadConfigSources("config/application.yml", classLoader));
            return configSources;
        }

        @Override
        protected List<ConfigSource> tryClassPath(final URI uri, final ClassLoader classLoader) {
            return new ArrayList<>();
        }
    }
}
