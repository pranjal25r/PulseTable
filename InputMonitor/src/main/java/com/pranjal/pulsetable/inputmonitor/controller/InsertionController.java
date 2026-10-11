package com.pranjal.pulsetable.inputmonitor.controller;

import com.pranjal.pulsetable.inputmonitor.service.InsertionHelperService;
import com.pranjal.pulsetable.inputmonitor.service.InsertionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api")
public class InsertionController {

    private final InsertionService insertionService;

    public InsertionController(InsertionService insertionService) {
        this.insertionService = insertionService;
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("/insert-batched")
    public ResponseEntity<Map<String, Object>> insertInBatches(@RequestBody Map<String, Object> request) {
        Integer streamId = (Integer) request.get("stream_id");

        if (streamId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Stream ID is required"));
        }

        try {
            // Fetch table name from database using stream_id
            String tableName = insertionService.getStreamNameById(streamId);

            if (tableName == null || tableName.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No table found for stream ID: " + streamId));
            }

            tableName = "sdb_" + InsertionHelperService.toSafeName(tableName);
            Map<String, Object> streamParams = insertionService.getStreamParametersById(streamId);
            int windowSize = (Integer) streamParams.get("window_size");
            int windowVelocity = (Integer) streamParams.get("window_velocity");
            String windowType = (String) streamParams.get("window_type");

            Map<String, Object> result;
            if ("sizetype".equals(windowType)) {
                result = insertionService.insertRowsInBatches(streamId, tableName, windowSize, windowVelocity);
            } else {
                result = insertionService.insertTimeWiseRowsInBatches(streamId, tableName, windowSize, windowVelocity);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("/stop-insertion")
    public ResponseEntity<String> stopInsertion() {
        insertionService.stopInsertion();
        return ResponseEntity.ok("stopped");
    }
}