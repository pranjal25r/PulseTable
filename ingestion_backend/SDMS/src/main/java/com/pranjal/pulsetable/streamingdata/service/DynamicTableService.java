package com.pranjal.pulsetable.streamingdata.service;

import com.pranjal.pulsetable.streamingdata.entity.StreamColEntity;
import com.pranjal.pulsetable.streamingdata.entity.StreamMasterEntity;
import com.pranjal.pulsetable.streamingdata.repository.StreamColRepository;
import com.pranjal.pulsetable.streamingdata.repository.StreamMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DynamicTableService {

    @Autowired
    private StreamColRepository streamColRepository;

    @Autowired
    private StreamMasterRepository streamMasterRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // the one rule for turning a stream or column name into a table or column name
    public static String toSafeName(String name) {
        String safe = name == null ? "" : name.trim().toLowerCase().replace(' ', '_');
        if (!safe.matches("^[a-z][a-z0-9_]{0,40}$")) {
            throw new IllegalArgumentException("Names may only contain letters, digits, spaces and underscores");
        }
        return safe;
    }

    public void createDynamicTables() {
        // Get all stream masters
        List<StreamMasterEntity> streamMasters = streamMasterRepository.findAll();

        for (StreamMasterEntity streamMaster : streamMasters) {
            // Use streamId instead of the entity object
            Long streamId = streamMaster.getStreamId();
            List<StreamColEntity> columns = streamColRepository.findByStream_StreamId(streamId);

            if (!columns.isEmpty()) {
                createTableForStream(streamMaster, columns);
            }
        }
    }

    private void createTableForStream(StreamMasterEntity streamMaster, List<StreamColEntity> columns) {
        String tableName = "sdb_" + toSafeName(streamMaster.getStreamName());

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (\n");
        sql.append("  id BIGINT AUTO_INCREMENT PRIMARY KEY,\n");

        for (int i = 0; i < columns.size(); i++) {
            StreamColEntity col = columns.get(i);
            String columnName = toSafeName(col.getStreamColName());
            String dataType = mapToSqlDataType(col.getStreamColDataType());

            sql.append("  `").append(columnName).append("` ").append(dataType);
            if (i < columns.size() - 1) {
                sql.append(",");
            }
            sql.append("\n");
        }
        sql.append(",  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n");
        sql.append(")");


        jdbcTemplate.execute(sql.toString());

        createSummaryTableForStream(streamMaster, columns);
        createTickTableForStream(streamMaster, columns);
    }

    private void createSummaryTableForStream(StreamMasterEntity streamMaster, List<StreamColEntity> columns) {
        String baseTableName = "sdb_" + toSafeName(streamMaster.getStreamName());
        String summaryTableName = baseTableName + "_summary";


        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ").append(summaryTableName).append(" (\n");
        sql.append("  id BIGINT AUTO_INCREMENT PRIMARY KEY,\n");
        sql.append("  stream_id BIGINT NOT NULL,\n");
        sql.append("  stream_col_id BIGINT NOT NULL,\n");
        sql.append("  column_name VARCHAR(255) NOT NULL,\n");
        sql.append("  sum DOUBLE DEFAULT 0,\n");
        sql.append("  avg DOUBLE DEFAULT 0,\n");
        sql.append("  max DOUBLE DEFAULT NULL,\n");
        sql.append("  min DOUBLE DEFAULT NULL,\n");
        sql.append("  row_count INT DEFAULT 0,\n");
        sql.append("  time_stamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n");
        sql.append("  FOREIGN KEY (stream_id) REFERENCES stream_master(stream_id),\n");
        sql.append("  FOREIGN KEY (stream_col_id) REFERENCES stream_cols(stream_col_id)\n");
        sql.append(")");

        jdbcTemplate.execute(sql.toString());

        List<String> queryColumns = getQueryColumnsForStream(streamMaster.getStreamId());

        for (StreamColEntity column : columns) {
            if (queryColumns.contains(toSafeName(column.getStreamColName()))) {
                String dataType = column.getStreamColDataType().toLowerCase();
                if (dataType.equals("integer") || dataType.equals("long") ||
                        dataType.equals("double") || dataType.equals("float")) {

                    String insertSql = "INSERT INTO " + summaryTableName +
                            " (stream_id, stream_col_id, column_name, sum, avg, max, min, row_count) VALUES (?, ?, ?, 0, 0, NULL, NULL, 0)";

                    jdbcTemplate.update(insertSql,
                            streamMaster.getStreamId(),
                            column.getStreamColId(),
                            toSafeName(column.getStreamColName())
                    );
                }
            }
        }
    }
    private void createTickTableForStream(StreamMasterEntity streamMaster, List<StreamColEntity> columns) {
        String baseTableName = "sdb_" + toSafeName(streamMaster.getStreamName());
        String tickTableName = baseTableName + "_tick";

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ").append(tickTableName).append(" (\n");
        sql.append("  window_id BIGINT AUTO_INCREMENT PRIMARY KEY,\n");
        sql.append("  stream_id BIGINT NOT NULL,\n");
        sql.append("  window_start_id INTEGER DEFAULT 0,\n");
        sql.append("  window_end_id INTEGER DEFAULT 0,\n");
        sql.append("  status VARCHAR(255) DEFAULT 'UNPROCESSED',\n");
        sql.append("  time_stamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n");
        sql.append("  FOREIGN KEY (stream_id) REFERENCES stream_master(stream_id)\n");
        sql.append(")");

        jdbcTemplate.execute(sql.toString());
    }

    private List<String> getQueryColumnsForStream(Long streamId) {
        // Join stream_queries with stream_cols to get column names
        String sql = "SELECT sc.column_name FROM stream_cols sc " +
                "JOIN stream_queries sq ON sc.stream_id = sq.stream_id AND sc.stream_col_id = sq.stream_col_id " +
                "WHERE sq.stream_id = ?";
        return jdbcTemplate.queryForList(sql, String.class, streamId).stream()
                .map(DynamicTableService::toSafeName).toList();
    }

    private String mapToSqlDataType(String dataType) {
        if (dataType == null) return "VARCHAR(255)";

        return switch (dataType.toLowerCase()) {
            case "string" -> "VARCHAR(255)";
            case "integer" -> "INT";
            case "long" -> "BIGINT";
            case "double" -> "DOUBLE";
            case "float" -> "FLOAT";
            case "boolean" -> "BOOLEAN";
            case "date" -> "DATE";
            case "timestamp" -> "TIMESTAMP";
            default -> "VARCHAR(255)";
        };
    }


    public void createDynamicTableForStream(Long streamId) {
        StreamMasterEntity streamMaster = streamMasterRepository.findById(streamId)
                .orElseThrow(() -> new RuntimeException("Stream not found"));

        List<StreamColEntity> columns = streamColRepository.findByStream_StreamId(streamId);

        if (!columns.isEmpty()) {
            createTableForStream(streamMaster, columns);
        }
    }

}