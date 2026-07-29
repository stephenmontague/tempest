package app.tempest.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Tempest unified REST/CRUD API.
 *
 * One application that serves CRUD for every domain (orders, items, waves, shipments)
 * against the single `tempest` database. It is NOT a Temporal client and runs no worker —
 * the Next.js UI is the Temporal client, and the four worker apps execute activities.
 *
 * Component/entity/repository scanning is rooted at `app.tempest` so the controllers
 * (this module), CRUD services + entities + repositories (tempest-domain), and shared
 * SecurityConfig (tempest-common) are all picked up. `tempest-temporal` is deliberately
 * not a dependency, so no Temporal connection is ever opened here.
 */
@SpringBootApplication(scanBasePackages = "app.tempest")
@EntityScan(basePackages = "app.tempest")
@EnableJpaRepositories(basePackages = "app.tempest")
public class TempestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TempestApiApplication.class, args);
    }
}
