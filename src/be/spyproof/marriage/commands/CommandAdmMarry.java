package be.spyproof.marriage.commands;

import be.spyproof.marriage.commands.handlers.Command;
import be.spyproof.marriage.datamanager.PlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

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

    @Command(command = "admmarry", trigger = "remove", args = {"player"}, playersOnly = false, permission = "marriage.admin.remove", desc = "Reset a player", usage = "/admmarry remove <player>")
    public void removePlayer(CommandSender sender, String player)
    {
        try{
            try {
                String partner = PlayerManager.getPartner(player);

                /*TODO if(!partner.equals("") && !partner.isEmpty())
                    this.plugin.getCommandDivorce().divorcePlayer(partner);*/
            } catch (IllegalArgumentException ignored) {}

            PlayerManager.resetPlayer(player);
            sender.sendMessage(ChatColor.DARK_GREEN + "Removed " + player + "'s data");
        }catch(IllegalArgumentException e){
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
    }

    @Command(command = "admmarry", trigger = "info", args = {"player"}, playersOnly = false, permission = "marriage.admin.info", desc = "Get the player information", usage = "/admmarry info <player>")
    public void getPlayerInfo(CommandSender sender, String player)
    {
        String status, gender, partner;
        boolean isHomeSet, trustsPartner;
        int x, y, z;
        try{
            status = PlayerManager.getStatus(player).toString();
            gender = PlayerManager.getGender(player).toString();
            partner = PlayerManager.getPartner(player);
            //isHomeSet = this.plugin.getPlayerManager().getIsHomeSet(player);
            trustsPartner = PlayerManager.trustsPartner(player);
            //x = this.plugin.getPlayerManager().getHomeX(player);
            //y = this.plugin.getPlayerManager().getHomeY(player);
            //z = this.plugin.getPlayerManager().getHomeZ(player);

            sender.sendMessage(ChatColor.YELLOW + "------------" + ChatColor.GOLD + ChatColor.BOLD + player + "------------");

            sender.sendMessage(ChatColor.GOLD + "Gender: " + ChatColor.YELLOW + gender);
            sender.sendMessage(ChatColor.GOLD + "Status: " + ChatColor.YELLOW + status);
            sender.sendMessage(ChatColor.GOLD + "Partner: " + ChatColor.YELLOW + partner);
            sender.sendMessage(ChatColor.GOLD + "Allow open inv: " + ChatColor.YELLOW + trustsPartner);
                        /*if(isHomeSet)
                            sender.sendMessage(ChatColor.GOLD + "Home: " + ChatColor.YELLOW + "X:" + x + " | Y:" + y + " | Z:" + z);
                        else
                            sender.sendMessage(ChatColor.GOLD + "Home: " + ChatColor.YELLOW + "Home is not set");*/
        }catch (IllegalArgumentException e){
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
    }

    @Command(command = "admmarry", trigger = "{debug}", args = {}, playersOnly = false, helpHidden = true)
    public void getDebug(CommandSender sender)
    {
        if (!sender.isOp())
            return;
        sender.sendMessage(ChatColor.DARK_GREEN + "Loaded players: ");
        for (int i = 0; i < PlayerManager.getLoadedPlayers().size(); i++)
            sender.sendMessage(ChatColor.YELLOW + " - " + PlayerManager.getLoadedPlayers().get(i).getName());

    }

    //TODO clear cooldown?
}
