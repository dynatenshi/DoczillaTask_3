package weather.model;

public record CacheEntry(long timestamp, WeatherData data) { }