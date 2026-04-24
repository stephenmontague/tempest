package app.tempest.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
		"app.tempest.common.config",
		"app.tempest.worker"
})
public class WorkflowWorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkflowWorkerApplication.class, args);
	}
}
