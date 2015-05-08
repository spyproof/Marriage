package be.spyproof.marriage.listeners;

import be.spyproof.marriage.Marriage;
import be.spyproof.marriage.Messages;
import be.spyproof.marriage.Status;
import be.spyproof.marriage.datamanager.PlayerManager;
import com.dthielke.herochat.ChannelChatEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;

/**
 * Created by Spyproof on 3/04/2015.
 */
public class PlayerListener implements Listener
{
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        PlayerManager.loadPlayer(e.getPlayer().getName());
        if (PlayerManager.getStatus(e.getPlayer().getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Player partner = Marriage.getPlayer(PlayerManager.getPartner(e.getPlayer().getName()));
            if (partner != null)
                Marriage.sendMessage(partner, Messages.partnerJoined.replace("{player}", e.getPlayer().getDisplayName()));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e)
    {
        playerDisconnected(e.getPlayer());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent e)
    {
        if (e.isCancelled())
            return;
        playerDisconnected(e.getPlayer());
    }

    @EventHandler
    public void onHeroChat(ChannelChatEvent e)
    {
        String prefix = PlayerManager.getPrefix(e.getSender().getName());
        if (prefix == null)
            prefix = "";

        e.setFormat(e.getFormat().replace("{marry}", prefix));
    }

    @EventHandler
    public void onNormalChat(AsyncPlayerChatEvent e) //TODO socialspy
    {
        if (e.isCancelled())
            return;

        if (PlayerManager.isPartnerChatOn(e.getPlayer().getName()))
        {
            e.setCancelled(true);
            Player partner = Marriage.getPlayer(PlayerManager.getPartner(e.getPlayer().getName()));
            if (partner == null)
            {
                Marriage.sendMessage(e.getPlayer(), Messages.notOnline.replace("{player}", PlayerManager.getPartner(e.getPlayer().getName())));
                Marriage.sendMessage(e.getPlayer(), "&cReturning back to normal chat");
                PlayerManager.setPartnerChat(e.getPlayer().getName(), false);
            }else
            {
                String message = Messages.privateMessage.replace("{sender}", e.getPlayer().getDisplayName())
                        .replace("{receiver}", partner.getDisplayName()).replace("{message}", ChatColor.stripColor(e.getMessage()));
                Marriage.sendMessage(e.getPlayer(), message);
                Marriage.sendMessage(partner, message);
            }
        }
    }

    private void playerDisconnected(Player player)
    {
        PlayerManager.unloadPlayer(player.getName());

        if (PlayerManager.getStatus(player.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Player partner = Marriage.getPlayer(PlayerManager.getPartner(player.getName()));
            if (partner != null)
                Marriage.sendMessage(partner, Messages.partnerDC.replace("{player}", player.getDisplayName()));
        }

        PlayerManager.setLastOnline(player.getName(), System.currentTimeMillis());
    }
}
