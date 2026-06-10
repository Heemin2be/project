package seasonal.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

final class CsvReader {
    private CsvReader() {
    }

    static List<String[]> readRows(String resourcePath) {
        InputStream inputStream = CsvReader.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new DataLoadException("리소스를 찾을 수 없습니다: " + resourcePath, null);
        }

        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                if (!line.trim().isEmpty()) {
                    rows.add(line.split(",", -1));
                }
            }
            return rows;
        } catch (IOException exception) {
            throw new DataLoadException("CSV 파일을 읽는 중 오류가 발생했습니다: " + resourcePath, exception);
        }
    }
}
