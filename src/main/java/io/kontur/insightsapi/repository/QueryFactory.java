package io.kontur.insightsapi.repository;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Service
public class QueryFactory {

    private final Logger logger = LoggerFactory.getLogger(QueryFactory.class);

    public String getSql(Resource argResource) {
        String sql = null;
        InputStream inputStream = null;
        try {
            inputStream = argResource.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            sql = reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            String error = String.format("Can't read file %s - %s", argResource.getFilename(), e.getMessage());
            logger.error(error);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return sql;
    }
}
