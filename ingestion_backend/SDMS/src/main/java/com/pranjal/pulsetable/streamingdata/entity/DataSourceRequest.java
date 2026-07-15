package com.pranjal.pulsetable.streamingdata.entity;

//package com.yourpackage.dto;

public class DataSourceRequest {
    private Long streamId;
    private String dataSourceType;
    private String dataSourcePath;

    // Getters and setters
    public Long getStreamId() {
        return streamId;
    }

    public void setStreamId(Long streamId) {
        this.streamId = streamId;
    }

    public String getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    public String getDataSourcePath() {
        return dataSourcePath;
    }

    public void setDataSourcePath(String dataSourcePath) {
        this.dataSourcePath = dataSourcePath;
    }
}
