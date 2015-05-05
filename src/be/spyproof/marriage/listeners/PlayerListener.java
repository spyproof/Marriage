package be.spyproof.marriage.listeners;

import be.spyproof.marriage.datamanager.PlayerManager;
import com.dthielke.herochat.ChannelChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Created by Spyproof on 3/04/2015.
 */
public class PlayerListener implements Listener
{
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        PlayerManager.loadPlayer(e.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e)
    {
        PlayerManager.unloadPlayer(e.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent e)
    {
        PlayerManager.unloadPlayer(e.getPlayer().getName());
    }

    @EventHandler
    public void onHeroChat(ChannelChatEvent e)
    {
        String prefix = PlayerManager.getPrefix(e.getSender().getName());
        if (prefix == null)
            prefix = "";

        e.setFormat(e.getFormat().replace("{marry}", prefix));
    }
}
