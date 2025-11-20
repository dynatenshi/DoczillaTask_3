package weather.model;

public record WeatherData(String city, double[] temperatures, String chartUrl) { }