package app.tempest.sms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import app.tempest.common.temporal.TaskQueues;
import app.tempest.sms.temporal.activities.impl.ConfirmShipmentActivityImpl;
import app.tempest.sms.temporal.activities.impl.CreateShipmentActivityImpl;
import app.tempest.sms.temporal.activities.impl.FetchFedExRatesActivityImpl;
import app.tempest.sms.temporal.activities.impl.FetchRatesActivityImpl;
import app.tempest.sms.temporal.activities.impl.FetchUPSRatesActivityImpl;
import app.tempest.sms.temporal.activities.impl.FetchUSPSRatesActivityImpl;
import app.tempest.sms.temporal.activities.impl.GenerateShippingLabelActivityImpl;
import app.tempest.sms.temporal.activities.impl.SelectRateActivityImpl;
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
               CreateShipmentActivityImpl createShipmentActivity,
               GenerateShippingLabelActivityImpl generateShippingLabelActivity,
               ConfirmShipmentActivityImpl confirmShipmentActivity,
               FetchRatesActivityImpl fetchRatesActivity,
               SelectRateActivityImpl selectRateActivity,
               FetchUSPSRatesActivityImpl fetchUSPSRatesActivity,
               FetchUPSRatesActivityImpl fetchUPSRatesActivity,
               FetchFedExRatesActivityImpl fetchFedExRatesActivity) {

          WorkerFactory factory = WorkerFactory.newInstance(workflowClient);

          Worker worker = factory.newWorker(TaskQueues.SMS);

          // Register all SMS activities (Spring-managed beans for DI)
          worker.registerActivitiesImplementations(
                    createShipmentActivity,
                    generateShippingLabelActivity,
                    confirmShipmentActivity,
                    fetchRatesActivity,
                    selectRateActivity,
                    fetchUSPSRatesActivity,
                    fetchUPSRatesActivity,
                    fetchFedExRatesActivity);

          log.info("Starting SMS Temporal worker on task queue: {}", TaskQueues.SMS);
          factory.start();

          return factory;
     }
}
