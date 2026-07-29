package app.tempest.wms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import app.tempest.common.temporal.TaskQueues;
import app.tempest.wms.temporal.activities.impl.UpdateWaveStatusActivityImpl;
import app.tempest.wms.temporal.activities.impl.WmsActivitiesImpl;
import app.tempest.wms.temporal.workflow.impl.WaveExecutionWorkflowImpl;
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
            WmsActivitiesImpl wmsActivities,
            UpdateWaveStatusActivityImpl updateWaveStatusActivity) {

        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);

        Worker worker = factory.newWorker(TaskQueues.WMS);

        // Register workflow implementations
        worker.registerWorkflowImplementationTypes(WaveExecutionWorkflowImpl.class);

        // Register consolidated WMS activities for cross-service calls
        // Plus internal WMS activities
        worker.registerActivitiesImplementations(
                wmsActivities,
                updateWaveStatusActivity);

        log.info("Starting WMS Temporal worker on task queue: {}", TaskQueues.WMS);
        factory.start();

        return factory;
    }
}
