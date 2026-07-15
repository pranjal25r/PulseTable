package com.pranjal.pulsetable.streamingdata.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stream_queries")
public class StreamQueryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long queryId;

    @ManyToOne
    @JoinColumn(name = "stream_id", referencedColumnName = "stream_id", nullable = false)
    private StreamMasterEntity stream;

    @ManyToOne
    @JoinColumn(name = "stream_col_id", referencedColumnName = "stream_col_id", nullable = false)
    private StreamColEntity streamCol;

    private String aggFunction;


    public Long getStreamId(){
        return stream.getStreamId();
    }
    public Long getStreamColId(){
        return streamCol.getStreamColId();
    }

    // Getters and Setters

    public Long getQueryId() {
        return queryId;
    }

    public void setQueryId(Long queryId) {
        this.queryId = queryId;
    }

    public StreamMasterEntity getStream() {
        return stream;
    }

    public void setStream(StreamMasterEntity stream) {
        this.stream = stream;
    }

    public StreamColEntity getStreamCol() {
        return streamCol;
    }

    public void setStreamCol(StreamColEntity streamCol) {
        this.streamCol = streamCol;
    }

    public String getAggFunction() {
        return aggFunction;
    }

    public void setAggFunction(String aggFunction) {
        this.aggFunction = aggFunction;
    }
}
