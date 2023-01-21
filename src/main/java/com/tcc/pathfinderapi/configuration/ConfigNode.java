package com.tcc.pathfinderapi.configuration;

public enum ConfigNode {

    MESSAGES_PREFIX("lang.yml","messages.prefix");

    private final String FILE_PATH;
    private final String ROOT;

    ConfigNode(String file, String root) {
        this.FILE_PATH = file;
        this.ROOT = root;
    }

    /**
     * Retrieves file path for a config option
     *
     * @return The file path for a config option
     */
    public String getFilePath() {

        return FILE_PATH;
    }

    /**
     * Retrieves the root for a config option
     *
     * @return The root for a config option
     */
    public String getRoot() {

        return ROOT;
    }
}
