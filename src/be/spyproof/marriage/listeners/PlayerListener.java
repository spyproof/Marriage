package be.spyproof.marriage.listeners;

import java.util.List;

import be.spyproof.marriage.Marriage;
import be.spyproof.marriage.Messages;
import be.spyproof.marriage.Permissions;
import be.spyproof.marriage.Status;

import com.dthielke.herochat.ChannelChatEvent;
import com.earth2me.essentials.Essentials;

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
    	Marriage.plugin.getPlayerManager().addPlayer(e.getPlayer().getName());
        if (Marriage.plugin.getPlayerManager().getStatus(e.getPlayer().getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Player partner = Marriage.plugin.getPlayer(Marriage.plugin.getPlayerManager().getPartner(e.getPlayer().getName()));
            if (partner != null)
                Marriage.plugin.sendMessage(partner, Messages.partnerJoined.replace("{player}", e.getPlayer().getDisplayName()));
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
        Status status = Marriage.plugin.getPlayerManager().getStatus(e.getSender().getName());
        String prefix = null;

        if (status.equals(Status.MARRIED_TO_PERSON))
            prefix = "&d" + Messages.prefixMarried + " &r";
        else if (status.equals(Status.MARRIED_TO_LEFT_HAND))
            prefix = "&d" + Messages.prefixLeftHand + " &r";
        else if (status.equals(Status.MARRIED_TO_RIGHT_HAND))
            prefix = "&d" + Messages.prefixRightHand + " &r";

        if (prefix == null)
            prefix = "";

        e.setFormat(e.getFormat().replace("{marry}", prefix));
    }

    //TODO handle essential & banmanager mutes
	@EventHandler
    public void onNormalChat(AsyncPlayerChatEvent e)
    {
        if (e.isCancelled())
            return;

        if (Marriage.plugin.getPlayerManager().isPartnerChatOn(e.getPlayer().getName()))
        {
            e.setCancelled(true);
            Player partner = Marriage.plugin.getPlayer(Marriage.plugin.getPlayerManager().getPartner(e.getPlayer().getName()));
            if (partner == null)
            {
            	Marriage.plugin.sendMessage(e.getPlayer(), Messages.notOnline.replace("{player}", Marriage.plugin.getPlayerManager().getPartner(e.getPlayer().getName())));
            	Marriage.plugin.sendMessage(e.getPlayer(), "&cReturning back to normal chat");
                Marriage.plugin.getPlayerManager().setPartnerChat(e.getPlayer().getName(), false);
            }else
            {
                String message = Messages.privateMessage.replace("{sender}", e.getPlayer().getDisplayName())
                        .replace("{receiver}", partner.getDisplayName()).replace("{message}", ChatColor.stripColor(e.getMessage()));
                message = message.replaceAll("&[klmno]", "");
                Marriage.plugin.sendMessage(e.getPlayer(), message);
                Marriage.plugin.sendMessage(partner, message);

                //Show the message on console
                message = Messages.privateMessageSocialspy.replace("{sender}", e.getPlayer().getDisplayName())
                        .replace("{receiver}", partner.getDisplayName()).replace("{message}", ChatColor.stripColor(e.getMessage()));
                Marriage.plugin.sendMessage(Marriage.plugin.getServer().getConsoleSender(), message);

                //If you have socialspy enabled in essentials
                Essentials essentials = (Essentials) Marriage.plugin.getServer().getPluginManager().getPlugin("Essentials");
                if (essentials != null)
                    if (essentials.isEnabled())
                    {
                        List<Player> players = Marriage.plugin.getOnlinePlayers();
                        for (Player p : players)
                            if (essentials.getUser(p).isSocialSpyEnabled())
                            	Marriage.plugin.sendMessage(p, message);
                        return;
                    }

                //If essentials is not enabled, check for my own permission
                List<Player> players = Marriage.plugin.getOnlinePlayers();
                for (Player p : players)
                    if (Permissions.hasPerm(p, Permissions.adminSocialSpy))
                    	Marriage.plugin.sendMessage(p, message);
            }
        }
    }

    private void playerDisconnected(Player player)
    {
        if (Marriage.plugin.getPlayerManager().getStatus(player.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Player partner = Marriage.plugin.getPlayer(Marriage.plugin.getPlayerManager().getPartner(player.getName()));
            if (partner != null)
                Marriage.plugin.sendMessage(partner, Messages.partnerDC.replace("{player}", player.getDisplayName()));
        }

        Marriage.plugin.getPlayerManager().setLastOnline(player.getName().toLowerCase(), System.currentTimeMillis());
    	Marriage.plugin.getPlayerManager().unloadPlayer(player.getName());
    }
}