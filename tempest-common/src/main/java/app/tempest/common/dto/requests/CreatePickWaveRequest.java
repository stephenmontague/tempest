package app.tempest.common.dto.requests;

import java.util.List;

import app.tempest.common.dto.PickItemDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to create pick tasks for an order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePickWaveRequest {
     private Long orderId;
     private Long facilityId;
     private String strategy;
     private List<PickItemDTO> items;
}

