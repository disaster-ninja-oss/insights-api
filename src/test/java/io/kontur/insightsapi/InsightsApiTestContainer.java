package io.kontur.insightsapi;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class InsightsApiTestContainer extends PostgreSQLContainer<InsightsApiTestContainer> {

    private static final String IMAGE_VERSION = "postgis/postgis:13-master";

    private static InsightsApiTestContainer container;

    private InsightsApiTestContainer() {
        super(DockerImageName.parse(IMAGE_VERSION).asCompatibleSubstituteFor("postgres"));
    }

    public static InsightsApiTestContainer getInstance() {
        if (container == null) {
            container = new InsightsApiTestContainer();
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("DB_URL", container.getJdbcUrl());
        System.setProperty("DB_USERNAME", container.getUsername());
        System.setProperty("DB_PASSWORD", container.getPassword());
    }

    @Override
    public void stop() {
        //do nothing, JVM handles shut down
    }
}
