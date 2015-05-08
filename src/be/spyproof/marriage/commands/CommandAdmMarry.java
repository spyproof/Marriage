package be.spyproof.marriage.commands;

import be.spyproof.marriage.Marriage;
import be.spyproof.marriage.annotations.Command;
import be.spyproof.marriage.annotations.Default;
import be.spyproof.marriage.datamanager.PlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 * Created by Nils on 3/04/2015.
 * Command handling inspired by https://github.com/Zenith-
 */
public class CommandAdmMarry
{
    @Command(command = "admmarry", trigger = "reload", args = {}, playersOnly = false, permission = "marriage.admin.reload", desc = "Reload the player config", usage = "/admmarry reload")
    public void forceReload(CommandSender sender)
    {
        PlayerManager.reload();
        sender.sendMessage(ChatColor.DARK_GREEN + "Reloading player config... Loaded " + PlayerManager.getLoadedPlayers().size() + " players");
    }

    @Command(command = "admmarry", trigger = "save", args = {}, playersOnly = false, permission = "marriage.admin.save", desc = "Save the config manually", usage = "/admmarry save")
    public void forceSave(CommandSender sender)
    {
        PlayerManager.saveAllPlayers();
        sender.sendMessage(ChatColor.DARK_GREEN + "Saved " + PlayerManager.getLoadedPlayers().size() + " players");
    }

    @Command(command = "admmarry", trigger = "remove", args = {"{player}"}, playersOnly = false, permission = "marriage.admin.remove", desc = "Reset a player", usage = "/admmarry remove <player>")
    public void removePlayer(CommandSender sender, String player)
    {
        try{
            try {
                String partner = PlayerManager.getPartner(player);

                if(!partner.equals("") && !partner.isEmpty())
                    CommandMarry.divorcePlayer(partner);
            } catch (IllegalArgumentException ignored) {}

            PlayerManager.resetPlayer(player);
            sender.sendMessage(ChatColor.DARK_GREEN + "Removed " + player + "'s data");
        }catch(IllegalArgumentException e){
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
    }

    @Default({"{player}"})
    @Command(command = "admmarry", trigger = "info", args = {"{player}"}, playersOnly = false, permission = "marriage.admin.info", desc = "Get the player information", usage = "/admmarry info <player>")
    public void getPlayerInfo(CommandSender sender, String player)
    {
        String status, gender, partner;
        boolean isHomeSet, trustsPartner;
        int x, y, z;
        try{
            status = PlayerManager.getStatus(player).toString();
            gender = PlayerManager.getGender(player).toString();
            partner = PlayerManager.getPartner(player);
            isHomeSet = PlayerManager.isHomeSet(player);
            trustsPartner = PlayerManager.trustsPartner(player);
            x = PlayerManager.getHomeX(player);
            y = PlayerManager.getHomeY(player);
            z = PlayerManager.getHomeZ(player);

            Marriage.sendMessage(sender, "&e------------&6&l" + player + "&e------------");

            Marriage.sendMessage(sender, "&6Gender: &e" + gender);
            Marriage.sendMessage(sender, "&6Status: &e" + status);
            Marriage.sendMessage(sender, "&6Partner: &e" + partner);
            Marriage.sendMessage(sender, "&6Allow open inv: " + ChatColor.YELLOW + trustsPartner);
            if(isHomeSet)
                Marriage.sendMessage(sender, "&6Home: &eX:" + x + "  Y:" + y + "  Z:" + z);
            else
                Marriage.sendMessage(sender, "&6Home: &eHome is not set");
        }catch (IllegalArgumentException e){
            Marriage.sendMessage(sender, ChatColor.RED + e.getMessage());
        }
    }

    @Command(command = "admmarry", trigger = "{debug}", args = {}, playersOnly = false, helpHidden = true)
    public void getDebug(CommandSender sender)
    {
        //When enabled, show the player debug information
        if (!sender.isOp())
            return;
        Marriage.toggleDebugger(sender.getName());
    }

    @Command(command = "admmarry", trigger = "plugin", args = {}, playersOnly = false, permission = "marriage.admin.plugin", desc = "Get more info about the plugin", usage = "/admmarry plugin")
    public void getPluginInfo(CommandSender sender)
    {
        PluginDescriptionFile description = Marriage.plugin.getDescription();
        Marriage.sendMessage(sender, "&eThe current version of " + Marriage.plugin.getName() + " is &6" + description.getVersion());
        if (description.getAuthors() != null)
            if (description.getAuthors().size() != 0)
            {
                String authors = "&eAuthors:&6";
                for (String s : description.getAuthors())
                    authors += " " + s;
                Marriage.sendMessage(sender, authors);
            }
        if (description.getWebsite() != null)
            Marriage.sendMessage(sender, "&eThe developer's website is &6&n" + description.getWebsite());
    }

    //TODO clear cooldown?

}
