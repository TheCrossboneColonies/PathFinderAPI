package com.tcc.pathfinderapi.configuration;

import com.tcc.pathfinderapi.PathFinderAPI;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Used to obtain values from a configuration file.
 * Also updates outdated configuration files with new settings (does not remove old settings).
 */
public class ConfigManager {

    private PathFinderAPI pathFinderAPI;
    private Map<String, YamlConfiguration> configs = new HashMap<>();

    private static ConfigManager configManager = null;
    public static ConfigManager getInstance () { return configManager; }

    /**
     * This value is updated whenever a change to any configuration file is made.
     */
    private final float CONFIG_VERSION = 1.0F;
    private final List<String> configFiles;

    public ConfigManager (PathFinderAPI pathFinderAPI) {

        this.pathFinderAPI = pathFinderAPI;

        this.configFiles = Arrays.asList(
                "config.yml",
                "lang.yml"
        );

        configManager = this;
    }

    public void loadConfigFiles () {

        if(!this.pathFinderAPI.getDataFolder().exists()) this.pathFinderAPI.getDataFolder().mkdirs();

        for (String filePath : configFiles) {

            File file = new File(this.pathFinderAPI.getDataFolder() + File.separator + filePath);

            if (!file.exists()) {

                try { file.createNewFile(); } 
                catch (IOException ioException) { ioException.printStackTrace(); }
            }

            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
            String versionString = yamlConfiguration.getString("version");
            float version = versionString == null ? -1F : Float.parseFloat(versionString);

            if (version != CONFIG_VERSION) { updateConfigFile(filePath); }
            configs.put(filePath, YamlConfiguration.loadConfiguration(file));
        }
    }

    public void updateConfigFile (String filePath) {

        File file = new File(this.pathFinderAPI.getDataFolder() + File.separator + filePath);
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);

        InputStream defaultStream = this.pathFinderAPI.getResource(filePath);
        if (defaultStream == null) return;

        yamlConfiguration.set("version", CONFIG_VERSION);
        yamlConfiguration.setComments("version", Arrays.asList("Do not change this value!"));

        YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
        
        for (String key : defaultConfig.getKeys(true)) {

            Object object = defaultConfig.get(key);

            if (!(object instanceof MemorySection)) {

                if (yamlConfiguration.get(key) == null) {

                    yamlConfiguration.set(key, object);
                    if (defaultConfig.getComments(key) != null) { yamlConfiguration.setComments(key, defaultConfig.getComments(key)); }
                }
            }
        }

        try { yamlConfiguration.save(file); } 
        catch (IOException ioException) { ioException.printStackTrace(); }
    }

    public boolean getBoolean (ConfigNode configNode) {

        YamlConfiguration yamlConfiguration = configs.get(configNode.getFilePath());
        return yamlConfiguration.getBoolean(configNode.getRoot());
    }

    public double getDouble (ConfigNode configNode) {

        YamlConfiguration yamlConfiguration = configs.get(configNode.getFilePath());
        return yamlConfiguration.getDouble(configNode.getRoot());
    }

    public int getInt (ConfigNode configNode) {

        YamlConfiguration yamlConfiguration = configs.get(configNode.getFilePath());
        return yamlConfiguration.getInt(configNode.getRoot());
    }

    public String getString (ConfigNode configNode) {

        YamlConfiguration yamlConfiguration = configs.get(configNode.getFilePath());
        return yamlConfiguration.getString(configNode.getRoot());
    }

    public List<String> getStringList (ConfigNode configNode) {

        YamlConfiguration yamlConfiguration = configs.get(configNode.getFilePath());
        return yamlConfiguration.getStringList(configNode.getRoot());
    }

    public List<Map<?,?>> getMapList (ConfigNode configNode) {

        YamlConfiguration yamlConfiguration = configs.get(configNode.getFilePath());
        return yamlConfiguration.getMapList(configNode.getRoot());
    }

    public Location getLocation (ConfigNode configNode) {

        YamlConfiguration yamlConfiguration = configs.get(configNode.getFilePath());
        return yamlConfiguration.getLocation(configNode.getRoot());
    }

    public Color getColor (ConfigNode configNode) {

        YamlConfiguration yamlConfiguration = configs.get(configNode.getFilePath());
        int red = yamlConfiguration.getInt(configNode.getRoot() + ".red");
        int green = yamlConfiguration.getInt(configNode.getRoot() + ".green");
        int blue = yamlConfiguration.getInt(configNode.getRoot() + ".blue");

        return Color.fromRGB(red, green, blue);
    }

    public String getMessage (ConfigNode messageNode) { return ChatColor.translateAlternateColorCodes('&', getString(ConfigNode.MESSAGES_PREFIX) + " " + getString(messageNode)); }
    public String getMessageNoColor (ConfigNode messageNode) { return getString(ConfigNode.MESSAGES_PREFIX) + " " + getString(messageNode); }
}
