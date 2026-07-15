package com.pranjal.pulsetable.streamingdata.repository;

import com.pranjal.pulsetable.streamingdata.entity.StreamColEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StreamColRepository extends JpaRepository<StreamColEntity, Long> {
    List<StreamColEntity> findByStream_StreamId(Long streamId);

    //List<StreamColEntity> findByStreamId(Long streamId);
    //List<StreamColEntity> findByStreamId(Long streamId);

}
