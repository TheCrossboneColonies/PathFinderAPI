package com.tcc.pathfinderapi.configuration;

public enum ConfigNode {

    DEBUG_MODE_ENABLED("config.yml", "debug_mode_enabled"),
    PERFORMANCE_CHUNK_INVALIDATION_TIME("config.yml", "performance.chunk_invalidation_time"),
    PERFORMANCE_RELATIVE_RADIUS("config.yml", "performance.relative_radius"),

    BLOCK_VISUALIZER_BLOCK_TYPE("config.yml", "visualizers.block.block_type"),
    BLOCK_VISUALIZER_BLOCK_DELAY("config.yml", "visualizers.block.block_delay"),

    PARTICLE_VISUALIZER_PARTICLE_COLOR("config.yml", "visualizers.particle.particle_color"),
    PARTICLE_VISUALIZER_PARTICLE_LEAD("config.yml", "visualizers.particle.particle_lead"),

    MESSAGES_PREFIX("lang.yml","messages.prefix");

    private final String FILE_PATH;
    private final String ROOT;

    ConfigNode (String file, String root) {

        this.FILE_PATH = file;
        this.ROOT = root;
    }

    /**
     * Retrieves file path for a config option.
     *
     * @return The file path for a config option
     */
    public String getFilePath () { return this.FILE_PATH; }

    /**
     * Retrieves the root for a config option.
     *
     * @return The root for a config option
     */
    public String getRoot () { return this.ROOT; }
}
