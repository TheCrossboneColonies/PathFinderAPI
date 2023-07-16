# PathFinderAPI
An API to intelligently generate paths between locations in a Minecraft world.

## About PathFinderAPI
PathFinderAPI is a quick and intelligent library developed around the [A* Search Algorithm](https://en.wikipedia.org/wiki/A*_search_algorithm). However, our team has modified it to be even faster by making it a [Heuristic Greedy Solution](https://en.wikipedia.org/wiki/Greedy_algorithm). This project utilizes algorithms seen in the real world to deliver high-speed and efficient path generation in Minecraft.

## Support
Looking for help with PathFinderAPI? You can [open an issue](https://github.com/TheCrossboneColonies/PathFinderAPI/issues/new) on our GitHub to seek support from one of our talented team members.

## Contributing
PathFinderAPI has always been an open-source, public plugin, and we're very open to community collaboration and involvement! Whether you have an idea to improve the plugin or want to code your own integrations, we'd love for you to check out our [GitHub](https://github.com/TheCrossboneColonies/PathFinderAPI).

## Usage
While PathFinderAPI is intended to be utilized as an extension of other plugins, it is packaged with a few default commands and permissions.

### Commands
- `/pathapi find blocks [Start; X Y Z] [End; X Y Z]` - Finds a path between two locations of a world using a block visualization.
- `/pathapi find particles [Start; X Y Z] [End; X Y Z]` - Finds a path between two locations of a world using a particle visualization.

### Permissions
- `pathapi.find.*` - Allows usage of all the `/pathapi find`` commands.
    - `pathapi.find.blocks` - Allows usage of the `/pathapi find blocks` command.
    - `pathapi.find.particles` - Allows usage of the `/pathapi find particles` command.

## Development
PathFinderAPI is designed to be easily integrated into any Spigot plugin with minimal effort needed from other developers.

### Importing
TODO

### API Methods
All of the necessary classes and methods for utilizing the PathFinderAPI are accessible from `com.tcc.pathfinderapi.api`.

The most basic and fundamental way to access PathFinderAPI's path generation is through `com.tcc.pathfinderapi.api.Path`. Here's a simple example that communicates to a player the number of blocks they must traverse to travel to a given destination.

```java
assert player instanceof org.bukkit.entity.Player;
assert destination instanceof org.bukkit.Location;

Path path = new Path(player, player.getLocation(), destination);
path.generatePath();

player.sendMessage(path.fullPath.size());
```

While generating a path is fun and all, the real excitement comes from actually being able to manipulate player and world data based on the outcomes of path generation. This all happens through the `com.tcc.pathfinderapi.api.visualizers.PathVisualizer` interface. Here are two examples that are included in PathFinderAPI as default visualizers.

**Block Visualizer:**
```java
public class BlockVisualizer implements PathVisualizer {
    
    private Map<Coordinate, BlockData> blockData;
    private ConfigManager configManager = ConfigManager.getInstance();

    @Override
    public void initializePath (Player player, LinkedList<Coordinate> fullPath) {

        this.blockData = new HashMap<Coordinate, BlockData>();
        Material material = Material.matchMaterial(configManager.getString(ConfigNode.BLOCK_VISUALIZER_BLOCK_TYPE));

        for (Coordinate coordinate : fullPath) {

            new BukkitRunnable() {

                @Override
                public void run () {

                    Block block = player.getWorld().getBlockAt(coordinate.getX(), coordinate.getY(), coordinate.getZ());
                    blockData.put(coordinate, block.getBlockData());
                    block.setType(material);
                }
            }.runTaskLater(Bukkit.getPluginManager().getPlugin("PathFinderAPI"), this.configManager.getInt(ConfigNode.BLOCK_VISUALIZER_BLOCK_DELAY) * fullPath.indexOf(coordinate));
        }
    }

    @Override
    public void interpretOldPath (Player player, LinkedList<Coordinate> relativePath) {}

    @Override
    public void interpretNewPath (Player player, LinkedList<Coordinate> relativePath) {}

    @Override
    public void endPath (Player player, LinkedList<Coordinate> fullPath) {

        for (Coordinate coordinate : fullPath) {

            Block block = player.getWorld().getBlockAt(coordinate.getX(), coordinate.getY(), coordinate.getZ());
            block.setBlockData(this.blockData.get(coordinate));
        }
    }
}
```

**Particle Visualizer:**
```java
public class ParticleVisualizer implements PathVisualizer {

    private boolean pathCompleted;
    private List<Coordinate> particleCoordinates;
    private List<Coordinate> oldParticleCoordinates;
    private Map<Coordinate, Double> oldParticleHeightAdditions;
    private ConfigManager configManager = ConfigManager.getInstance();

    @Override
    public void initializePath (Player player, LinkedList<Coordinate> fullPath) {

        this.pathCompleted = false;
        this.particleCoordinates = new ArrayList<Coordinate>();
        this.oldParticleCoordinates = new ArrayList<Coordinate>();
        this.oldParticleHeightAdditions = new HashMap<Coordinate, Double>();

        new BukkitRunnable() {

            @Override
            public void run () {

                while (!pathCompleted) {

                    for (Coordinate particleCoordinate : particleCoordinates) {

                        double heightAddition = oldParticleHeightAdditions.getOrDefault(particleCoordinate, 1.25);
                        if (oldParticleCoordinates.contains(particleCoordinate)) { heightAddition += 0.025; }
                        if (heightAddition > 2.0) { heightAddition = 1.25; }
                        oldParticleHeightAdditions.put(particleCoordinate, heightAddition);

                        DustOptions dustOptions = new DustOptions(configManager.getColor(ConfigNode.PARTICLE_VISUALIZER_PARTICLE_COLOR), 1.0F);
                        player.spawnParticle(Particle.REDSTONE, particleCoordinate.getX(), particleCoordinate.getY() + heightAddition, particleCoordinate.getZ(), 50, dustOptions);
                    }

                    oldParticleCoordinates = particleCoordinates;
                    try { Thread.sleep(100); }
                    catch (InterruptedException interruptedException) { interruptedException.printStackTrace(); }
                }
            }
        }.runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("PathFinderAPI"));
    }

    @Override
    public void interpretOldPath (Player player, LinkedList<Coordinate> relativePath) { this.particleCoordinates.clear(); }

    @Override
    public void interpretNewPath (Player player, LinkedList<Coordinate> relativePath) {

        int leadIndex = Math.min(this.configManager.getInt(ConfigNode.PARTICLE_VISUALIZER_PARTICLE_LEAD), relativePath.size() - 1);
        this.particleCoordinates.add(relativePath.get(leadIndex));
    }

    @Override
    public void endPath (Player player, LinkedList<Coordinate> fullPath) {

        this.pathCompleted = true;
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
    }
}
```

Here are two examples that utilze the above visualizers to generate paths between a player and a given destination.

**Block Visualizer:**
```java
assert player instanceof org.bukkit.entity.Player;
assert destination instaceof org.bukkit.Location;

Path path = new Path(player, player.getLocation(), destination, new BlockVisualizer());
path.generatePath();
```

**Particle Visualizer:**
```java
assert player instanceof org.bukkit.entity.Player;
assert destination instaceof org.bukkit.Location;

Path path = new Path(player, player.getLocation(), destination, new ParticleVisualizer());
path.generatePath();
```
