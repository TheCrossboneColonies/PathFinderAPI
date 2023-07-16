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
- `pathapi.find.*` - Allows usage of all the `/pathapi find` commands.
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

- [`com.tcc.pathfinderapi.api.visualizers.BlockVisualizer`](https://github.com/TheCrossboneColonies/PathFinderAPI/blob/master/src/main/java/com/tcc/pathfinderapi/api/visualizers/BlockVisualizer.java)
- [`com.tcc.pathfinderapi.api.visualizers.ParticleVisualizer`](https://github.com/TheCrossboneColonies/PathFinderAPI/blob/master/src/main/java/com/tcc/pathfinderapi/api/visualizers/ParticleVisualizer.java)

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
