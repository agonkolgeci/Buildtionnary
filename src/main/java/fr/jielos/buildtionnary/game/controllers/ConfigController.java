package fr.jielos.buildtionnary.game.controllers;

import fr.jielos.buildtionnary.Buildtionnary;
import fr.jielos.buildtionnary.PluginComponent;
import fr.jielos.buildtionnary.components.world.Cuboid;
import fr.jielos.buildtionnary.game.Game;
import fr.jielos.buildtionnary.game.GameComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigController extends GameComponent {

    private final FileConfiguration fileConfiguration;
    public ConfigController(Buildtionnary instance, Game game) {
        super(instance, game);

        this.fileConfiguration = instance.getConfig();
    }

    public String getString(Value value) {
        return ChatColor.translateAlternateColorCodes('&', fileConfiguration.getString(value.getPath()));
    }

    public int getInt(Value value) {
        final int integer = fileConfiguration.getInt(value.getPath());

        switch (value) {
            case MIN_PLAYERS: {
                return Math.max(integer, 2);
            }
        }

        return integer;
    }

    public boolean getBoolean(Value value) {
        return fileConfiguration.getBoolean(value.getPath());
    }

    public Location getLocation(Value value) {
        return (Location) fileConfiguration.get(value.getPath());
    }

    public Cuboid getArea(Value value) {
        final Location firstLocation = (Location) fileConfiguration.get(value.getPath() + ".1");
        final Location secondLocation = (Location) fileConfiguration.get(value.getPath() + ".2");
        if(firstLocation == null || secondLocation == null) return null;

        return new Cuboid(firstLocation, secondLocation);
    }

    public ConfigurationSection getConfigurationSection(Value value) {
        return fileConfiguration.getConfigurationSection(value.getPath());
    }

    public enum Value {
        // Configuration
        MIN_PLAYERS("min-players"),
        MAX_PLAYERS("max-players"),

        // Scoreboard
        SCOREBOARD_NAME("scoreboard.name"),
        SCOREBOARD_FOOTER_ENABLED("scoreboard.footer.enabled"),
        SCOREBOARD_FOOTER_CONTENT("scoreboard.footer.content"),

        // Timers
        TIMER_LAUNCH("timers.launch"),
        TIMER_BUILD("timers.build"),
        TIMER_FINISH("timers.finish"),

        // Locations
        WAITING_ROOM("locations.waiting-room"),
        BUILD_AREA("locations.build-area");

        private final String path;

        Value(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

}
