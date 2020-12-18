package io.kontur.insightsapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.insightsapi.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class StatisticService {

    private final ObjectMapper objectMapper;

    @Value("${statistics.path}")
    private String filePath;

    public StatisticService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Statistic createObjFromJson() throws IOException {
        return objectMapper.readValue(new File(filePath), Statistic.class);
    }
}
