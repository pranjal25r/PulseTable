package com.pranjal.pulsetable.streamingdata.controller;

import com.pranjal.pulsetable.streamingdata.entity.DataSourceRequest;
import com.pranjal.pulsetable.streamingdata.entity.StreamColEntity;
import com.pranjal.pulsetable.streamingdata.entity.StreamMasterEntity;
import com.pranjal.pulsetable.streamingdata.entity.StreamQueryEntity;
import com.pranjal.pulsetable.streamingdata.repository.StreamColRepository;
import com.pranjal.pulsetable.streamingdata.service.DataSourceService;
import com.pranjal.pulsetable.streamingdata.service.FileProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ingestion")
public class FileController {

    @Autowired
    private FileProcessingService fileProcessingService;

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> badName(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("/stream-master")
    public ResponseEntity<Map<String, Long>> createStreamMaster(@RequestBody StreamMasterEntity streamMaster) {
        System.out.println("Received StreamMasterEntity: " + streamMaster);

        StreamMasterEntity savedStream = fileProcessingService.savesStreamMaster(streamMaster);

        // Prepare the response with only streamId
        Map<String, Long> response = new HashMap<>();
        response.put("streamId", savedStream.getStreamId()); // Only include the streamId in the response

        return ResponseEntity.ok(response); // Send only the streamId in the response
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("/stream-col")
    public ResponseEntity<String> createStreamCol(@RequestBody StreamColEntity streamCol) {
        System.out.println("Received StreamColEntity: " + streamCol);
        boolean result = fileProcessingService.saveStreamCol(streamCol);
        if(result)
            return ResponseEntity.ok("Stream column saved successfully!");
        else
            return ResponseEntity.status(400).body("Failed to save the info");
    }


    @Autowired
    private StreamColRepository streamColRepository;

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/stream-col/{streamId}")
    public ResponseEntity<List<StreamColEntity>> getStreamColsByStreamId(@PathVariable Long streamId) {
        List<StreamColEntity> cols = streamColRepository.findByStream_StreamId(streamId);
        return ResponseEntity.ok(cols);
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("/stream-query")
    public ResponseEntity<String> createStreamQuery(@RequestBody StreamQueryEntity streamQuery) {
        boolean result = fileProcessingService.saveStreamQuery(streamQuery);

        if(result)
            return ResponseEntity.ok("Stream Query saved successfully!");
        else
            return ResponseEntity.status(400).body("Failed to save the info");
    }

    @PostMapping("/generate_tables")
    public ResponseEntity<String> generateTables(@RequestBody StreamQueryEntity streamQuery) {
        boolean result = fileProcessingService.saveStreamQuery(streamQuery);

        if(result)
            return ResponseEntity.ok("Stream Query saved successfully!");
        else
            return ResponseEntity.status(400).body("Failed to save the info");
    }

        @Autowired
        private DataSourceService dataSourceService;
        @PostMapping("/data-source")
        public ResponseEntity<?> createDataSource(@RequestBody DataSourceRequest request) {
            boolean created = dataSourceService.handleDataSource(request);

            if (created) {
                return ResponseEntity.ok(Map.of("message", "Data source saved and table created."));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create table.");
            }
        }
    }
