package com.pranjal.pulsetable.outputmonitor.controller;

import com.pranjal.pulsetable.outputmonitor.repository.StreamInfoRepository;
import com.pranjal.pulsetable.outputmonitor.dto.StreamInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/ingestion")
public class StreamInfoController {
    @Autowired
    private StreamInfoRepository streamInfoRepository;

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/stream-master")
    public List<StreamInfo> getAllStreams() {
        return streamInfoRepository.findAll();
    }

}
