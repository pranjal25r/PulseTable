package com.pranjal.pulsetable.outputmonitor.dto;

public class StreamColumnRequest {
    private Long streamId;
    private Long colId;

    public Long getStreamId() {
        return streamId;
    }

    public void setStreamId(Long streamId) {
        this.streamId = streamId;
    }

    public Long getColId() {
        return colId;
    }

    public void setColId(Long colId) {
        this.colId = colId;
    }
}