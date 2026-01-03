package app.tempest.common.dto.requests;

import java.util.List;

import app.tempest.common.dto.PickItemDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to create pick tasks for a wave.
 * A wave can contain items from multiple orders.
 * Each PickItemDTO includes its orderId for tracking.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePickWaveRequest {
     private Long waveId;
     private Long facilityId;
     private String strategy;
     private List<PickItemDTO> items;
}

