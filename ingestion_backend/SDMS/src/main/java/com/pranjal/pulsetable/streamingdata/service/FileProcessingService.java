package com.pranjal.pulsetable.streamingdata.service;


import com.pranjal.pulsetable.streamingdata.entity.StreamColEntity;
import com.pranjal.pulsetable.streamingdata.entity.StreamMasterEntity;
import com.pranjal.pulsetable.streamingdata.entity.StreamQueryEntity;
import com.pranjal.pulsetable.streamingdata.repository.StreamColRepository;
import com.pranjal.pulsetable.streamingdata.repository.StreamMasterRepository;
import com.pranjal.pulsetable.streamingdata.repository.StreamQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

//@Service
//public class FileProcessingService {
//
//    @Autowired
//    private TextDataRepository textDataRepository;
//    @Autowired
//    private StreamMasterRepository streamMasterRepository;
//
//    @Autowired
//    private StreamColRepository streamColRepository;
//
//    @Autowired
//    private StreamQueryRepository streamQueryRepository;
//
//    public void saveStreamMaster(StreamMasterEntity streamMaster) {
//
//        streamMasterRepository.save(streamMaster);
//    }
//
//    public List<StreamMasterEntity> getAllStreamMasters() {
//        return streamMasterRepository.findAll();
//    }
//
//
//    public boolean saveStreamCol(StreamColEntity streamCol) {
//        if(streamMasterRepository.existsById(streamCol.getStreamId())) {
//            streamColRepository.save(streamCol);
//            return true;
//        }
//        return false;
//    }
//
//    public boolean saveStreamQuery(StreamQueryEntity streamQuery) {
//        if(streamMasterRepository.existsById(streamQuery.getStreamId())
//                && streamColRepository.existsById(streamQuery.getStreamColId())) {
//            streamQueryRepository.save(streamQuery);
//            return true;
//        }
//        return false;
//
//    }
//}

@Service
public class FileProcessingService {

    @Autowired
    private StreamMasterRepository streamMasterRepository;

    @Autowired
    private StreamColRepository streamColRepository;

    @Autowired
    private StreamQueryRepository streamQueryRepository;

    public void saveStreamMaster(StreamMasterEntity streamMaster) {
        streamMasterRepository.save(streamMaster);
    }

    public List<StreamMasterEntity> getAllStreamMasters() {
        return streamMasterRepository.findAll();
    }


    public boolean saveStreamCol(StreamColEntity streamCol) {
        StreamMasterEntity incomingStream = streamCol.getStream();

        if (incomingStream != null && incomingStream.getStreamId() != null) {
            int streamId = Math.toIntExact(incomingStream.getStreamId());

            // getting stream entity
            StreamMasterEntity streamMaster = streamMasterRepository.findById(Long.valueOf(streamId)).orElse(null);

            if (streamMaster != null) {
                streamCol.setStream(streamMaster); // set managed entity
                streamColRepository.save(streamCol); // save column
                return true;
            }
        }

        return false;
    }


    public List<StreamColEntity> getAllStreamCols() {
        return streamColRepository.findAll();
    }


    public boolean saveStreamQuery(StreamQueryEntity streamQuery) {
        if (streamMasterRepository.existsById(streamQuery.getStreamId())
                && streamColRepository.existsById(streamQuery.getStreamColId())) {
            streamQueryRepository.save(streamQuery);
            return true;
        }
        return false;
    }

    public StreamMasterEntity savesStreamMaster(StreamMasterEntity streamMaster) {
        System.out.println("DataSourceType: " + streamMaster.getDataSourceType());
        System.out.println("DataSourcePath: " + streamMaster.getDataSourcePath());
        return streamMasterRepository.save(streamMaster);
    }
}
