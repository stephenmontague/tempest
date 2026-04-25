package app.tempest.oms.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import app.tempest.common.temporal.TaskQueues;
import app.tempest.oms.temporal.activities.impl.CreateOrderActivityImpl;
import app.tempest.oms.temporal.activities.impl.MarkOrderAwaitingWaveActivityImpl;
import app.tempest.oms.temporal.activities.impl.MarkOrderReservedActivityImpl;
import app.tempest.oms.temporal.activities.impl.MarkOrderShippedActivityImpl;
import app.tempest.oms.temporal.activities.impl.OmsActivitiesImpl;
import app.tempest.oms.temporal.activities.impl.RandomDAGActivitiesImpl;
import app.tempest.oms.temporal.activities.impl.ValidateOrderActivityImpl;
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
               ValidateOrderActivityImpl validateOrderActivity,
               CreateOrderActivityImpl createOrderActivity,
               MarkOrderAwaitingWaveActivityImpl markOrderAwaitingWaveActivity,
               MarkOrderReservedActivityImpl markOrderReservedActivity,
               MarkOrderShippedActivityImpl markOrderShippedActivity,
               OmsActivitiesImpl omsActivities,
               RandomDAGActivitiesImpl randomDAGActivities) {

          WorkerFactory factory = WorkerFactory.newInstance(workflowClient);

          Worker worker = factory.newWorker(TaskQueues.OMS);

          // Register activity implementations only — workflows run on the standalone workflow worker
          worker.registerActivitiesImplementations(
                    validateOrderActivity,
                    createOrderActivity,
                    markOrderAwaitingWaveActivity,
                    markOrderReservedActivity,
                    markOrderShippedActivity,
                    omsActivities,
                    randomDAGActivities);

          log.info("Starting OMS activity worker on task queue: {}", TaskQueues.OMS);
          factory.start();

          return factory;
     }
}
