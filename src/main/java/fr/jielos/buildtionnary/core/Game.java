package fr.jielos.buildtionnary.core;

import fr.jielos.buildtionnary.Buildtionnary;
import fr.jielos.buildtionnary.PluginComponent;
import fr.jielos.buildtionnary.core.board.BoardController;
import fr.jielos.buildtionnary.core.cache.GameBuilders;
import fr.jielos.buildtionnary.core.cache.GameCache;
import fr.jielos.buildtionnary.core.cache.player.GamePlayer;
import fr.jielos.buildtionnary.core.config.ConfigController;
import fr.jielos.buildtionnary.core.cache.status.GameStatus;
import fr.jielos.buildtionnary.core.schedulers.GameLaunchScheduler;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Game extends PluginComponent {

    private final GameController gameController;
    private final ConfigController configController;
    private final BoardController boardController;

    private final GameCache gameCache;
    private final GameBuilders gameBuilders;

    private GameStatus status;

    private GameLaunchScheduler gameLaunchScheduler;

    public Game(Buildtionnary instance) {
        super(instance);

        this.gameController = new GameController(instance, this);
        this.configController = new ConfigController(instance, this);
        this.boardController = new BoardController(instance, this);

        this.gameCache = new GameCache(instance, this);
        this.gameBuilders = new GameBuilders(instance, this);
    }

    public void initGame() {
        setStatus(GameStatus.WAITING_FOR_PLAYERS);

        gameController.loadOnlinePlayers();
    }

    public boolean canLaunch() {
        final int playersSize = gameCache.getPlayers().size();
        final int minPlayers = configController.getInt(ConfigController.Value.MIN_PLAYERS);
        return playersSize >= minPlayers && isWaiting();
    }
    public void checkLaunch() {
        if(canLaunch() && !isLaunching()) launchGame();
    }
    public void launchGame() {
        setStatus(GameStatus.LAUNCHING);

        gameLaunchScheduler = new GameLaunchScheduler(instance, this);
        gameLaunchScheduler.runTaskTimer(instance, 0, 20);
    }
    public void cancelLaunch() {
        if(isLaunching()) {
            gameLaunchScheduler.stop();

            setStatus(GameStatus.WAITING_FOR_PLAYERS);

            instance.getServer().broadcastMessage("§cImpossible de démarrer la partie, visiblement il n'y a plus assez de joueurs.");

            for(Player player : instance.getServer().getOnlinePlayers()) {
                boardController.updatePlayerBoard(player);
            }
        }
    }

    public void startGame() {
        setStatus(GameStatus.PLAYING);

        instance.getServer().broadcastMessage("§eLancement de la partie en cours, téléportation des joueurs en cours...");

        for(Player player : gameCache.getPlayers()) {
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(configController.getLocation(ConfigController.Value.WAITING_ROOM));

            gameCache.addGamePlayer(player);
        }

        gameBuilders.nextBuilder();
    }

    public boolean canFinish() {
        return gameBuilders.getRemainingBuilders().isEmpty() || gameCache.getGamePlayers().size() <= 1;
    }
    public boolean checkFinish() {
        if(canFinish()) {
            finishGame();
            return true;
        }

        return false;
    }
    public void finishGame() {
        setStatus(GameStatus.FINISH);

        instance.getServer().getScheduler().getPendingTasks().forEach(BukkitTask::cancel);

        final List<GamePlayer> gameWinners = gameCache.setGameWinners(gameCache.getSortedGamePlayers().stream().collect(Collectors.groupingBy(GamePlayer::getPoints)).entrySet().stream().max(Map.Entry.comparingByKey()).get().getValue());
        for(Player player : instance.getServer().getOnlinePlayers()) {
            gameController.clearContents(player);

            player.setGameMode(GameMode.ADVENTURE);
            player.teleport(configController.getLocation(ConfigController.Value.WAITING_ROOM));

            boardController.updatePlayerBoard(player);
        }

        final int maxPoints = gameWinners.get(0).getPoints();
        instance.getServer().broadcastMessage(String.format(" \n§a§lFélicitations à§r §7§l%s§r §a§lqui remporte%s la partie avec un total de§r §e%d points§r§a§l.\n§7§oVous pouvez observer toutes vos statistiques depuis le tableau des score à droite de votre écran.\n ", gameWinners.stream().map(gamePlayer -> gamePlayer.getPlayer().getName()).collect(Collectors.joining(", ")), gameWinners.size() > 1 ? "nt" : "", maxPoints));

        new BukkitRunnable() {
            @Override
            public void run() {
                instance.getServer().reload();
            }
        }.runTaskLater(instance, configController.getInt(ConfigController.Value.TIMER_FINISH) * 20L);
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }
    public GameStatus getStatus() {
        return status;
    }

    public boolean isWaiting() {
        return status == GameStatus.WAITING_FOR_PLAYERS || isLaunching();
    }
    public boolean isLaunching() { return status == GameStatus.LAUNCHING && gameLaunchScheduler != null; }
    public boolean isPlaying() {
        return status == GameStatus.PLAYING;
    }
    public boolean isFinish() {
        return status == GameStatus.FINISH;
    }

    public GameController getGameController() {
        return gameController;
    }
    public ConfigController getConfigController() {
        return configController;
    }
    public BoardController getBoardController() {
        return boardController;
    }

    public GameLaunchScheduler getGameLaunchScheduler() {
        return gameLaunchScheduler;
    }
    public void setGameLaunchScheduler(GameLaunchScheduler gameLaunchScheduler) {
        this.gameLaunchScheduler = gameLaunchScheduler;
    }

    public GameCache getGameData() {
        return gameCache;
    }
    public GameBuilders getGameBuilders() {
        return gameBuilders;
    }


}
