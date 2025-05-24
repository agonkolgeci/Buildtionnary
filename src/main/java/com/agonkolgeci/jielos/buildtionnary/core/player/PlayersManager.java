package com.agonkolgeci.jielos.buildtionnary.core.player;

import com.agonkolgeci.jielos.buildtionnary.Buildtionnary;
import com.agonkolgeci.jielos.buildtionnary.core.GameManager;
import com.agonkolgeci.jielos.buildtionnary.plugin.PluginAdapter;
import com.agonkolgeci.jielos.buildtionnary.utils.PlayerUtils;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class PlayersManager implements PluginAdapter {

    private final GameManager gameManager;

    private final Map<UUID, GameUser> users;
    private final Map<UUID, GamePlayer> players;
    private final Map<UUID, GameSpectator> spectators;

    public PlayersManager(GameManager gameManager) {
        this.gameManager = gameManager;

        this.users = new HashMap<>();
        this.players = new HashMap<>();
        this.spectators = new HashMap<>();
    }

    @Override
    public void load() throws RuntimeException {
        gameManager.getPlugin().getServer().getOnlinePlayers().forEach(this::addPlayer);
    }

    @Override
    public void unload() {
    }

    public void addPlayer(Player player) {
        users.put(player.getUniqueId(), players.compute(player.getUniqueId(), (uuid, gamePlayer) -> {
            if(gamePlayer != null) throw new IllegalStateException("Player already registered");

            return new GamePlayer(gameManager, player);
        }));

        PlayerUtils.clearContents(player);

        player.teleport(gameManager.getConfig().getWaitingRoom());
        player.setGameMode(GameMode.ADVENTURE);

        final Component joinMessage = Component.text()
                .append(Component.text(player.getName(), NamedTextColor.GRAY))
                .appendSpace()
                .append(Component.text("a rejoint la partie ! "))
                .append(Component.text("(" + players.size() + "/" + gameManager.getConfig().getMaxPlayers() + ")", NamedTextColor.GREEN))
                .colorIfAbsent(NamedTextColor.YELLOW)
                .build();

        gameManager.getPlugin().getServer().broadcast(joinMessage);
        gameManager.getPlugin().getServer().sendActionBar(joinMessage);

        final PluginMeta pluginMeta = gameManager.getPlugin().getPluginMeta();
        player.sendMessage(Component.text()
                .appendNewline()
                .append(Component.text(pluginMeta.getName(), NamedTextColor.GOLD, TextDecoration.BOLD))
                .appendNewline()
                .append(Component.text(Objects.requireNonNull(pluginMeta.getDescription()), NamedTextColor.GRAY))
                .build()
        );

        gameManager.checkStatus();
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        users.remove(player.getUniqueId());

        switch (gameManager.getStatus()) {
            case WAITING_FOR_PLAYERS, STARTING ->  {
                final Component quitMessage = Component.text()
                        .append(Component.text(player.getName(), NamedTextColor.GRAY))
                        .appendSpace()
                        .append(Component.text("a quitté la partie ! "))
                        .append(Component.text("(" + players.size() + "/" + gameManager.getConfig().getMaxPlayers() + ")", NamedTextColor.RED))
                        .colorIfAbsent(NamedTextColor.YELLOW)
                        .build();

                gameManager.getPlugin().getServer().broadcast(quitMessage);
                gameManager.getPlugin().getServer().sendActionBar(quitMessage);
            }

            default -> {
                gameManager.getPlugin().getServer().broadcast(
                        Component.text()
                            .append(Component.text(player.getName(), NamedTextColor.GRAY, TextDecoration.BOLD))
                            .append(Component.space())
                            .append(Component.text("s'est déconnecté en plein jeu.", NamedTextColor.RED))
                            .build()
                );
            }
        }

        gameManager.checkStatus();
    }

    public boolean isPlayer(Player player) {
        return players.containsKey(player.getUniqueId());
    }

    public GamePlayer getPlayer(Player player) {
        return players.getOrDefault(player.getUniqueId(), null);
    }

    public void addSpectator(@NotNull Player player) {
        users.put(player.getUniqueId(), spectators.computeIfAbsent(player.getUniqueId(), uuid -> {
            if(spectators.containsKey(player.getUniqueId())) throw new IllegalStateException("Spectator already registered");

            return new GameSpectator(gameManager, player);
        }));

        PlayerUtils.clearContents(player);

        player.teleport(gameManager.getConfig().getWaitingRoom());
        player.setGameMode(GameMode.SPECTATOR);

        player.sendMessage(
                Component.text()
                        .append(Component.text("Vous êtes désormais "))
                        .append(Component.text("Spectateur", NamedTextColor.DARK_GRAY))
                        .append(Component.text(", vous rejoindrez la prochaine partie automatiquement."))
                        .colorIfAbsent(NamedTextColor.GRAY).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.TRUE)
        );

        player.showTitle(Title.title(
                Component.text("MODE SPECTATEUR", NamedTextColor.DARK_GRAY, TextDecoration.BOLD),
                Component.empty(),
                Title.Times.times(Ticks.duration(10), Ticks.duration(70), Ticks.duration(20))
        ));
    }

    public boolean isSpectator(Player player) {
        return spectators.containsKey(player.getUniqueId());
    }

    public void removeSpectator(Player player) {
        spectators.remove(player.getUniqueId());
        users.remove(player.getUniqueId());
    }

    public List<GameUser> getUsers() {
        return users.values().stream().toList();
    }

    public List<GamePlayer> getPlayers() {
        return players.values().stream().sorted(Comparator.comparingInt(GamePlayer::getPoints).reversed()).collect(Collectors.toList());
    }

    public List<GameSpectator> getSpectators() {
        return spectators.values().stream().toList();
    }

    public List<GamePlayer> getWinners() {
        return players.values().stream().collect(Collectors.groupingBy(GamePlayer::getPoints)).entrySet().stream().max(Map.Entry.comparingByKey()).orElseThrow().getValue();
    }
}
