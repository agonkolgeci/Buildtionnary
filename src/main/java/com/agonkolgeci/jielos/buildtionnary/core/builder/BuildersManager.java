package com.agonkolgeci.jielos.buildtionnary.core.builder;

import com.agonkolgeci.jielos.buildtionnary.Buildtionnary;
import com.agonkolgeci.jielos.buildtionnary.api.word.WordsAPI;
import com.agonkolgeci.jielos.buildtionnary.core.GameManager;
import com.agonkolgeci.jielos.buildtionnary.core.GameStatus;
import com.agonkolgeci.jielos.buildtionnary.core.player.GamePlayer;
import com.agonkolgeci.jielos.buildtionnary.plugin.PluginAdapter;
import com.agonkolgeci.jielos.buildtionnary.plugin.events.ListenerAdapter;
import com.agonkolgeci.jielos.buildtionnary.utils.PlayerUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildersManager implements PluginAdapter, ListenerAdapter {

    private final GameManager gameManager;

    private final Map<GamePlayer, GameBuilder> builders;
    @Getter private GameBuilder builder;

    public BuildersManager(GameManager gameManager) {
        this.gameManager = gameManager;

        this.builders = new HashMap<>();
        this.builder = null;
    }

    @Override
    public void load() throws RuntimeException {
        this.clearArea();

        gameManager.getInstance().getEventsManager().registerAdapter(this);
    }

    @Override
    public void unload() {
        gameManager.getInstance().getEventsManager().unregisterAdapter(this);
    }

    public List<GamePlayer> getRemainingBuilders() {
        return gameManager.getPlayersManager().getPlayers().stream().filter(gameBuilder -> !builders.containsKey(gameBuilder)).toList();
    }

    private void clearArea() {
        gameManager.getConfig().getBuildArea().forEach(block -> block.setType(Material.AIR, false));

        for(Chunk chunk : gameManager.getConfig().getBuildArea().getChunks()) {
            for(Entity entity : chunk.getEntities()) {
                if(!(entity instanceof Player)) {
                    entity.remove();
                }
            }
        }
    }

    public void next() {
        if(this.builder != null) {
            this.broadcastStats();

            this.builder.cancel();
            this.builder = null;
        }

        this.clearArea();

        final List<GamePlayer> remainingBuilders = this.getRemainingBuilders();
        if(remainingBuilders.isEmpty()) {
            gameManager.finish();

            return;
        }

        final GamePlayer selectedPlayer = remainingBuilders.get(Buildtionnary.SPLITTABLE_RANDOM.nextInt(remainingBuilders.size()));
        final String word = WordsAPI.retrieveRandomWord();

        this.builder = new GameBuilder(gameManager, selectedPlayer, word);
        this.builders.put(selectedPlayer, builder);

        for(GamePlayer gamePlayer : gameManager.getPlayersManager().getPlayers()) {
            gamePlayer.getPlayer().setGameMode(GameMode.SPECTATOR);
            gamePlayer.getPlayer().teleport(gameManager.getConfig().getBuildArea().getCenter());

            PlayerUtils.clearContents(gamePlayer.getPlayer());
        }

        selectedPlayer.getPlayer().setGameMode(GameMode.CREATIVE);
        selectedPlayer.getPlayer().showTitle(Title.title(Component.text(word.toUpperCase(), NamedTextColor.YELLOW, TextDecoration.BOLD), Component.text("À vos pinceaux !"), Title.DEFAULT_TIMES));

        gameManager.getPlugin().getServer().broadcast(Component.text("\n")
                .append(Component.text("C'est au tour de ", NamedTextColor.GOLD))
                .append(Component.text(selectedPlayer.getPlayer().getName(), NamedTextColor.GRAY, TextDecoration.BOLD))
                .append(Component.text(" de construire le mot qui lui a été donné.\n", NamedTextColor.GOLD))
                .append(Component.text("Vous devez trouver le mot par le biais de sa construction et l'écrire dans le chat le plus rapidement possible.\n", NamedTextColor.GRAY, TextDecoration.ITALIC))
        );

        this.builder.runTaskTimer(gameManager.getPlugin(), 0, 20);

        gameManager.getInstance().getLogger().info("It's now " + selectedPlayer.getPlayer().getName() + "'s turn to build the word: " + word + ".");
    }

    public void broadcastStats() {
        final List<GamePlayer> builders = gameManager.getPlayersManager().getPlayers();
        final List<Component> lines = new ArrayList<>();

        lines.add(Component.text("Classement général des points", NamedTextColor.GREEN, TextDecoration.BOLD));

        for (int i = 0; i < builders.size(); i++) {
            final GamePlayer builder = builders.get(i);

            lines.add(Component.text()
                    .append(Component.text("#" + (i + 1) + " ", NamedTextColor.GOLD))
                    .append(Component.text(builder.getPlayer().getName(), NamedTextColor.YELLOW))
                    .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(builder.getPoints() + " points", NamedTextColor.GRAY))
                    .build()
            );
        }

        gameManager.getPlugin().getServer().sendMessage(Component.text().appendNewline().append(Component.join(Component::newline, lines)).appendNewline());
        gameManager.getPlugin().getServer().broadcast(Component.text()
                .append(Component.text("Le mot que vous deviez trouver était "))
                .append(Component.text(builder.getWord(), NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("."))
                .colorIfAbsent(NamedTextColor.GRAY)
                .build()
        );
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();

        if(player.isOp() && player.getGameMode() == GameMode.CREATIVE) return;

        if(!gameManager.getConfig().getBuildArea().contains(player.getLocation())) {
            player.teleport(gameManager.getConfig().getBuildArea().getCenter());
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        final Block block = event.getBlock();

        if(gameManager.getStatus() != GameStatus.PLAYING || !gameManager.getConfig().getBuildArea().contains(block.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Block block = event.getBlock();

        if(gameManager.getStatus() != GameStatus.PLAYING || !gameManager.getConfig().getBuildArea().contains(block.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncChatEvent event) {
        event.renderer((targetPlayer, sourceDisplayName, message, audience) -> {
            return Component.empty().append(sourceDisplayName.colorIfAbsent(NamedTextColor.DARK_GRAY)).appendSpace().append(Component.text(":")).appendSpace().append(message).colorIfAbsent(NamedTextColor.GRAY);
        });

        final Player player = event.getPlayer();

        switch (gameManager.getStatus()) {
            case PLAYING -> {
                if(!gameManager.getPlayersManager().isPlayer(player)) {
                    event.setCancelled(true);
                    player.sendMessage(Component.text("Vous ne pouvez pas écrire dans le chat car vous n'êtes pas en jeu.", NamedTextColor.RED));

                    return;
                }

                final GamePlayer gamePlayer = gameManager.getPlayersManager().getPlayer(player);
                if(gamePlayer != null && builder != null) {
                    if(builder.getGamePlayer() == gamePlayer) {
                        event.setCancelled(true);
                        player.sendMessage(Component.text("Vous ne pouvez pas écrire dans le chat car vous êtes le Constructeur.", NamedTextColor.RED));

                        return;
                    }

                    if(gamePlayer.hasFound(builder)) {
                        event.setCancelled(true);
                        player.sendMessage(Component.text("Vous avez déjà trouvé le mot.", NamedTextColor.GREEN));

                        return;
                    }

                    if(event.message() instanceof TextComponent text && WordsAPI.COLLATOR.compare(builder.getWord(), text.content()) == 0) {
                        gamePlayer.getFoundedBuilds().add(builder);

                        final int pointsToBuilder = 10;
                        final int pointsToFinder = (int) gameManager.getPlayersManager().getPlayers().stream().filter(anGamePlayer -> !anGamePlayer.getFoundedBuilds().contains(builder)).count() * 10;

                        builder.getGamePlayer().addPoints(pointsToBuilder);
                        gamePlayer.addPoints(pointsToFinder);

                        event.setCancelled(true);

                        gameManager.getPlugin().getServer().sendMessage(
                                Component.text()
                                        .append(Component.text(player.getName(), NamedTextColor.GRAY, TextDecoration.BOLD))
                                        .appendSpace()
                                        .append(Component.text("vient de trouver le mot !", NamedTextColor.GOLD))
                                        .build()
                        );
                    }

                    if(gameManager.getPlayersManager().getPlayers().stream().filter(anGamePlayer -> anGamePlayer != builder.getGamePlayer()).allMatch(anGamePlayer -> anGamePlayer.getFoundedBuilds().contains(builder))) {
                        Bukkit.getScheduler().runTask(gameManager.getPlugin(), this::next);
                    }
                }
            }
        }
    }


}
