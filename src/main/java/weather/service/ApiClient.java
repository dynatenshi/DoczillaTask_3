package weather.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class ApiClient {
    private static final String GEOCODING_URL =
            "https://geocoding-api.open-meteo.com/v1/search?name=%s&count=1";
    private static final String WEATHER_URL =
            "https://api.open-meteo.com/v1/forecast?latitude=%.6f&longitude=%.6f&hourly=temperature_2m&timezone=auto";

    private final HttpClient httpClient;

    public ApiClient() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public JsonObject fetchCoordinates(String city) throws Exception {
        String url = String.format(GEOCODING_URL, URLEncoder.encode(city, StandardCharsets.UTF_8));
        String json = sendHttpRequest(url);

        JsonObject data = JsonParser
                .parseString(json)
                .getAsJsonObject();
        JsonArray results = data
                .getAsJsonArray("results");

        if (results == null || results.isEmpty()) {
            throw new RuntimeException("City not found: " + city);
        }
        return results.get(0).getAsJsonObject();
    }

    public double[] fetchTemperatures(double lat, double lon) throws Exception {
        String url = String.format(Locale.US, WEATHER_URL, lat, lon);
        String json = sendHttpRequest(url);

        JsonObject response = JsonParser.parseString(json).getAsJsonObject();
        JsonArray temps = response
                .getAsJsonObject("hourly")
                .getAsJsonArray("temperature_2m");

        return extractTemperatures(temps);
    }

    private double[] extractTemperatures(JsonArray temps) {
        int hours = Math.min(24, temps.size());
        double[] temperatures = new double[hours];
        for (int i = 0; i < hours; i++) {
            temperatures[i] = temps.get(i).getAsDouble();
        }
        return temperatures;
    }

    private String sendHttpRequest(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Accept", "application/json")
                .header("User-Agent", "WeatherService/1.0")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP error " + response.statusCode() + " for URL: " + url);
        }

        return response.body();
    }
}