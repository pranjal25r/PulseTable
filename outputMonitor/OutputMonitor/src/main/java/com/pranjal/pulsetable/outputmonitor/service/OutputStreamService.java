package com.pranjal.pulsetable.outputmonitor.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OutputStreamService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // to retrieve stream name using stream id
    public String getStreamNameById(Long streamId) {
        String sql = "SELECT stream_name FROM stream_master WHERE stream_id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, streamId);
    }

    // to retrieve table data using stream id
    public List<Map<String, Object>> getTableDataByStreamId(Long streamId) {
        String streamName = getStreamNameById(streamId);

        String tableName = "sdb_" + streamName;

        try {
            String sql = "SELECT * FROM " + tableName;
            return jdbcTemplate.queryForList(sql);
        } catch (BadSqlGrammarException e) {
            throw new RuntimeException("Table does not exist: " + tableName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data from table: " + tableName, e);
        }
    }

    public List<Map<String, Object>> getSummaryDataByStreamId(Long streamId) {
        String streamName = getStreamNameById(streamId);
        String tableName = "sdb_" + streamName + "_summary";
        try {
            String sql = "SELECT * FROM " + tableName  +" order by time_stamp DESC LIMIT 20";
            return jdbcTemplate.queryForList(sql);
        } catch (BadSqlGrammarException e) {
            throw new RuntimeException("Table does not exist: " + tableName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data from table: " + tableName, e);
        }
    }
    public List<Map<String, Object>> getColumnSpecificSummaryData(Long streamId, Long colId) {
        String streamName = getStreamNameById(streamId);
        String tableName = "sdb_" + streamName + "_summary";
        try {
            String sql = "SELECT * FROM " + tableName  +" WHERE stream_col_id=? order by id ASC";
            return jdbcTemplate.queryForList(sql, colId);
        } catch (BadSqlGrammarException e) {
            throw new RuntimeException("Table does not exist: " + tableName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data from table: " + tableName, e);
        }
    }
}
