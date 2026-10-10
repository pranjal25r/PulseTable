package com.pranjal.pulsetable.outputmonitor.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            return keepChosenAggregates(streamId, jdbcTemplate.queryForList(sql), null);
        } catch (BadSqlGrammarException e) {
            throw new RuntimeException("Table does not exist: " + tableName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data from table: " + tableName, e);
        }
    }
    public List<Map<String, Object>> getColumnSpecificSummaryData(Long streamId, Long colId, String aggFunction) {
        String streamName = getStreamNameById(streamId);
        String tableName = "sdb_" + streamName + "_summary";
        try {
            String sql = "SELECT * FROM " + tableName  +" WHERE stream_col_id=? order by id ASC";
            return keepChosenAggregates(streamId, jdbcTemplate.queryForList(sql, colId), aggFunction);
        } catch (BadSqlGrammarException e) {
            throw new RuntimeException("Table does not exist: " + tableName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data from table: " + tableName, e);
        }
    }

    // keeps only the aggregates picked for each column in stream_queries.agg_function,
    // and only aggFunction when the caller asks for one of them
    private List<Map<String, Object>> keepChosenAggregates(Long streamId, List<Map<String, Object>> rows, String aggFunction) {
        Map<Long, Set<String>> chosenByColumn = new HashMap<>();
        String sql = "SELECT stream_col_id, agg_function FROM stream_queries WHERE stream_id = ?";
        for (Map<String, Object> query : jdbcTemplate.queryForList(sql, streamId)) {
            if (query.get("agg_function") != null) {
                chosenByColumn.computeIfAbsent(((Number) query.get("stream_col_id")).longValue(), k -> new HashSet<>())
                        .add(query.get("agg_function").toString().toLowerCase());
            }
        }

        for (Map<String, Object> row : rows) {
            Set<String> chosen = chosenByColumn.get(((Number) row.get("stream_col_id")).longValue());
            if (chosen == null) {
                continue;
            }
            if (aggFunction != null && chosen.contains(aggFunction.toLowerCase())) {
                chosen = Set.of(aggFunction.toLowerCase());
            }
            for (String aggregate : List.of("sum", "avg", "max", "min")) {
                if (!chosen.contains(aggregate)) {
                    row.remove(aggregate);
                }
            }
        }
        return rows;
    }
}
