package fr.jielos.buildtionnary.game;

import fr.jielos.buildtionnary.Buildtionnary;
import fr.jielos.buildtionnary.PluginComponent;
import fr.jielos.buildtionnary.game.controllers.BoardController;
import fr.jielos.buildtionnary.game.controllers.ConfigController;
import fr.jielos.buildtionnary.game.controllers.GameController;
import fr.jielos.buildtionnary.game.data.GameBuilders;
import fr.jielos.buildtionnary.game.data.GameData;
import fr.jielos.buildtionnary.game.data.players.GamePlayer;
import fr.jielos.buildtionnary.game.references.Status;
import fr.jielos.buildtionnary.game.schedulers.GameLaunchScheduler;
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

    private final GameData gameData;
    private final GameBuilders gameBuilders;

    private GameLaunchScheduler gameLaunchScheduler;

    private Status status;

    public Game(Buildtionnary instance) {
        super(instance);

        this.gameController = new GameController(instance, this);
        this.configController = new ConfigController(instance, this);
        this.boardController = new BoardController(instance, this);

        this.gameData = new GameData(instance, this);
        this.gameBuilders = new GameBuilders(instance, this);
    }

    public void initGame() {
        setStatus(Status.WAITING_FOR_PLAYERS);

        gameController.loadOnlinePlayers();
    }

    public boolean canLaunch() {
        final int minPlayers = configController.getInt(ConfigController.Value.MIN_PLAYERS);
        final int playersSize = gameData.getPlayers().size();
        return playersSize >= minPlayers && isWaiting();
    }
    public void checkLaunch() {
        if(canLaunch() && !isLaunching()) launchGame();
    }
    public void launchGame() {
        if(isWaiting()) {
            setStatus(Status.LAUNCHING);

            gameLaunchScheduler = new GameLaunchScheduler(instance, this);
            gameLaunchScheduler.runTaskTimer(instance, 0, 20);
        }
    }
    public void cancelLaunch() {
        if(isWaiting() && gameLaunchScheduler != null) {
            gameLaunchScheduler.stop();

            setStatus(Status.WAITING_FOR_PLAYERS);

            instance.getServer().broadcastMessage("§cImpossible de démarrer la partie, visiblement il n'y a plus assez de joueurs.");

            for(Player player : instance.getServer().getOnlinePlayers()) {
                boardController.updatePlayerBoard(player);
            }
        }
    }

    public void startGame() {
        setStatus(Status.PLAYING);

        instance.getServer().broadcastMessage("§eLancement de la partie en cours, téléportation des joueurs en cours...");

        for(Player player : gameData.getPlayers()) {
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(configController.getLocation(ConfigController.Value.WAITING_ROOM));

            gameData.addGamePlayer(player);
        }

        gameBuilders.nextBuilder();
    }

    public boolean canFinish() {
        return gameBuilders.getRemainingBuilders().isEmpty() || gameData.getGamePlayers().size() <= 1;
    }
    public boolean checkFinish() {
        if(canFinish()) {
            finishGame();
            return true;
        }

        return false;
    }
    public void finishGame() {
        setStatus(Status.FINISH);

        instance.getServer().getScheduler().getPendingTasks().forEach(BukkitTask::cancel);

        final List<GamePlayer> gameWinners = gameData.setGameWinners(gameData.getSortedGamePlayers().stream().collect(Collectors.groupingBy(GamePlayer::getPoints)).entrySet().stream().max(Map.Entry.comparingByKey()).get().getValue());
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
        }.runTaskLater(instance, configController.getInt(ConfigController.Value.TIMER_FINISH) * 20);
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    public Status getStatus() {
        return status;
    }

    public boolean isWaiting() {
        return status == Status.WAITING_FOR_PLAYERS || status == Status.LAUNCHING;
    }
    public boolean isLaunching() { return status == Status.LAUNCHING; }
    public boolean isPlaying() {
        return status == Status.PLAYING;
    }
    public boolean isFinish() {
        return status == Status.FINISH;
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

    public GameData getGameData() {
        return gameData;
    }
    public GameBuilders getGameBuilders() {
        return gameBuilders;
    }


}
