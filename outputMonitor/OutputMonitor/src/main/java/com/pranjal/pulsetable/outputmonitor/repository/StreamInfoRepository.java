package com.pranjal.pulsetable.outputmonitor.repository;

import com.pranjal.pulsetable.outputmonitor.dto.StreamInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreamInfoRepository extends JpaRepository<StreamInfo, Long> {
}
