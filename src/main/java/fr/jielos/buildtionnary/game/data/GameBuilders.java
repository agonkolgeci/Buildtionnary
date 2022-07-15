package fr.jielos.buildtionnary.game.data;

import fr.jielos.buildtionnary.Buildtionnary;
import fr.jielos.buildtionnary.components.world.Cuboid;
import fr.jielos.buildtionnary.game.Game;
import fr.jielos.buildtionnary.game.GameComponent;
import fr.jielos.buildtionnary.game.controllers.ConfigController;
import fr.jielos.buildtionnary.game.data.players.GameBuilder;
import fr.jielos.buildtionnary.game.data.players.GamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.Collator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class GameBuilders extends GameComponent implements Listener {

    private final Cuboid buildArea;
    private final Collator collator;

    private GameBuilder gameBuilder;
    public GameBuilders(Buildtionnary instance, Game game) {
        super(instance, game);

        this.buildArea = game.getConfigController().getArea(ConfigController.Value.BUILD_AREA);
        if(buildArea != null) {
            clearBuildArea();
        }

        this.collator = Collator.getInstance();
        this.collator.setStrength(Collator.NO_DECOMPOSITION);

        instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    public void clearBuildArea() {
        buildArea.forEach(block -> {
            if(block.getY() > buildArea.getLowerY()) {
                block.setType(Material.AIR);
            } else {
                block.setType(Material.GRASS);
            }
        });

        for(Chunk chunk : buildArea.getChunks()) {
            for(Entity entity : chunk.getEntities()) {
                if(!(entity instanceof Player)) {
                    entity.remove();
                }
            }
        }
    }

    public void nextBuilder() {
        clearBuildArea();

        if(!game.checkFinish()) {
            setGameBuilder(new GameBuilder(instance, game, getRandomBuilder()));

            for(GamePlayer anGamePlayer : game.getGameData().getGamePlayers().values()) {
                final Player anPlayer = anGamePlayer.getPlayer();

                if(anPlayer == gameBuilder.getGamePlayer().getPlayer()) {
                    anPlayer.setGameMode(GameMode.CREATIVE);
                } else {
                    anPlayer.setGameMode(GameMode.SPECTATOR);
                }

                anPlayer.teleport(buildArea.getCenter());
                game.getBoardController().updatePlayerBoard(anPlayer);
            }

            instance.getInitializer().sendTitle(gameBuilder.getPlayer(), ChatColor.YELLOW.toString() + ChatColor.BOLD + gameBuilder.getWord().toUpperCase(), "§7À vos pinceaux !", 10, 70, 20);
            instance.getServer().broadcastMessage(String.format(" \n§6C'est au tour de §7§l%s §6de construire le mot qui lui a été donné.\n§7§oVous devez trouver le mot par le bias de sa construction et l'écrire dans le chat le plus rapidement possible.\n ", gameBuilder.getPlayer().getName()));
        }
    }
    public boolean canNext() {
        return gameBuilder != null && game.getGameData().getGamePlayers().values().stream().noneMatch(anGamePlayer -> !anGamePlayer.hasFound(gameBuilder.getGamePlayer()) && anGamePlayer != gameBuilder.getGamePlayer());
    }
    public void checkNext() {
        if(canNext()) {
            gameBuilder.stop();
        }
    }
    public void broadcastStats(GameBuilder oldGameBuilder) {
        final List<GamePlayer> gamePlayersSorted = game.getGameData().getSortedGamePlayers();
        final StringJoiner stringJoiner = new StringJoiner("\n");

        stringJoiner.add("§a§lClassement général des points");

        for(GamePlayer gamePlayer : gamePlayersSorted) {
            stringJoiner.add(String.format("§6#%d §e%s §8- §7%d points", gamePlayersSorted.indexOf(gamePlayer)+1, gamePlayer.getPlayer().getName(), gamePlayer.getPoints()));
        }

        instance.getServer().broadcastMessage(String.format(" \n%s\n ", stringJoiner));
        instance.getServer().broadcastMessage(String.format("§7Le mot que vous deviez trouver était §6§l%s§7.", oldGameBuilder.getWord()));
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if(game.isPlaying()) {
            final Player anPlayer = event.getPlayer();
            if(!buildArea.contains(anPlayer.getLocation())) {
                anPlayer.teleport(buildArea.getCenter());
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        final Block block = event.getBlock();

        if(game.isPlaying()) {
            if(!buildArea.contains(block.getLocation())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Block block = event.getBlock();

        if(game.isPlaying()) {
            if(!buildArea.contains(block.getLocation()) || block.getY() == buildArea.getLowerY()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();

        if(game.isPlaying() && game.getGameController().isPlayer(player)) {
            final GamePlayer gamePlayer = game.getGameData().getGamePlayer(player);
            final GamePlayer gameBuilderPlayer = getGameBuilder().getGamePlayer();

            if(gamePlayer != gameBuilderPlayer) {
                if(!gamePlayer.hasFound(gameBuilderPlayer)) {
                    final String trueWord = gameBuilder.getWord();
                    final String targetWord = event.getMessage();

                    if(collator.compare(trueWord, targetWord) == 0) {
                        event.setCancelled(true);

                        final int pointsWon = (int) game.getGameData().getGamePlayers().values().stream().filter(anGamePlayer -> !anGamePlayer.getFoundedWords().contains(gameBuilderPlayer) && anGamePlayer != gameBuilder.getGamePlayer()).count() * 10;

                        instance.getServer().broadcastMessage(String.format("§7§l%s §6vient de trouver le mot !", player.getName()));

                        gameBuilderPlayer.addPoints(10);
                        gamePlayer.addPoints(pointsWon);
                        gamePlayer.getFoundedWords().add(gameBuilderPlayer);

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                checkNext();
                            }
                        }.runTask(instance);
                    }
                } else {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        }

        event.setFormat("§8%1$s: §7%2$s");
    }

    public List<GamePlayer> getRemainingBuilders() {
        return game.getGameData().getGamePlayers().values().stream().filter(gamePlayer -> !gamePlayer.hasBuild() && game.getGameData().hasGamePlayer(gamePlayer.getPlayer())).collect(Collectors.toList());
    }
    public GamePlayer getRandomBuilder() {
        final List<GamePlayer> remainingBuilders = getRemainingBuilders();
        return remainingBuilders.get(instance.getSplittableRandom().nextInt(remainingBuilders.size()));
    }

    public GameBuilder getGameBuilder() {
        return gameBuilder;
    }
    public void setGameBuilder(GameBuilder gameBuilder) {
        this.gameBuilder = gameBuilder;
    }

}
