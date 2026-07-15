package com.pranjal.pulsetable.streamingdata.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stream_cols")
public class StreamColEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stream_col_id")
    private Long streamColId;

    @ManyToOne
    @JoinColumn(name = "stream_id", referencedColumnName = "stream_id", nullable = false)
    private StreamMasterEntity stream;

    @Column(name = "column_name", nullable = false)
    private String streamColName;

    @Column(name = "column_data_type", nullable = false)
    private String streamColDataType;


    // Getters and Setters

    public Long getStreamColId() {
        return streamColId;
    }


    public void setStreamColId(Long streamColId) {
        this.streamColId = streamColId;
    }

    public StreamMasterEntity getStream() {
        return stream;
    }

    public Long getStreamId() {
        return stream != null ? stream.getStreamId() : null;
    }

    public void setStream(StreamMasterEntity stream) {
        this.stream = stream;
    }

    public String getStreamColName() {
        return streamColName;
    }
    public void setStreamColName(String streamColName) {
        this.streamColName = streamColName;
    }

    public String getStreamColDataType() {
        return streamColDataType;
    }
    public void setStreamColDataType(String streamColDataType) {
        this.streamColDataType = streamColDataType;
    }
}
