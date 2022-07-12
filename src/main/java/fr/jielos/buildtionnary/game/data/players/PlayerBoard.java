package fr.jielos.buildtionnary.game.data.players;

import fr.jielos.buildtionnary.Buildtionnary;
import fr.jielos.buildtionnary.components.display.Time;
import fr.jielos.buildtionnary.game.Game;
import fr.jielos.buildtionnary.game.GameComponent;
import fr.jielos.buildtionnary.game.controllers.ConfigController;
import fr.minuskube.netherboard.Netherboard;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerBoard extends GameComponent {

    private final Player player;
    private final BPlayerBoard board;
    public PlayerBoard(Buildtionnary instance, Game game, Player player) {
        super(instance, game);

        this.player = player;

        this.board = Netherboard.instance().createBoard(player, game.getConfigController().getString(ConfigController.Value.SCOREBOARD_NAME));

        game.getBoardController().getBoards().put(player, this);
    }

    public void update() {
        final List<String> lines = new ArrayList<>(Collections.singletonList(""));

        final int playersSize = game.getGameData().getPlayers().size();
        if(game.isWaiting()) {

            lines.addAll(Arrays.asList("§a§lStatut", String.format("§7- §fJoueurs: §a%d", playersSize)));
            lines.add(" ");

            if(!game.isLaunching()) {
                lines.addAll(Arrays.asList(String.format("§7§oEn attente de§r §e%d §7§oj...", game.getConfigController().getInt(ConfigController.Value.MIN_PLAYERS) - playersSize), "§7§opour démarrer la partie."));
            } else {
                lines.addAll(Arrays.asList("§7La partie va démarrer", String.format("§7dans §r%s §7...", Time.formatSeconds(game.getGameLaunchScheduler().getSeconds(), ChatColor.YELLOW, false))));
            }
        } else if(game.isPlaying()) {
            final GameBuilder gameBuilder = game.getGameBuilders().getGameBuilder();

            lines.add("§b§lPartie");
            lines.add(String.format("§7- §fJoueurs restant: §e%d", playersSize));
            lines.add(String.format("§7- §fConstructeur: §a%s", gameBuilder.getPlayer().getName()));
            lines.add(String.format("§7- §fTemps restant: §r%s", Time.formatSeconds(gameBuilder.getSeconds(), ChatColor.YELLOW, false)));

            if(game.getGameController().isPlayer(player)) {
                final GamePlayer gamePlayer = game.getGameData().getGamePlayer(player);

                lines.add(" ");
                if(gameBuilder.getGamePlayer() != gamePlayer) {
                    lines.add("§d§lVos statistiques");
                    lines.add(String.format("§7- §fVotre position: §6#%d", game.getGameData().getSortedGamePlayers().indexOf(gamePlayer) + 1));
                    lines.add(String.format("§7- §fVos points: §b%d pts", gamePlayer.getPoints()));
                } else {
                    lines.add("§a§lÀ vos pinceaux");
                    lines.addAll(Arrays.asList("§7Vous devez faire deviner", "§7à un maximum de personnes", String.format("§7le mot §e%s§7.", gameBuilder.getWord())));
                }
            }
        } else if(game.isFinish()) {
            final List<GamePlayer> gameWinners = game.getGameData().getGameWinners();

            lines.add("§a§lPartie terminée");
            lines.addAll(Arrays.asList("§7Félicitations aux gagnants", "§7qui remportent la partie !"));

            if(!gameWinners.isEmpty()) {
                lines.addAll(Arrays.asList(" ", String.format("§b§lGagnants§r §7(§e%d pts§7)", gameWinners.get(0).getPoints())));
                for(GamePlayer gameWinner : gameWinners) {
                    lines.add(String.format("§8⋆ §7%s", gameWinner.getPlayer().getName()));
                }
            }

            if(game.getGameController().isPlayer(player)) {
                final GamePlayer gamePlayer = game.getGameData().getGamePlayer(player);
                lines.addAll(Arrays.asList("  ", "§d§lVos statistiques", "§7Vous avez obtenu un total de", String.format("§e%d points §7durant cette partie.", gamePlayer.getPoints())));
            }
        }

        if(game.getConfigController().getBoolean(ConfigController.Value.SCOREBOARD_FOOTER_ENABLED)) {
            lines.addAll(Arrays.asList("§k", game.getConfigController().getString(ConfigController.Value.SCOREBOARD_FOOTER_CONTENT)));
        }

        board.setAll(lines.toArray(new String[0]));
    }

    public void destroy() {
        board.delete();
        game.getBoardController().removePlayerBoard(player);
    }

    public Player getPlayer() {
        return player;
    }

    public BPlayerBoard getBoard() {
        return board;
    }
}
