package app.tempest.oms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import app.tempest.common.temporal.TaskQueues;
import app.tempest.oms.temporal.activities.impl.CreateOrderActivityImpl;
import app.tempest.oms.temporal.activities.impl.MarkOrderAwaitingWaveActivityImpl;
import app.tempest.oms.temporal.activities.impl.MarkOrderReservedActivityImpl;
import app.tempest.oms.temporal.activities.impl.MarkOrderShippedActivityImpl;
import app.tempest.oms.temporal.activities.impl.OmsActivitiesImpl;
import app.tempest.oms.temporal.activities.impl.ValidateOrderActivityImpl;
import app.tempest.oms.temporal.workflow.impl.OrderFulfillmentWorkflowImpl;
import app.tempest.oms.temporal.workflow.impl.OrderIntakeWorkflowImpl;
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
               ValidateOrderActivityImpl validateOrderActivity,
               CreateOrderActivityImpl createOrderActivity,
               MarkOrderAwaitingWaveActivityImpl markOrderAwaitingWaveActivity,
               MarkOrderReservedActivityImpl markOrderReservedActivity,
               MarkOrderShippedActivityImpl markOrderShippedActivity,
               OmsActivitiesImpl omsActivities) {

          WorkerFactory factory = WorkerFactory.newInstance(workflowClient);

          Worker worker = factory.newWorker(TaskQueues.OMS);

          // Register workflow implementations
          worker.registerWorkflowImplementationTypes(
                    OrderIntakeWorkflowImpl.class,
                    OrderFulfillmentWorkflowImpl.class);

          // Register activity implementations (Spring-managed beans for DI)
          worker.registerActivitiesImplementations(
                    validateOrderActivity,
                    createOrderActivity,
                    markOrderAwaitingWaveActivity,
                    markOrderReservedActivity,
                    markOrderShippedActivity,
                    omsActivities);  // Remote activities for cross-service calls

          log.info("Starting OMS Temporal worker on task queue: {}", TaskQueues.OMS);
          factory.start();

          return factory;
     }
}
