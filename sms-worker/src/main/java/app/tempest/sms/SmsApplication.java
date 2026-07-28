package app.tempest.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * SMS worker application — runs the Temporal worker on the sms-tasks queue (SMS + carrier
 * rate activities). No REST controllers (CRUD is served by tempest-api). Scans app.tempest
 * so this module's worker config, TemporalConfig (tempest-temporal), and shared domain
 * repos/entities (tempest-domain) are all picked up.
 */
@SpringBootApplication(scanBasePackages = "app.tempest")
@EntityScan(basePackages = "app.tempest")
@EnableJpaRepositories(basePackages = "app.tempest")
public class SmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmsApplication.class, args);
	}

}
