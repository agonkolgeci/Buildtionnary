package fr.jielos.buildtionnary.components.display;

import fr.jielos.buildtionnary.components.packets.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;

import java.lang.reflect.*;

public class ActionBar implements Reflection {

    private final GetActionBar actionBar;

    private final int MAJOR_VERSION =
            Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]);

    public ActionBar() {
        actionBar = oldActionBar();
    }

    public interface GetActionBar {
        void send(Player player, String message);
    }

    public GetActionBar getMethod() { return actionBar; }

    private GetActionBar oldActionBar() {
        return (player, message) -> {
            try {
                Constructor<?> constructor = getNMSClass("PacketPlayOutChat").getConstructor(getNMSClass("IChatBaseComponent"), byte.class);
                Object icbc = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + message + "\"}");
                Object packet = constructor.newInstance(icbc, (byte) 2);
                Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
                Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
                playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
}