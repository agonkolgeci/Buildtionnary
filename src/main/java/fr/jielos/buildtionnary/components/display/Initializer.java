package fr.jielos.buildtionnary.components.display;

import org.bukkit.entity.Player;

import java.util.Collection;

public class Initializer {

    private final ActionBar actionBar;
    private final Title titleMain;

    public Initializer() {
        actionBar = new ActionBar();
        titleMain = new Title();
    }

    public void sendActionBar(Player player, String message) {
        actionBar.getMethod().send(player, message);
    }
    public void sendActionBar(Collection<? extends Player> players, String message) {
        final ActionBar.GetActionBar getActionBar = actionBar.getMethod();
        players.forEach(player -> getActionBar.send(player, message));
    }

    public void sendTitle(Player player, String title, String subtitle, int in, int stay, int out) {
        titleMain.getMethod().send(player, title, subtitle != null ? subtitle : "", in, stay, out);
    }
    public void sendTitle(Collection<? extends Player> players, String title, String subtitle, int in, int stay, int out) {
        final Title.GetTitle getTitle = titleMain.getMethod();
        players.forEach(player -> getTitle.send(player, title, subtitle != null ? subtitle : "", in, stay, out));
    }
}
