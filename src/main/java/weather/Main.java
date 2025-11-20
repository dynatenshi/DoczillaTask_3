package weather;

import weather.handler.StaticFileHandler;
import weather.handler.WeatherHandler;
import weather.service.Cache;
import com.sun.net.httpserver.HttpServer;
import weather.service.ChartService;
import weather.service.WeatherService;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        Cache cache = new Cache();
        ChartService chartService = new ChartService();
        WeatherService weatherService = new WeatherService(cache, chartService);
        WeatherHandler handler = new WeatherHandler(weatherService);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/weather", handler::handle);
        server.createContext("/charts", new StaticFileHandler());
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("Started on port " + PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(cache::cleanup));
    }
}