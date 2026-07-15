package com.pranjal.pulsetable.streamingdata.repository;

import com.pranjal.pulsetable.streamingdata.entity.StreamQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreamQueryRepository extends JpaRepository<StreamQueryEntity, Long> {
}
