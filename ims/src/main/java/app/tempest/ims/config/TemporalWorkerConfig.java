package app.tempest.ims.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import app.tempest.common.temporal.TaskQueues;
import app.tempest.ims.temporal.activities.impl.AllocateInventoryActivityImpl;
import app.tempest.ims.temporal.activities.impl.ConsumeInventoryActivityImpl;
import app.tempest.ims.temporal.activities.impl.ReleaseInventoryActivityImpl;
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
               AllocateInventoryActivityImpl allocateInventoryActivity,
               ReleaseInventoryActivityImpl releaseInventoryActivity,
               ConsumeInventoryActivityImpl consumeInventoryActivity) {

          WorkerFactory factory = WorkerFactory.newInstance(workflowClient);

          Worker worker = factory.newWorker(TaskQueues.IMS);

          // Register all IMS activities (Spring-managed beans for DI)
          worker.registerActivitiesImplementations(
                    allocateInventoryActivity,
                    releaseInventoryActivity,
                    consumeInventoryActivity);

          log.info("Starting IMS Temporal worker on task queue: {}", TaskQueues.IMS);
          factory.start();

          return factory;
     }
}
