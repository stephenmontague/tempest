package app.tempest.ims.config;

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
    public WorkerFactory workerFactory(
            WorkflowClient workflowClient,
            ImsActivitiesImpl imsActivities) {

        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);

        Worker worker = factory.newWorker(TaskQueues.IMS);

        // Register consolidated IMS activities for cross-service calls
        worker.registerActivitiesImplementations(imsActivities);

        log.info("Starting IMS Temporal worker on task queue: {}", TaskQueues.IMS);
        factory.start();

        return factory;
    }
}
