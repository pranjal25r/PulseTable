package com.pranjal.pulsetable.outputmonitor.dto;

public class QueryRequest {
    private String raw_query;
    private String timestamp;

    public String getRaw_query() {
        return raw_query;
    }

    public void setRaw_query(String raw_query) {
        this.raw_query = raw_query;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
