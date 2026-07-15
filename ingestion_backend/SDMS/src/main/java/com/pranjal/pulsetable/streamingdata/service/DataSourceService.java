package com.pranjal.pulsetable.streamingdata.service;

import com.pranjal.pulsetable.streamingdata.entity.DataSourceRequest;
import org.springframework.stereotype.Service;

@Service
public class DataSourceService {
    public boolean handleDataSource(DataSourceRequest request) {
        // Save request to DB
        // Create table logic here
        return true;
    }
}
