package com.pranjal.pulsetable.outputmonitor.dto;

import jakarta.persistence.*;

@Entity
@Table(name = "stream_master")
public class StreamInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long streamId;

    private String streamName;
    private String windowType;
    private int windowSize;
    private int windowVelocity;
    private String dataSourceType;
    private String dataSourcePath;

    public Long getStreamId() {
        return streamId;
    }

    public void setStreamId(Long streamId) {
        this.streamId = streamId;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public String getWindowType() {
        return windowType;
    }

    public void setWindowType(String windowType) {
        this.windowType = windowType;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public int getWindowVelocity() {
        return windowVelocity;
    }

    public void setWindowVelocity(int windowVelocity) {
        this.windowVelocity = windowVelocity;
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