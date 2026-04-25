package app.tempest.wms.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import app.tempest.common.temporal.TaskQueues;
import app.tempest.wms.temporal.activities.impl.UpdateWaveStatusActivityImpl;
import app.tempest.wms.temporal.activities.impl.WmsActivitiesImpl;
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
            WmsActivitiesImpl wmsActivities,
            UpdateWaveStatusActivityImpl updateWaveStatusActivity) {

        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);

        Worker worker = factory.newWorker(TaskQueues.WMS);

        // Register activity implementations only — workflows run on the standalone workflow worker
        worker.registerActivitiesImplementations(
                wmsActivities,
                updateWaveStatusActivity);

        log.info("Starting WMS activity worker on task queue: {}", TaskQueues.WMS);
        factory.start();

        return factory;
    }
}
