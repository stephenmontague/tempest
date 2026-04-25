package app.tempest.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(
		scanBasePackages = {
				"app.tempest.common.config",
				"app.tempest.worker"
		},
		exclude = {
				DataSourceAutoConfiguration.class,
				HibernateJpaAutoConfiguration.class
		}
)
public class WorkflowWorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkflowWorkerApplication.class, args);
	}
}
