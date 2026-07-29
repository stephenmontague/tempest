package app.tempest.oms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import app.tempest.common.temporal.TaskQueues;
import app.tempest.oms.temporal.activities.impl.MarkOrderReservedActivityImpl;
import app.tempest.oms.temporal.activities.impl.MarkOrderShippedActivityImpl;
import app.tempest.oms.temporal.activities.impl.OmsActivitiesImpl;
import app.tempest.oms.temporal.activities.impl.RandomDAGActivitiesImpl;
import app.tempest.oms.temporal.workflow.impl.RandomDAGWorkflowImpl;
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
               MarkOrderReservedActivityImpl markOrderReservedActivity,
               MarkOrderShippedActivityImpl markOrderShippedActivity,
               OmsActivitiesImpl omsActivities,
               RandomDAGActivitiesImpl randomDAGActivities) {

          WorkerFactory factory = WorkerFactory.newInstance(workflowClient);

          Worker worker = factory.newWorker(TaskQueues.OMS);

          // Register workflow implementations.
          // (Order intake is now a plain CRUD endpoint, not a workflow.)
          worker.registerWorkflowImplementationTypes(
                    RandomDAGWorkflowImpl.class);

          // Register activity implementations (Spring-managed beans for DI)
          worker.registerActivitiesImplementations(
                    markOrderReservedActivity,
                    markOrderShippedActivity,
                    omsActivities, // Remote activities for cross-service calls (WaveExecutionWorkflow)
                    randomDAGActivities); // Demo activities

          log.info("Starting OMS Temporal worker on task queue: {}", TaskQueues.OMS);
          factory.start();

          return factory;
     }
}
