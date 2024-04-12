package fr.jielos.buildtionnary.core.cache.player;

import fr.jielos.buildtionnary.Buildtionnary;
import fr.jielos.buildtionnary.utils.time.DateUtils;
import fr.jielos.buildtionnary.core.Game;
import fr.jielos.buildtionnary.core.GameComponent;
import fr.jielos.buildtionnary.core.config.ConfigController;
import fr.jielos.buildtionnary.core.cache.status.GameStatus;
import fr.mrmicky.fastboard.FastBoard;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerBoard extends GameComponent {

    private final Player player;
    private final FastBoard board;
    private final Map<GameStatus, Integer> voidLines;
    public PlayerBoard(Buildtionnary instance, Game game, Player player) {
        super(instance, game);

        this.player = player;
        this.board = new FastBoard(player);
        this.board.updateTitle(game.getConfigController().getString(ConfigController.Value.SCOREBOARD_NAME));
        this.voidLines = new HashMap<>();

        game.getBoardController().getBoards().put(player, this);
    }

    public void update() {
        final GameStatus status = game.getStatus();
        final List<String> lines = new ArrayList<>(Collections.singletonList(""));

        final int playersSize = game.getGameData().getPlayers().size();
        if(game.isWaiting()) {
            lines.addAll(Arrays.asList("§a§lStatut", String.format("§7- §fJoueurs: §a%d", playersSize)));
            lines.add(getVoidLine());

            if(!game.isLaunching()) {
                lines.addAll(Arrays.asList(String.format("§7§oEn attente de§r §e%d §7§oj...", game.getConfigController().getInt(ConfigController.Value.MIN_PLAYERS) - playersSize), "§7§opour démarrer la partie."));
            } else {
                lines.addAll(Arrays.asList("§7La partie va démarrer", String.format("§7dans §r%s §7...", DateUtils.formatSeconds(game.getGameLaunchScheduler().getSeconds(), ChatColor.YELLOW, false))));
            }
        } else {
            final GamePlayer gamePlayer = game.getGameData().getGamePlayer(player);

            if(game.isPlaying()) {
                final GameBuilder gameBuilder = game.getGameBuilders().getGameBuilder();

                lines.add("§b§lPartie");
                lines.add(String.format("§7- §fJoueurs restant: §e%d", playersSize));
                lines.add(String.format("§7- §fConstructeur: §a%s", gameBuilder.getPlayer().getName()));
                lines.add(String.format("§7- §fTemps restant: §r%s", DateUtils.formatSeconds(gameBuilder.getSeconds(), ChatColor.YELLOW, false)));

                lines.add(getVoidLine());

                final List<GamePlayer> sortedGamePlayers = game.getGameData().getSortedGamePlayers();
                final List<GamePlayer> sortedGamePlayersLimited = sortedGamePlayers.stream().limit(3).collect(Collectors.toList());

                lines.add("§d§lClassement");
                for(GamePlayer anGamePlayer : sortedGamePlayersLimited) {
                    final StringJoiner stringJoiner = new StringJoiner(" ");
                    final String chatColor = anGamePlayer == gamePlayer ? ChatColor.BOLD.toString() : "";

                    stringJoiner.add(ChatColor.DARK_GRAY + String.format("#%d", sortedGamePlayers.indexOf(anGamePlayer)+1));
                    stringJoiner.add(ChatColor.WHITE + chatColor + String.format("%s§r:", anGamePlayer.getPlayer().getName()));
                    stringJoiner.add(ChatColor.AQUA + String.format("%d pts", anGamePlayer.getPoints()));

                    lines.add(stringJoiner.toString());
                }

                if(!sortedGamePlayersLimited.contains(gamePlayer)) {
                    lines.add(String.format("§a> #%d §f§l%s§r: §b%d pts", sortedGamePlayers.indexOf(gamePlayer)+1, gamePlayer.getPlayer().getName(), gamePlayer.getPoints()));
                }

                if(sortedGamePlayers.size() > sortedGamePlayersLimited.size()) {
                    lines.add(" §7...");
                }

                if(gamePlayer == gameBuilder.getGamePlayer()) {
                    lines.add(getVoidLine());
                    lines.add("§a§lVous devez construire");
                    lines.add(String.format("§8⋆ §e%s§7.", gameBuilder.getWord()));
                }
            } else if(game.isFinish()) {
                final List<GamePlayer> gameWinners = game.getGameData().getGameWinners();

                lines.add("§a§lPartie terminée");
                lines.addAll(Arrays.asList("§7Félicitations aux gagnants", "§7qui remportent la partie !"));

                if(!gameWinners.isEmpty()) {
                    lines.addAll(Arrays.asList(getVoidLine(), String.format("§b§lGagnants§r §7(§e%d pts§7)", gameWinners.get(0).getPoints())));
                    for(GamePlayer gameWinner : gameWinners) {
                        lines.add(String.format("§8⋆ §7%s", gameWinner.getPlayer().getName()));
                    }
                }

                if(gamePlayer != null) {
                    lines.addAll(Arrays.asList(getVoidLine(), "§d§lVos statistiques", "§7Vous avez obtenu un total de", String.format("§e%d points §7durant cette partie.", gamePlayer.getPoints())));
                }
            }
        }

        if(game.getConfigController().getBoolean(ConfigController.Value.SCOREBOARD_FOOTER_ENABLED)) {
            lines.addAll(Arrays.asList("§k", game.getConfigController().getString(ConfigController.Value.SCOREBOARD_FOOTER_CONTENT)));
        }

        voidLines.clear();
        board.updateLines(lines.toArray(new String[0]));
    }

    public void destroy() {
        board.delete();
        game.getBoardController().removePlayerBoard(player);
    }

    public String getVoidLine() {
        final int repeat = voidLines.getOrDefault(game.getStatus(), 0) + 1;
        voidLines.put(game.getStatus(), repeat);

        return StringUtils.repeat(" ", repeat);
    }

    public Player getPlayer() {
        return player;
    }

    public FastBoard getBoard() {
        return board;
    }
}
