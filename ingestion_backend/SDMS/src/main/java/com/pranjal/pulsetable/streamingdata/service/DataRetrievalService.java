package com.pranjal.pulsetable.streamingdata.service;

import com.pranjal.pulsetable.streamingdata.entity.DynamicTableMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DataRetrievalService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> getStreamColsTableData(Long streamId) {
        String sql = "SELECT stream_col_id as streamColId,column_name as streamColName, column_data_type as streamColDataType, stream_id as streamId FROM stream_cols where stream_id=?";
        return jdbcTemplate.queryForList(sql, streamId);
    }
    public List<Map<String, Object>> getStreamQueriesTableData(Long streamId) {
        String sql = "SELECT q.stream_col_id as columnId, q.agg_function as aggregationType, c.column_name as columnName FROM stream_queries q inner join stream_cols c on q.stream_col_id=c.stream_col_id WHERE q.stream_id=?";
        return jdbcTemplate.queryForList(sql, streamId);
    }
}