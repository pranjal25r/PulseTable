package com.pranjal.pulsetable.streamingdata.entity;

public class DynamicTableMetadata {
    private Long streamColId;
    private String streamColDataType;
    private String streamColName;
    private Long streamId;

    public DynamicTableMetadata() {
    }

    public DynamicTableMetadata(Long streamColId, String streamColDataType, String streamColName, Long streamId) {
        this.streamColId = streamColId;
        this.streamColDataType = streamColDataType;
        this.streamColName = streamColName;
        this.streamId = streamId;
    }

    public Long getStreamColId() {
        return streamColId;
    }

    public void setStreamColId(Long streamColId) {
        this.streamColId = streamColId;
    }

    public String getStreamColDataType() {
        return streamColDataType;
    }

    public void setStreamColDataType(String streamColDataType) {
        this.streamColDataType = streamColDataType;
    }

    public String getStreamColName() {
        return streamColName;
    }

    public void setStreamColName(String streamColName) {
        this.streamColName = streamColName;
    }

    public Long getStreamId() {
        return streamId;
    }

    public void setStreamId(Long streamId) {
        this.streamId = streamId;
    }
}