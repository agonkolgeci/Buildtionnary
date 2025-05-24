package com.agonkolgeci.jielos.buildtionnary.core;

import com.agonkolgeci.jielos.buildtionnary.Buildtionnary;
import com.agonkolgeci.jielos.buildtionnary.core.board.GameBoard;
import com.agonkolgeci.jielos.buildtionnary.core.builder.BuildersManager;
import com.agonkolgeci.jielos.buildtionnary.core.config.GameConfig;
import com.agonkolgeci.jielos.buildtionnary.core.player.GamePlayer;
import com.agonkolgeci.jielos.buildtionnary.core.player.GameUser;
import com.agonkolgeci.jielos.buildtionnary.core.player.PlayersManager;
import com.agonkolgeci.jielos.buildtionnary.core.scheduler.GameStartScheduler;
import com.agonkolgeci.jielos.buildtionnary.plugin.PluginAdapter;
import com.agonkolgeci.jielos.buildtionnary.plugin.PluginManager;
import com.agonkolgeci.jielos.buildtionnary.plugin.events.ListenerAdapter;
import com.agonkolgeci.jielos.buildtionnary.utils.PlayerUtils;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class GameManager extends PluginManager implements PluginAdapter, ListenerAdapter {

    private final GameConfig config;

    @Setter private GameStatus status;

    private final PlayersManager playersManager;
    private final BuildersManager buildersManager;

    @Nullable private GameStartScheduler startScheduler;

    public GameManager(Buildtionnary instance) {
        super(instance);

        this.config = new GameConfig(instance);
        this.status = GameStatus.WAITING_FOR_PLAYERS;

        this.playersManager = new PlayersManager(this);
        this.buildersManager = new BuildersManager(this);
    }

    @Override
    public void load() throws RuntimeException {
        instance.getEventsManager().registerAdapter(this);

        playersManager.load();
        buildersManager.load();

        Bukkit.getScheduler().runTaskTimerAsynchronously(instance.getPlugin(), () -> playersManager.getUsers().stream().map(GameUser::getBoard).forEach(GameBoard::update), 0, 1);
    }

    @Override
    public void unload() {
        instance.getEventsManager().unregisterAdapter(this);

        playersManager.unload();
        buildersManager.unload();
    }

    public int getMissingPlayers() {
        return config.getMinPlayers() - playersManager.getPlayers().size();
    }

    public boolean hasSufficientPlayers() {
        return playersManager.getPlayers().size() >= config.getMinPlayers();
    }

    public void checkStatus() {
        switch (status) {
            case WAITING_FOR_PLAYERS -> {
                if(!hasSufficientPlayers()) return;

                this.start();
            }

            case STARTING -> {
                if(hasSufficientPlayers()) return;

                this.status = GameStatus.WAITING_FOR_PLAYERS;

                if(startScheduler != null) {
                    this.startScheduler.cancel();
                    this.startScheduler = null;
                }

                instance.getPlugin().getServer().broadcast(Component.text("Impossible de démarrer la partie, visiblement il n'y a plus assez de joueurs.", NamedTextColor.RED));
            }

            default -> {
                if(playersManager.getPlayers().size() > 1) return;

                this.finish();
            }
        }
    }

    public void start() {
        if(startScheduler != null) throw new IllegalStateException("Game is already starting");

        this.status = GameStatus.STARTING;

        this.startScheduler = new GameStartScheduler(this);
        this.startScheduler.runTaskTimer(instance.getPlugin(), 0, 20);
    }

    public void play() {
        this.status = GameStatus.PLAYING;

        instance.getPlugin().getServer().broadcast(Component.text("Lancement de la partie en cours, téléportation des joueurs en cours...", NamedTextColor.YELLOW));

        for(GamePlayer builder : playersManager.getPlayers()) {
            builder.getPlayer().setGameMode(GameMode.SPECTATOR);
            builder.getPlayer().teleport(config.getWaitingRoom());
        }

        this.buildersManager.next();
    }

    public void finish() {
        this.status = GameStatus.FINISH;

        this.startScheduler.cancel();

        final List<GamePlayer> winners = playersManager.getWinners();
        for(Player player : instance.getPlugin().getServer().getOnlinePlayers()) {
            PlayerUtils.clearContents(player);

            player.setGameMode(GameMode.ADVENTURE);
            player.teleport(config.getWaitingRoom());
        }

        final int maxPoints = winners.getFirst().getPoints();
        instance.getPlugin().getServer().sendMessage(
                Component.text()
                        .appendNewline()
                        .append(Component.text("Félicitations à", NamedTextColor.GREEN, TextDecoration.BOLD))
                        .appendSpace()
                        .append(Component.text(winners.stream().map(gamePlayer -> gamePlayer.getPlayer().getName()).collect(Collectors.joining(", ")), NamedTextColor.GRAY, TextDecoration.BOLD))
                        .appendSpace()
                        .append(Component.text("qui remport" + (winners.size() > 1 ? "ent" : "e"), NamedTextColor.GREEN, TextDecoration.BOLD))
                        .appendSpace()
                        .append(Component.text("la partie avec un total de", NamedTextColor.GREEN, TextDecoration.BOLD))
                        .appendSpace()
                        .append(Component.text(maxPoints + " points", NamedTextColor.YELLOW))
                        .append(Component.text(".", NamedTextColor.GREEN, TextDecoration.BOLD))
                        .appendNewline()
        );

        instance.getPlugin().getServer().getScheduler().runTaskLater(instance.getPlugin(), () -> {
            instance.getPlugin().getServer().reload();
        }, config.getTimerFinish() * 20L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        event.joinMessage(null);

        switch (status) {
            case WAITING_FOR_PLAYERS, STARTING -> playersManager.addPlayer(player);
            default -> playersManager.addSpectator(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        event.quitMessage(null);

        if(playersManager.isPlayer(player)) {
            playersManager.removePlayer(player);
        } else if(playersManager.isSpectator(player)) {
            playersManager.removeSpectator(player);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        final Entity entity = event.getEntity();
        if(entity instanceof Player) {
            event.setCancelled(true);
        }
    }

}
