package app.tempest.ims.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import app.tempest.common.temporal.TaskQueues;
import app.tempest.ims.temporal.activities.impl.ImsActivitiesImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class TemporalWorkerConfig {

    @Bean
    @ConditionalOnProperty(name = "temporal.worker.enabled", havingValue = "true", matchIfMissing = true)
    public WorkerFactory workerFactory(
            WorkflowClient workflowClient,
            ImsActivitiesImpl imsActivities) {

        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);

        Worker worker = factory.newWorker(TaskQueues.ACTIVITIES);

        // Register IMS activities
        worker.registerActivitiesImplementations(imsActivities);

        log.info("Starting IMS activity worker on task queue: {}", TaskQueues.ACTIVITIES);
        factory.start();

        return factory;
    }
}
