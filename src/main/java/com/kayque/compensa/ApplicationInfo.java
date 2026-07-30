package com.kayque.compensa;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class ApplicationInfo {

    private static final String RESOURCE =
            "/com/kayque/compensa/application.properties";

    private static final Properties PROPERTIES =
            loadProperties();

    public static final String NAME =
            getRequiredProperty("application.name");

    public static final String VERSION =
            getRequiredProperty("application.version");

    public static final String DEVELOPER =
            getRequiredProperty("application.developer");

    private ApplicationInfo() {
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();

        try (
                InputStream inputStream =
                        ApplicationInfo.class
                                .getResourceAsStream(RESOURCE)
        ) {
            if (inputStream == null) {
                throw new IllegalStateException(
                        "Os metadados do aplicativo não foram encontrados."
                );
            }

            try (
                    InputStreamReader reader =
                            new InputStreamReader(
                                    inputStream,
                                    StandardCharsets.UTF_8
                            )
            ) {
                properties.load(reader);
            }

            return properties;

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Não foi possível carregar os metadados do aplicativo.",
                    exception
            );
        }
    }

    private static String getRequiredProperty(
            String propertyName
    ) {
        String value = PROPERTIES.getProperty(propertyName);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "O metadado "
                            + propertyName
                            + " não foi configurado."
            );
        }

        return value.trim();
    }
}