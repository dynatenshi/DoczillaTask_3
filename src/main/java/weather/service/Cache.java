package weather.service;

import weather.model.CacheEntry;
import weather.model.WeatherData;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Cache {
    private static final long CACHE_TTL = TimeUnit.MINUTES.toMillis(15);
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public WeatherData get(String city) {
        String cityKey = city.toLowerCase();
        CacheEntry entry = cache.get(cityKey);

        if (entry != null && System.currentTimeMillis() - entry.timestamp() < CACHE_TTL) {
            return entry.data();
        }
        return null;
    }

    public void put(String city, WeatherData data) {
        String cityKey = city.toLowerCase();
        cache.put(cityKey, new CacheEntry(System.currentTimeMillis(), data));
    }

    public void cleanup() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(
                entry -> now - entry.getValue().timestamp() > CACHE_TTL
        );
    }
}