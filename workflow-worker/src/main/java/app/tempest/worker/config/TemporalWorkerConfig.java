package app.tempest.worker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import app.tempest.common.temporal.TaskQueues;
import app.tempest.worker.workflow.impl.OrderIntakeWorkflowImpl;
import app.tempest.worker.workflow.impl.RandomDAGWorkflowImpl;
import app.tempest.worker.workflow.impl.WaveExecutionWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class TemporalWorkerConfig {

    @Bean
    public WorkerFactory workerFactory(WorkflowClient workflowClient) {
        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);

        Worker worker = factory.newWorker(TaskQueues.WORKFLOWS);

        // Register all workflow implementations — no activities
        worker.registerWorkflowImplementationTypes(
                OrderIntakeWorkflowImpl.class,
                RandomDAGWorkflowImpl.class,
                WaveExecutionWorkflowImpl.class);

        log.info("Starting workflow worker on task queue: {}", TaskQueues.WORKFLOWS);
        factory.start();

        return factory;
    }
}
