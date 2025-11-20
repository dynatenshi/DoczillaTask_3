package weather.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import weather.model.WeatherData;
import weather.service.WeatherService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class WeatherHandler {
    private final WeatherService weatherService;
    private final Gson gson = new Gson();

    public WeatherHandler(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }

        String city = extractCity(exchange.getRequestURI().getQuery());
        if (city == null || city.trim().isEmpty()) {
            sendError(exchange, 400, "City parameter is required");
            return;
        }

        try {
            WeatherData weatherData = weatherService.getWeatherData(city);
            sendSuccessResponse(exchange, weatherData);
        } catch (Exception e) {
            sendError(exchange, 500, "Error processing request: " + e.getMessage());
        }
    }

    private String extractCity(String query) {
        if (query == null) return null;

        Map<String, String> params = parseQuery(query);
        return params.get("city");
    }

    private Map<String, String> parseQuery(String query) {
        return java.util.Arrays.stream(query.split("&"))
                .map(param -> param.split("="))
                .filter(pair -> pair.length == 2)
                .collect(java.util.stream.Collectors.toMap(
                        pair -> pair[0],
                        pair -> URLDecoder.decode(pair[1], StandardCharsets.UTF_8)
                ));
    }

    private void sendSuccessResponse(HttpExchange exchange, WeatherData data) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("city", data.city());
        response.addProperty("chartUrl", data.chartUrl());
        response.add("temperatures", gson.toJsonTree(data.temperatures()));

        sendResponse(exchange, 200, response.toString());
    }

    private void sendResponse(HttpExchange exchange, int code, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders()
                .set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(code, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int code, String message) throws IOException {
        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        sendResponse(exchange, code, error.toString());
    }
}