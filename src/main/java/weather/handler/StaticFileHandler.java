package weather.handler;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class StaticFileHandler implements HttpHandler {
    private static final String CHARTS_DIR = "charts";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String filename = extractFilename(exchange.getRequestURI().getPath());

        if (!isValidFilename(filename)) {
            sendError(exchange, 400, "Invalid filename");
            return;
        }

        File file = new File(CHARTS_DIR, filename);
        if (!file.exists()) {
            sendError(exchange, 404, "Chart not found");
            return;
        }

        serveFile(exchange, file);
    }

    private String extractFilename(String path) {
        return Paths.get(path).getFileName().toString();
    }

    private boolean isValidFilename(String filename) {
        return filename != null &&
                !filename.contains("..") &&
                !filename.contains("/") &&
                !filename.contains("\\");
    }

    private void serveFile(HttpExchange exchange, File file) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "image/png");
        exchange.getResponseHeaders().set("Cache-Control", "max-age=900");
        exchange.sendResponseHeaders(200, file.length());

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = exchange.getResponseBody()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
    }

    private void sendError(HttpExchange exchange, int code, String message) throws IOException {
        exchange.sendResponseHeaders(code, message.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(message.getBytes());
        }
    }
}