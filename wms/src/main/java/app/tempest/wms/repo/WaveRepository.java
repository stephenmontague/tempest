package app.tempest.wms.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.tempest.wms.entity.Wave;
import app.tempest.wms.entity.Wave.WaveStatus;

@Repository
public interface WaveRepository extends JpaRepository<Wave, Long> {

     Optional<Wave> findByTenantIdAndId(String tenantId, Long id);

     Optional<Wave> findByTenantIdAndWaveNumber(String tenantId, String waveNumber);

     List<Wave> findByTenantIdAndFacilityIdAndStatus(String tenantId, Long facilityId, WaveStatus status);

     List<Wave> findByTenantIdAndStatus(String tenantId, WaveStatus status);

     List<Wave> findByTenantIdAndFacilityId(String tenantId, Long facilityId);

     List<Wave> findByTenantId(String tenantId);

     boolean existsByTenantIdAndWaveNumber(String tenantId, String waveNumber);
}
