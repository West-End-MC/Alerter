package alerter;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.Spark;
import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.api.statistic.types.DoubleStatistic;
import alerter.Utils.ConfigUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public final class Alerter extends JavaPlugin {

    @Override
    public void onEnable() {
        ConfigUtils.setUp(this);
        Bukkit.getScheduler().runTaskTimer(this, this::getAndReportTPS, 0, 300 * 20);
    }
    public void onDisable() {
        ConfigUtils.reload();
        FileConfiguration config = ConfigUtils.get();
        String stop = config.getString("stop");
        sendToTelegram(stop);
    }

    public void publish(LogRecord record) {
        if (record.getLevel().equals(Level.SEVERE)) { // Проверка на уровень ERROR
            sendToTelegram("ERROR in logs: " + record.getMessage());
        }
    }
    private void getAndReportTPS() {
        ConfigUtils.reload();
        FileConfiguration config = ConfigUtils.get();
        Spark spark = SparkProvider.get();
        DoubleStatistic<StatisticWindow.TicksPerSecond> tps = spark.tps();
        double tpsLast5Mins = tps.poll(StatisticWindow.TicksPerSecond.MINUTES_5);
        String tpsMessage = config.getString("text") + tpsLast5Mins;

        // Код для отправки значения TPS куда-либо, например, в Telegram
        if (tpsLast5Mins < config.getDouble("tps-limit")) {
            sendToTelegram(tpsMessage);
        }
    }

    private void sendToTelegram(String message) {
        ConfigUtils.reload();
        FileConfiguration config = ConfigUtils.get();

        String botToken = config.getString("bot-token");
        String chatId = config.getString("chat-id");

        try {
            String apiURL = "https://api.telegram.org/bot" + botToken + "/sendMessage?chat_id=" + chatId + "&text=" + URLEncoder.encode(message, StandardCharsets.UTF_8);

            // Создаем URL и открываем соединение
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            // Отправляем запрос и получаем ответ
            int status = con.getResponseCode();

            con.disconnect();
            // Проверяем, успешно ли был отправлен запрос
            if (status == HttpURLConnection.HTTP_OK) {
                // Запрос успешно выполнен
                Bukkit.getLogger().info("TPS successfully reported to Telegram.");
            } else {
                // Обрабатываем ошибку
                Bukkit.getLogger().warning("Failed to report TPS to Telegram. Response code: " + status);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Bukkit.getLogger().warning("Error sending TPS to Telegram: " + e.getMessage());
        }
    }
}