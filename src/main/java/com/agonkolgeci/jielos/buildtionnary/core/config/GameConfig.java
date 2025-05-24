package com.agonkolgeci.jielos.buildtionnary.core.config;

import com.agonkolgeci.jielos.buildtionnary.Buildtionnary;
import com.agonkolgeci.jielos.buildtionnary.plugin.PluginManager;
import com.agonkolgeci.jielos.buildtionnary.utils.world.Cuboid;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;

@Getter
public class GameConfig extends PluginManager {

    private final FileConfiguration configuration;

    private int minPlayers;
    private int maxPlayers;

    private Component scoreboardName;
    private Component scoreboardFooter;

    private int timerStart;
    private int timerBuild;
    private int timerFinish;

    private Location waitingRoom;
    private Cuboid buildArea;

    public GameConfig(Buildtionnary instance) {
        super(instance);

        this.instance.getPlugin().saveDefaultConfig();
        this.configuration = instance.getPlugin().getConfig();

        this.minPlayers = Math.max(configuration.getInt("min-players"), 2);
        this.maxPlayers = configuration.getInt("max-players");

        this.scoreboardName = Buildtionnary.MINI_MESSAGE.deserialize(Objects.requireNonNull(configuration.getString("scoreboard.name")));
        this.scoreboardFooter = Buildtionnary.MINI_MESSAGE.deserialize(Objects.requireNonNull(configuration.getString("scoreboard.footer")));

        this.timerStart = configuration.getInt("timers.start");
        this.timerBuild = configuration.getInt("timers.build");
        this.timerFinish = configuration.getInt("timers.finish");

        this.waitingRoom = this.getLocation("waiting-room");
        this.buildArea = this.getCuboid("build-area");
    }

    private Location getLocation(String path) {
        final World world = instance.getPlugin().getServer().getWorld(Objects.requireNonNullElseGet(configuration.getString(path + ".world"), () -> {
            instance.getPlugin().getLogger().warning("Can't find world in config at path " + path + ", using primary world instead.");

            return instance.getPlugin().getServer().getWorlds().stream().findFirst().orElseThrow().getName();
        }));

        final double x = Objects.requireNonNull((Double) configuration.get(path + ".x"));
        final double y = Objects.requireNonNull((Double) configuration.get(path + ".y"));
        final double z = Objects.requireNonNull((Double) configuration.get(path + ".z"));
        final float yaw = (float) configuration.getDouble(path + ".yaw");
        final float pitch = (float) configuration.getDouble(path + ".pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    private Cuboid getCuboid(String path) {
        return new Cuboid(this.getLocation(path + ".1"), this.getLocation(path + ".2"));
    }

}
