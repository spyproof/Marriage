package be.spyproof.marriage.listeners;

import java.util.List;

import be.spyproof.marriage.Marriage;
import be.spyproof.marriage.handlers.Messages;
import be.spyproof.marriage.handlers.Permissions;
import be.spyproof.marriage.Status;

import com.avaje.ebeaninternal.server.cluster.mcast.Message;
import com.dthielke.herochat.ChannelChatEvent;
import com.earth2me.essentials.Essentials;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.BmAPI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
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
            if (Permissions.hasMoney(e.getPlayer(), "unlock.perk.login-message"))
            {
                Player partner = Marriage.plugin.getPlayer(Marriage.plugin.getPlayerManager().getPartner(e.getPlayer().getName()));

                if (partner != null)
                    Messages.sendMessage(partner, Messages.partnerJoined.replace("{player}", e.getPlayer().getDisplayName()));
            }
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
        //Handle the prefix
        if (!Permissions.hasMoney(e.getSender().getPlayer(), "unlock.perk.prefix"))
        {
            e.setFormat(e.getFormat().replace("{marry}", ""));
            return;
        }

        Status status = Marriage.plugin.getPlayerManager().getStatus(e.getSender().getName());
        String prefix = null;

        if (status.equals(Status.MARRIED_TO_PERSON))
            prefix = "&r" + Messages.prefixMarried + " &r";
        else if (status.equals(Status.MARRIED_TO_LEFT_HAND))
            prefix = "&r" + Messages.prefixLeftHand + " &r";
        else if (status.equals(Status.MARRIED_TO_RIGHT_HAND))
            prefix = "&r" + Messages.prefixRightHand + " &r";

        if (prefix == null)
            prefix = "";

        e.setFormat(e.getFormat().replace("{marry}", prefix));
    }

	@EventHandler
    public void onNormalChat(AsyncPlayerChatEvent e)
    {
        if (e.isCancelled())
            return;

        if (Marriage.plugin.getPlayerManager().isPartnerChatOn(e.getPlayer().getName()))
        {
            e.setCancelled(true);

            //Check if muted
            if (Marriage.plugin.isPluginEnabled(Messages.essentialsPluginName))
            {
                Essentials essentials = (Essentials) Marriage.plugin.getServer().getPluginManager().getPlugin(Messages.essentialsPluginName);
                if (essentials.getUser(e.getPlayer()).isMuted())
                {
                    Messages.sendMessage(e.getPlayer(), Messages.muted);
                    return;
                }
            }

            if (Marriage.plugin.isPluginEnabled(Messages.banManagerPluginName))
            {
                if (BmAPI.isMuted(e.getPlayer()))
                {
                    Messages.sendMessage(e.getPlayer(), Messages.muted);
                    return;
                }
            }

            //Handle private chat
            Player partner = Marriage.plugin.getPlayer(Marriage.plugin.getPlayerManager().getPartner(e.getPlayer()
                                                                                                             .getName()));
            if (partner == null)
            {
            	Messages.sendMessage(e.getPlayer(), Messages.notOnline.replace("{player}", Marriage.plugin.getPlayerManager().getPartner(e.getPlayer().getName())));
            	Messages.sendMessage(e.getPlayer(), "&cReturning back to normal chat");
                Marriage.plugin.getPlayerManager().setPartnerChat(e.getPlayer().getName(), false);
            }else
            {
                String message = Messages.privateMessage.replace("{sender}", e.getPlayer().getDisplayName())
                        .replace("{receiver}", partner.getDisplayName()).replace("{message}", ChatColor.stripColor(e.getMessage()));
                message = message.replaceAll("&[klmno]", "");
                Messages.sendMessage(e.getPlayer(), message);
                Messages.sendMessage(partner, message);

                //Show the message on console
                message = Messages.privateMessageSocialspy.replace("{sender}", e.getPlayer().getDisplayName())
                        .replace("{receiver}", partner.getDisplayName()).replace("{message}", ChatColor.stripColor(e.getMessage()));
                Messages.sendMessage(Marriage.plugin.getServer().getConsoleSender(), message);

                //If you have socialspy enabled in essentials
                Essentials essentials = (Essentials) Marriage.plugin.getServer().getPluginManager().getPlugin("Essentials");
                if (essentials != null)
                    if (essentials.isEnabled())
                    {
                        List<Player> players = Marriage.plugin.getOnlinePlayers();
                        for (Player p : players)
                            if (essentials.getUser(p).isSocialSpyEnabled())
                            	Messages.sendMessage(p, message);
                        return;
                    }

                //If essentials is not enabled, check for my own permission
                List<Player> players = Marriage.plugin.getOnlinePlayers();
                for (Player p : players)
                    if (Permissions.hasPerm(p, Permissions.adminSocialSpy))
                    	Messages.sendMessage(p, message);
            }
        }
    }

    @EventHandler
    public void onPlayerDead(PlayerDeathEvent e)
    {
        if (Marriage.config.getBoolean("smite-on-partner-dead"))
        {
            if (Marriage.plugin.getPlayerManager().getStatus(e.getEntity().getName()).equals(Status.MARRIED_TO_PERSON))
            {
                String partnerName = Marriage.plugin.getPlayerManager().getPartner(e.getEntity().getName());
                Player p = Marriage.plugin.getPlayer(partnerName);

                if (Permissions.hasMoney(e.getEntity(), "unlock.perk.no-smite-on-partner-dead"))
                    return;

                if (p == null){
                    Messages.sendDebugInfo("p == null");
                    return;
                }

                Location loc = p.getLocation();
                loc.getWorld().strikeLightningEffect(loc);
            }
        }
    }

    private void playerDisconnected(Player player)
    {
        if (Marriage.plugin.getPlayerManager().getStatus(player.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Player partner = Marriage.plugin.getPlayer(Marriage.plugin.getPlayerManager().getPartner(player.getName()));
            if (partner != null)
                Messages.sendMessage(partner, Messages.partnerDC.replace("{player}", player.getDisplayName()));
        }

        Marriage.plugin.getPlayerManager().setLastOnline(player.getName().toLowerCase(), System.currentTimeMillis());
    	Marriage.plugin.getPlayerManager().unloadPlayer(player.getName());
    }
}