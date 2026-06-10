package seasonal.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class DotEnvReader {
    private static final Path DOT_ENV = Path.of(".env");

    private DotEnvReader() {
    }

    /**
     * .env 파일에서 지정한 키의 값을 읽습니다.
     * 키가 없거나 파일이 없으면 null을 반환합니다.
     */
    static String read(String key) {
        if (!Files.exists(DOT_ENV)) {
            return null;
        }
        try {
            for (String line : Files.readAllLines(DOT_ENV)) {
                line = line.strip();
                if (line.startsWith("#") || !line.contains("=")) {
                    continue;
                }
                int idx = line.indexOf('=');
                String k = line.substring(0, idx).strip();
                String v = line.substring(idx + 1).strip();
                if (key.equals(k) && !v.isBlank()) {
                    return v;
                }
            }
        } catch (IOException exception) {
            throw new ApiRequestException(".env 파일을 읽지 못했습니다.", exception);
        }
        return null;
    }
}
