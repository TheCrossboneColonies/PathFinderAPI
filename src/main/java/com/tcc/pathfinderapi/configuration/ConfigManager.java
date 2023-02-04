package com.tcc.pathfinderapi.configuration;

import com.tcc.pathfinderapi.PathFinderAPI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Used to obtain values from a configuration file.
 * Also updates outdated configuration files with new settings (does not remove old settings)
 */
public class ConfigManager {

    private PathFinderAPI plugin;

    private Map<String, YamlConfiguration> configs = new HashMap<>();
    /**
     * This value is updated whenever a change to any configuration file is made
     */
    private final float CONFIG_VERSION = 1.0F;

    private final List<String> configFiles;


    public ConfigManager(PathFinderAPI plugin) {
        this.plugin = plugin;

        configFiles = Arrays.asList(
                "config.yml",
                "lang.yml"
        );

    }

    public void loadConfigFiles() {

        // Ensure directory exists
        if(!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

        // Loop through each config file
        for (String filePath : configFiles) {
            File file = new File(plugin.getDataFolder() + File.separator + filePath);

            if (!file.exists()) {
                // Create file
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Check version and if it needs updating
            YamlConfiguration configFile = YamlConfiguration.loadConfiguration(file);
            String versionStr = configFile.getString("version");
            float version = versionStr == null ? -1F : Float.parseFloat(versionStr);

            if (version != CONFIG_VERSION) {
                // Use config nodes to update file
                updateConfigFile(filePath);
            }

            // Load config file
            configs.put(filePath, YamlConfiguration.loadConfiguration(file));

        }


    }

    public void updateConfigFile(String filePath) {

        File file = new File(plugin.getDataFolder() + File.separator + filePath);
        YamlConfiguration configFile = YamlConfiguration.loadConfiguration(file);

        // Get updated config from resources with defaults
        InputStream defaultStream = plugin.getResource(filePath);
        if(defaultStream == null) return;

        // Update version (done first so it is placed at top of file)
        configFile.set("version", CONFIG_VERSION);
        configFile.setComments("version", Arrays.asList("Do not change this value!"));

        YamlConfiguration defaultConfig = YamlConfiguration
                .loadConfiguration(new InputStreamReader(defaultStream));
        for(String key : defaultConfig.getKeys(true)){
            Object ob = defaultConfig.get(key);
            if(!(ob instanceof MemorySection)){
                if(configFile.get(key) == null){
                    configFile.set(key, ob);
                    if(defaultConfig.getComments(key) != null) {
                        configFile.setComments(key, defaultConfig.getComments(key));
                    }

                }
            }

        }

        // Save config file
        try {
            configFile.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public boolean getBoolean(ConfigNode configNode){
        YamlConfiguration config = configs.get(configNode.getFilePath());
        return config.getBoolean(configNode.getRoot());
    }

    public double getDouble(ConfigNode configNode){
        YamlConfiguration config = configs.get(configNode.getFilePath());
        return config.getDouble(configNode.getRoot());
    }

    public int getInt(ConfigNode configNode){
        YamlConfiguration config = configs.get(configNode.getFilePath());
        return config.getInt(configNode.getRoot());
    }

    public String getString(ConfigNode configNode){
        YamlConfiguration config = configs.get(configNode.getFilePath());
        return config.getString(configNode.getRoot());
    }

    public List<String> getStringList(ConfigNode configNode){
        YamlConfiguration config = configs.get(configNode.getFilePath());
        return config.getStringList(configNode.getRoot());
    }

    public List<Map<?,?>> getMapList(ConfigNode configNode){
        YamlConfiguration config = configs.get(configNode.getFilePath());
        return config.getMapList(configNode.getRoot());
    }

    public Location getLocation(ConfigNode configNode){
        YamlConfiguration config = configs.get(configNode.getFilePath());
        return config.getLocation(configNode.getRoot());
    }

    public String getMessage(ConfigNode messageNode) {
        return ChatColor.translateAlternateColorCodes('&', getString(ConfigNode.MESSAGES_PREFIX) + " " + getString(messageNode));
    }

    public String getMessageNoColor(ConfigNode messageNode) {
        return getString(ConfigNode.MESSAGES_PREFIX) + " " + getString(messageNode);
    }

}
