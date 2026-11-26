package com.accenture.dhruv.healthdekho.seed;

import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SeedCsv {

    private static final Pattern FIELD = Pattern.compile("(?:\"((?:[^\"]|\"\")*)\"|([^,\"]*))(?:,|$)");

    private SeedCsv() {
    }

    public static List<Map<String, String>> read(String table) {
        ClassPathResource resource = new ClassPathResource("seed/" + table + ".csv");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            List<String> header = parseLine(reader.readLine());
            List<Map<String, String>> rows = new ArrayList<>();
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                List<String> fields = parseLine(line);
                if (fields.size() != header.size()) {
                    throw new IllegalArgumentException("Wrong number of fields in " + table + ".csv at line " + lineNumber);
                }
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < header.size(); i++) {
                    row.put(header.get(i), fields.get(i));
                }
                rows.add(row);
            }
            return rows;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + resource.getPath(), e);
        }
    }

    private static List<String> parseLine(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Expected a CSV line");
        }
        List<String> fields = new ArrayList<>();
        Matcher matcher = FIELD.matcher(line);
        int position = 0;
        do {
            matcher.region(position, line.length());
            if (!matcher.lookingAt()) {
                throw new IllegalArgumentException("Invalid seed CSV field at character " + position);
            }
            String quoted = matcher.group(1);
            fields.add(quoted == null ? matcher.group(2) : quoted.replace("\"\"", "\""));
            position = matcher.end();
        } while (position < line.length());
        if (line.endsWith(",")) {
            fields.add("");
        }
        return fields;
    }
}
