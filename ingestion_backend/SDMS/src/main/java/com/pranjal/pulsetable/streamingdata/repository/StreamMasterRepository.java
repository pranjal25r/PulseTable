package com.pranjal.pulsetable.streamingdata.repository;

import com.pranjal.pulsetable.streamingdata.entity.StreamMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreamMasterRepository extends JpaRepository<StreamMasterEntity, Long> {
}
