package alerter.Utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
public class ConfigUtils {
    private static File file;
    private static FileConfiguration config;

    public static void setUp(Plugin plugin) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        file = new File(plugin.getDataFolder(), "config.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().warning(e.getMessage());
                return;
            }
        }

        config = YamlConfiguration.loadConfiguration(file);

        load_defaults(config);
    }

    private static void load_defaults(FileConfiguration config) {
        config.addDefault("bot-token", "bot-token");
        config.addDefault("chat-id", "chat-id");
        config.addDefault("text", "Current TPS:");
        config.addDefault("tps-limit", 18);
        config.addDefault("stop", "Server was stopped");

        config.options().copyDefaults(true);
        save();
    }

    public static FileConfiguration get() {
        return config;
    }

    public static void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().warning(e.getMessage());
        }
    }

    public static void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }
}
