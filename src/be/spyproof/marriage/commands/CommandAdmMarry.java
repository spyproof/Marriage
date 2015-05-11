package be.spyproof.marriage.commands;

import be.spyproof.marriage.Marriage;
import be.spyproof.marriage.Messages;
import be.spyproof.marriage.Permissions;
import be.spyproof.marriage.annotations.Command;
import be.spyproof.marriage.annotations.Default;
import be.spyproof.marriage.datamanager.PlayerManager;

import com.earth2me.essentials.Essentials;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 * Created by Nils on 3/04/2015.
 * Command handling inspired by https://github.com/Zenith-
 */
public class CommandAdmMarry
{
	private PlayerManager playerManager;

    public CommandAdmMarry()
    {
    	this.playerManager = Marriage.plugin.getPlayerManager();
    }
    
    @Command(command = "admmarry", trigger = "reload", args = {}, playersOnly = false, permission = Permissions.adminReload, desc = "Reload the player config", usage = "/admmarry reload")
    public void forceReload(CommandSender sender)
    {
        playerManager.reload();
        sender.sendMessage(ChatColor.DARK_GREEN + "Reloading player config... Loaded " + playerManager.getLoadedPlayers().size() + " players");
    }

    @Command(command = "admmarry", trigger = "save", args = {}, playersOnly = false, permission = Permissions.adminSave, desc = "Save the config manually", usage = "/admmarry save")
    public void forceSave(CommandSender sender)
    {
        playerManager.saveAllPlayers();
        sender.sendMessage(ChatColor.DARK_GREEN + "Saved " + playerManager.getLoadedPlayers().size() + " players");
    }

    @Command(command = "admmarry", trigger = "remove", args = {"{player}"}, playersOnly = false, permission = Permissions.adminRemove, desc = "Reset a player", usage = "/admmarry remove <player>")
    public void removePlayer(CommandSender sender, String player)
    {
        try{
            try {
                String partner = playerManager.getPartner(player);

                if(partner != null && !partner.equals("") && !partner.isEmpty())
                    CommandMarry.divorcePlayer(partner);
            } catch (IllegalArgumentException ignored) {}

            playerManager.resetPlayer(player);
            sender.sendMessage(ChatColor.DARK_GREEN + "Removed " + player + "'s data");
        }catch(IllegalArgumentException e){
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
    }

    @Default({"{player}"})
    @Command(command = "admmarry", trigger = "info", args = {"{player}"}, playersOnly = false, permission = Permissions.adminInfo, desc = "Get the player information", usage = "/admmarry info <player>")
    public void getPlayerInfo(CommandSender sender, String player)
    {
        String status, gender, partner;
        boolean isHomeSet, trustsPartner;
        int x, y, z;

    	if (playerManager.getStatus(player) == null)
    	{   
    		Marriage.plugin.sendMessage(sender, Messages.notOnline);
    		return;
    	}
        
        try{
            status = playerManager.getStatus(player).toString();
            gender = playerManager.getGender(player).toString();
            partner = playerManager.getPartner(player);
            isHomeSet = playerManager.isHomeSet(player);
            trustsPartner = playerManager.trustsPartner(player);
            x = playerManager.getHomeX(player);
            y = playerManager.getHomeY(player);
            z = playerManager.getHomeZ(player);

            Marriage.plugin.sendMessage(sender, "&e------------&6&l" + player + "&e------------");

            Marriage.plugin.sendMessage(sender, "&6Gender: &e" + gender);
            Marriage.plugin.sendMessage(sender, "&6Status: &e" + status);
            Marriage.plugin.sendMessage(sender, "&6Partner: &e" + partner);
            Marriage.plugin.sendMessage(sender, "&6Allow open inv: " + ChatColor.YELLOW + trustsPartner);
            if(isHomeSet)
                Marriage.plugin.sendMessage(sender, "&6Home: &eX:" + x + "  Y:" + y + "  Z:" + z);
            else
                Marriage.plugin.sendMessage(sender, "&6Home: &eHome is not set");
        }catch (IllegalArgumentException e){
            Marriage.plugin.sendMessage(sender, ChatColor.RED + e.getMessage());
        }
    }

    @Command(command = "admmarry", trigger = "{debug}", args = {}, playersOnly = false, helpHidden = true)
    public void getDebug(CommandSender sender)
    {
        //When enabled, show the player debug information
        if (!sender.isOp())
            return;
        Marriage.plugin.toggleDebugger(sender.getName());
    }

    @Command(command = "admmarry", trigger = "plugin", args = {}, playersOnly = false, permission = Permissions.adminPlugin, desc = "Get more info about the plugin", usage = "/admmarry plugin")
    public void getPluginInfo(CommandSender sender)
    {
        PluginDescriptionFile description = Marriage.plugin.getDescription();
        Marriage.plugin.sendMessage(sender, "&eThe current version of " + Marriage.plugin.getName() + " is &6" + description.getVersion());
        if (description.getAuthors() != null)
            if (description.getAuthors().size() != 0)
            {
                String authors = "&eAuthors:&6";
                for (String s : description.getAuthors())
                    authors += " " + s;
                Marriage.plugin.sendMessage(sender, authors);
            }
        if (description.getWebsite() != null)
            Marriage.plugin.sendMessage(sender, "&eThe developer's website is &6&n" + description.getWebsite());
    }
    
    @Command(command = "admmarry", trigger = "socialspy", args = {}, playersOnly = false, permission = Permissions.adminSocialSpy, desc = "See the partner chat", usage = "/admmarry socialspy")
    public void socialspy(CommandSender sender)
    {
    	Essentials essentials = (Essentials) Marriage.plugin.getServer().getPluginManager().getPlugin("Essentials");
    	if (essentials != null)
    		if (essentials.isEnabled())
    		{
    			Marriage.plugin.sendMessage(sender, "&eThis plugin is hooked into the socialspy of essentials");
    			return;
    		}
     
    	Marriage.plugin.sendMessage(sender, "&eSocial spy is enabled for you &o" + Permissions.adminSocialSpy);
    }
    
    //TODO clear cooldown?
}
