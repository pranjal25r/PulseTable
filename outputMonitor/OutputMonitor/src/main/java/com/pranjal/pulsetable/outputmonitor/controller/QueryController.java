package com.pranjal.pulsetable.outputmonitor.controller;

import com.pranjal.pulsetable.outputmonitor.dto.QueryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class QueryController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("/execute-query")
    public ResponseEntity<?> executeQuery(@RequestBody QueryRequest request) {
        String rawQuery = request.getRaw_query();

        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Query is required"));
        }

        rawQuery = rawQuery.trim();
        String lowerQuery = rawQuery.toLowerCase();

        // only select statement will pass
        if (!lowerQuery.startsWith("select ")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only SELECT queries are allowed"));
        }

        // blocking the keywords
        String[] blockedKeywords = {
                "insert", "update", "delete", "drop", "alter", "create", "truncate"
        };

        for (String keyword : blockedKeywords) {
            if (lowerQuery.contains(keyword)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", keyword + " is not allowed"));
            }
        }

        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(rawQuery);
            return ResponseEntity.ok(Map.of("data", result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Failed to execute query",
                    "details", e.getMessage()
            ));
        }
    }
}