package com.pranjal.pulsetable.inputmonitor.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InsertionHelperService {

    private final JdbcTemplate jdbcTemplate;
    public String getDataSourcePathById(int streamId) {
        try {
            String sql = "SELECT data_source_path FROM stream_master WHERE stream_id = ?";
            return jdbcTemplate.queryForObject(sql, String.class, streamId);
        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve data source path for ID " + streamId + ": " + e.getMessage(), e);
        }
    }

    public InsertionHelperService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> insertRowFromCsv(String tableName, int streamId, List<Map<String, Object>> columns, Map<String, String> csvRow) {
        Map<String, Object> insertedValues = new HashMap<>();
        List<String> columnNames = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (Map<String, Object> column : columns) {
            String columnName = (String) column.get("COLUMN_NAME");
            String dataType = (String) column.get("TYPE_NAME");
            String columnDef = column.get("COLUMN_DEF") != null ? column.get("COLUMN_DEF").toString() : null;
            boolean isAutoIncrement = "YES".equals(column.get("IS_AUTOINCREMENT"));

            if (isAutoIncrement || (columnDef != null && columnDef.toUpperCase().contains("CURRENT_TIMESTAMP"))) {
                continue;
            }

            String csvValue = csvRow.get(columnName);
            Object value;

            if (csvValue != null && !csvValue.isEmpty()) {
                value = convertToAppropriateType(csvValue, dataType);
            } else {
                System.out.println("generating default value");
                value = generateDefaultValue(dataType, columnName);
            }

            columnNames.add(columnName);
            values.add(value);
            insertedValues.put(columnName, value);
        }

        if (columnNames.isEmpty()) {
            jdbcTemplate.update("INSERT INTO " + tableName + " DEFAULT VALUES");
        } else {
            String sql = "INSERT INTO " + tableName + " (" +
                    String.join(", ", columnNames) + ") VALUES (" +
                    columnNames.stream().map(c -> "?").collect(Collectors.joining(", ")) + ")";

            jdbcTemplate.update(sql, values.toArray());
        }
        return insertedValues;
    }

    public List<Map<String, String>> readCsvData(int streamId) throws IOException {
        List<Map<String, String>> result = new ArrayList<>();
        String csvFilePath = getDataSourcePathById(streamId);

        if (csvFilePath == null || csvFilePath.isEmpty()) {
            throw new IOException("No data source path found for stream ID: " + streamId);
        }

        try (Reader reader = new FileReader(csvFilePath)) {
            CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);

            for (CSVRecord record : csvParser) {
                Map<String, String> row = new HashMap<>();
                csvParser.getHeaderNames().forEach(header ->
                        row.put(header, record.get(header))
                );
                result.add(row);
            }
        }

        return result;
    }

    //here all one batch will be pre-processed and will be sent to summary table
    public void updateBatchSummaryStatistics(String tableName, int streamId, List<Map<String, Object>> batchRows) {
        if (batchRows.isEmpty()) {
            return;
        }

        String summaryTableName = tableName + "_summary";

        try {
            String columnQuery = "SELECT DISTINCT column_name, stream_col_id FROM " + summaryTableName +
                    " WHERE stream_id = ?";
            List<Map<String, Object>> columnInfo = jdbcTemplate.queryForList(columnQuery, streamId);

            // For each column, calculate batch statistics
            for (Map<String, Object> columnData : columnInfo) {
                String columnName = (String) columnData.get("column_name");
                Long streamColId = (Long) columnData.get("stream_col_id");

                // Get the most recent statistics
                String lastStatsSql = "SELECT sum, avg, max, min, row_count FROM " + summaryTableName +
                        " WHERE stream_id = ? AND column_name = ? ORDER BY id DESC LIMIT 1";

                Map<String, Object> lastStats;
                lastStats = jdbcTemplate.queryForMap(lastStatsSql, streamId, columnName);


                // Accumulate values for this batch
                double batchSum = 0.0;
                Double batchMax = null;
                Double batchMin = null;
                int batchCount = 0;

                // Process each row in the batch
                for (Map<String, Object> row : batchRows) {
                    Object value = row.get(columnName);
                    if (value instanceof Number) {
                        double numericValue = ((Number) value).doubleValue();
                        batchSum += numericValue;
                        batchCount++;

                        // Update batch max/min
                        batchMax = batchMax != null ? Math.max(batchMax, numericValue) : numericValue;
                        batchMin = batchMin != null ? Math.min(batchMin, numericValue) : numericValue;
                    }
                }

                // update takes place here
                if (batchCount > 0) {
                    // Get current values
                    Double currentSum = (Double) lastStats.get("sum");
                    Integer rowCount = (Integer) lastStats.get("row_count");
                    Double currentMax = (Double) lastStats.get("max");
                    Double currentMin = (Double) lastStats.get("min");

                    // new values are calculated here
                    double newSum = (currentSum != null ? currentSum : 0) + batchSum;
                    int newRowCount = (rowCount != null ? rowCount : 0) + batchCount;
                    double newAvg = newSum / newRowCount;

                    double newMax = currentMax != null ?
                            (batchMax != null ? Math.max(currentMax, batchMax) : currentMax) :
                            (batchMax != null ? batchMax : 0);

                    double newMin = currentMin != null ?
                            (batchMin != null ? Math.min(currentMin, batchMin) : currentMin) :
                            (batchMin != null ? batchMin : 0);

                    String insertSql = "INSERT INTO " + summaryTableName +
                            " (stream_id, stream_col_id, column_name, sum, avg, max, min, row_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                    jdbcTemplate.update(insertSql,
                            streamId,
                            streamColId,
                            columnName,
                            newSum,
                            newAvg,
                            newMax,
                            newMin,
                            newRowCount
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating batch statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void createTickEntry(String tableName, int streamId, int windowStartId, int windowEndId) {
        //note that table name already starts with sdb
        String tickTableName = tableName + "_tick";

        try {
            String sql = "INSERT INTO " + tickTableName +
                    " (stream_id, window_start_id, window_end_id, status) VALUES (?, ?, ?, ?)";

            jdbcTemplate.update(sql, streamId, windowStartId, windowEndId, "Processed");
        } catch (Exception e) {
            System.err.println("Error creating tick entry: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public Object convertToAppropriateType(String value, String dataType) {
        dataType = dataType.toUpperCase();

        try {
            if (dataType.contains("VARCHAR") || dataType.contains("CHAR") || dataType.contains("TEXT")) {
                return value;
            } else if (dataType.contains("INT")) {
                return Integer.parseInt(value);
            } else if (dataType.contains("DECIMAL") || dataType.contains("NUMERIC") ||
                    dataType.contains("FLOAT") || dataType.contains("DOUBLE")) {
                return Double.parseDouble(value);
            } else if (dataType.contains("DATE")) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                return new java.sql.Date(format.parse(value).getTime());
            } else if (dataType.contains("TIME")) {
                SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                return new java.sql.Time(format.parse(value).getTime());
            } else if (dataType.contains("TIMESTAMP")) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return new java.sql.Timestamp(format.parse(value).getTime());
            } else if (dataType.contains("BOOLEAN") || dataType.contains("BIT")) {
                return Boolean.parseBoolean(value);
            } else {
                return value;
            }
        } catch (NumberFormatException | ParseException e) {
            return value;
        }
    }

    public List<Map<String, Object>> getTableColumns(String tableName) {
        return jdbcTemplate.execute((Connection conn) -> {
            List<Map<String, Object>> columns = new ArrayList<>();
            DatabaseMetaData metaData = conn.getMetaData();

            try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
                while (rs.next()) {
                    Map<String, Object> column = new HashMap<>();
                    column.put("COLUMN_NAME", rs.getString("COLUMN_NAME"));
                    column.put("TYPE_NAME", rs.getString("TYPE_NAME"));
                    column.put("IS_NULLABLE", rs.getString("IS_NULLABLE"));
                    column.put("IS_AUTOINCREMENT", rs.getString("IS_AUTOINCREMENT"));
                    column.put("COLUMN_DEF", rs.getString("COLUMN_DEF"));
                    columns.add(column);
                }
            }
            return columns;
        });
    }
    public Object generateDefaultValue(String dataType, String columnName) {
        dataType = dataType.toUpperCase();

        if (dataType.contains("VARCHAR") || dataType.contains("CHAR") || dataType.contains("TEXT")) {
            return "Sample_" + columnName;
        } else if (dataType.contains("INT")) {
            return 1;
        } else if (dataType.contains("DECIMAL") || dataType.contains("NUMERIC") ||
                dataType.contains("FLOAT") || dataType.contains("DOUBLE")) {
            return 1.0;
        } else if (dataType.contains("DATE")) {
            return new java.sql.Date(System.currentTimeMillis());
        } else if (dataType.contains("TIME")) {
            return new java.sql.Time(System.currentTimeMillis());
        } else if (dataType.contains("TIMESTAMP")) {
            return new java.sql.Timestamp(System.currentTimeMillis());
        } else if (dataType.contains("BOOLEAN") || dataType.contains("BIT")) {
            return true;
        } else {
            return "Default_" + columnName;
        }
    }

    public Map<String, Object> getStreamParametersById(Integer streamId) {
        Map<String, Object> params = new HashMap<>();

        try {
            String query = "SELECT window_size, window_velocity, window_type FROM stream_master WHERE stream_id = ?";
            Map<String, Object> result = jdbcTemplate.queryForMap(query, streamId);

            params.put("window_size", result.get("window_size") != null ? result.get("window_size") : 2);
            params.put("window_velocity", result.get("window_velocity") != null ? result.get("window_velocity") : 5);
            params.put("window_type", result.get("window_type") != null ? result.get("window_type") : "sizetype");
        } catch (Exception e) {
            params.put("window_size", 2);
            params.put("window_velocity", 5);
            params.put("window_type", "sizetype");
            System.out.println("Could not retrieve stream parameters: " + e.getMessage());
        }

        return params;
    }

    public String getStreamNameById(int streamId) {
        try {
            String sql = "SELECT stream_name FROM stream_master WHERE stream_id = ?";
            return jdbcTemplate.queryForObject(sql, String.class, streamId);
        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve stream name for ID " + streamId + ": " + e.getMessage(), e);
        }
    }

    public int getCurrentRowCount(String tableName) {
        String countSql = "SELECT COUNT(*) FROM " + tableName;
        return jdbcTemplate.queryForObject(countSql, Integer.class);
    }

    public void deleteOldestRows(String tableName, int rowCount) {
        if (rowCount <= 0) return;

        String deleteSQL = "DELETE FROM " + tableName +
                " WHERE id IN (SELECT id FROM (SELECT id FROM " + tableName +
                " ORDER BY id ASC LIMIT ?) as temp_table)";

        jdbcTemplate.update(deleteSQL, rowCount);
        System.out.println("window is truncated (row based truncation)");

    }
    public void deleteRowsOlderThanTimeWindow(String tableName, int windowSizeMinutes) {

        String deleteSQL = "DELETE FROM " + tableName +
                " WHERE created_at < DATE_SUB(NOW(), INTERVAL ? SECOND)";

        jdbcTemplate.update(deleteSQL, windowSizeMinutes);
        System.out.println("window is truncated (row based truncation)");

    }
    public Integer getLastProcessedRowId(String tableName, int streamId) {
        String tickTableName = tableName + "_tick";

        try {
            String sql = "SELECT window_end_id FROM " + tickTableName +
                    " WHERE stream_id = ? ORDER BY window_id DESC LIMIT 1";

            return jdbcTemplate.queryForObject(sql, Integer.class, streamId);
        }
        //if no entries found
        catch (Exception e) {
            System.out.println("No previous tick entries found: " + e.getMessage());
            return -1;
        }
    }
}