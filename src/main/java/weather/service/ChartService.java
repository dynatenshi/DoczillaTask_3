package weather.service;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class ChartService {
    private static final String CHARTS_DIR = "charts";
    private static final int CHART_WIDTH = 800;
    private static final int CHART_HEIGHT = 400;

    public ChartService() {
        new File(CHARTS_DIR).mkdirs();
    }

    public String generateChart(String city, double[] temperatures) throws IOException {
        XYSeries series = new XYSeries("Temperature (°C)");

        for (int hour = 0; hour < temperatures.length; hour++) {
            series.add(hour, temperatures[hour]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "24-Hour Temperature Forecast for " + city,
                "Time (Hours)",
                "Temperature (°C)",
                dataset
        );

        return saveChart(city, chart);
    }

    private String saveChart(String city, JFreeChart chart) throws IOException {
        String timestamp = DateTimeFormatter.ISO_INSTANT
                .format(Instant.now())
                .replace(":", "-");
        String filename = String.format("%s_%s.png", sanitizeFileName(city), timestamp);

        File chartFile = new File(CHARTS_DIR, filename);
        ChartUtils.saveChartAsPNG(chartFile, chart, CHART_WIDTH, CHART_HEIGHT);

        return "/charts/" + filename;
    }

    private String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
    }
}