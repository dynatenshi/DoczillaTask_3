package weather.service;

import weather.model.Coordinates;
import weather.model.WeatherData;
import com.google.gson.JsonObject;

public class WeatherService {
    private final Cache cache;
    private final ApiClient apiClient;
    private final ChartService chartService;

    public WeatherService(Cache cache, ChartService chartService) {
        this.cache = cache;
        this.chartService = chartService;
        this.apiClient = new ApiClient();
    }

    public WeatherData getWeatherData(String city) throws Exception {
        WeatherData cached = cache.get(city);
        if (cached != null) {
            return cached;
        }

        WeatherData freshData = fetchFreshWeatherData(city);
        cache.put(city, freshData);
        return freshData;
    }

    private WeatherData fetchFreshWeatherData(String city) throws Exception {
        JsonObject coordsJson = apiClient.fetchCoordinates(city);
        Coordinates coords = new Coordinates(
                coordsJson.get("latitude").getAsDouble(),
                coordsJson.get("longitude").getAsDouble()
        );

        double[] temperatures = apiClient
                .fetchTemperatures(coords.latitude(), coords.longitude());

        String chartUrl = chartService.generateChart(city, temperatures);

        return new WeatherData(city, temperatures, chartUrl);
    }
}