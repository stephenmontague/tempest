package app.tempest.oms.temporal.activities.impl;

import org.springframework.stereotype.Component;

import app.tempest.common.temporal.activities.oms.RandomDAGActivities;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of RandomDAGActivities.
 * These are stub activities that simulate work with logging and sleep.
 */
@Slf4j
@Component
public class RandomDAGActivitiesImpl implements RandomDAGActivities {

    @Override
    public void allocate(String demoId) {
        log.info("🔵 [ALLOCATE] Starting allocation - demoId: {}", demoId);
        simulateWork(2000);
        log.info("✅ [ALLOCATE] Allocation complete - demoId: {}", demoId);
    }

    @Override
    public void sendEmail(String demoId) {
        log.info("📧 [EMAIL] Sending notification email - demoId: {}", demoId);
        simulateWork(1500);
        log.info("✅ [EMAIL] Email sent - demoId: {}", demoId);
    }

    @Override
    public void printLabel(String demoId) {
        log.info("🏷️  [PRINT_LABEL] Generating shipping label - demoId: {}", demoId);
        simulateWork(2500);
        log.info("✅ [PRINT_LABEL] Label printed - demoId: {}", demoId);
    }

    @Override
    public void ship(String demoId) {
        log.info("📦 [SHIP] Confirming shipment - demoId: {}", demoId);
        simulateWork(1800);
        log.info("✅ [SHIP] Shipment confirmed - demoId: {}", demoId);
    }

    /**
     * Simulate work by sleeping for the specified duration.
     * 
     * @param millis Duration in milliseconds
     */
    private void simulateWork(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Activity interrupted during simulated work", e);
        }
    }
}
