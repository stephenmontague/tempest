package app.tempest.common.temporal.activities.oms;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * Activities for the RandomDAGWorkflow demo.
 * These are stub activities that simulate work with logging and sleep.
 */
@ActivityInterface
public interface RandomDAGActivities {

    /**
     * Simulate inventory allocation.
     *
     * @param demoId The demo execution ID for correlation
     */
    @ActivityMethod
    void allocate(String demoId);

    /**
     * Simulate sending an email notification.
     *
     * @param demoId The demo execution ID for correlation
     */
    @ActivityMethod
    void sendEmail(String demoId);

    /**
     * Simulate printing a shipping label.
     *
     * @param demoId The demo execution ID for correlation
     */
    @ActivityMethod
    void printLabel(String demoId);

    /**
     * Simulate shipping confirmation.
     *
     * @param demoId The demo execution ID for correlation
     */
    @ActivityMethod
    void ship(String demoId);
}
