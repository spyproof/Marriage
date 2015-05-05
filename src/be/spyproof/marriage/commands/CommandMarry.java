package be.spyproof.marriage.commands;

import be.spyproof.marriage.Marriage;
import be.spyproof.marriage.Status;
import be.spyproof.marriage.commands.handlers.Command;
import be.spyproof.marriage.datamanager.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nils on 2/04/2015.
 * Command handling inspired by https://github.com/Zenith-
 */
@SuppressWarnings("deprecated")
public class CommandMarry
{
    private Map<String, String> offers;

    public CommandMarry()
    {
        this.offers = new HashMap<String, String>();
    }

    @Command(command = "marry", trigger = "deny", args = {}, playersOnly = true, permission = "marriage.player.marry.other", desc = "Deny the marriage reuqest", usage = "/marry deny")
    public void DenyMarriage(CommandSender sender)
    {
        try{
            Player sadPlayer = Bukkit.getServer().getPlayer(this.offers.get(sender.getName())); //sadPartner = player who did /marry <player>

            Marriage.sendMessage(sender, ChatColor.RED + "You rejected " + sadPlayer.getName() + "'s marriage request");
            Marriage.sendMessage(sadPlayer, ChatColor.RED + sender.getName() + " rejected your marriage request ☹");

            this.offers.remove(sender.getName());

        }catch (IllegalArgumentException e){
            Marriage.sendMessage(sender, Marriage.getConfigs().getString("message.no-proposal"));
        }
    }

    @Command(command = "marry", trigger = "accept", args = {}, playersOnly = true, permission = "marriage.player.marry.other", desc = "Accept the marriage request", usage = "/marry accept")
    public void AcceptMarriage(CommandSender sender)
    {

        if (PlayerManager.isMarried(sender.getName()))
        {
            Marriage.sendMessage(sender, Marriage.getConfigs().getString("message.already-married"));
            return;
        }

        String newPartner = this.offers.get(sender.getName()); //newPartner = player who did /marry <player>

        if (PlayerManager.isMarried(newPartner))
        {
            Marriage.sendMessage(sender, Marriage.getConfigs().getString("message.already-married-other").replace("{player}",
                                                                                                         newPartner));
            return;
        }

        try{
            //TODO cooldowns
            String broadcast = Marriage.getConfigs().getString("message.broadcast").replace("{prefix}", Marriage.getConfigs().getString("message.prefix"))
                    .replace("{player1}", sender.getName()).replace("{player2}", newPartner);
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcast));
            this.offers.remove(sender.getName());
        }catch (IllegalArgumentException e){
            Marriage.sendMessage(sender, Marriage.getConfigs().getString("message.no-proposal"));
        }
    }

    @Command(command = "marry", trigger = "{left}", args = {}, playersOnly = true, permission = "marriage.player.marry.self", helpHidden = true)
    public void MarryLeftHand(CommandSender sender)
    {
        if (PlayerManager.isMarried(sender.getName()))
        {
            Marriage.sendMessage(sender, Marriage.getConfigs().getString("message.already-married"));
            return;
        }

        PlayerManager.setStatus(sender.getName(), Status.MARRIED_TO_LEFT_HAND);

        String broadcast = Marriage.getConfigs().getString("message.broadcast").replace("{prefix}", Marriage
                .getConfigs().getString("message.prefix")).replace("{player1}", sender.getName()).replace("{player2}", "its left hand");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcast));
    }

    @Command(command = "marry", trigger = "{right}", args = {}, playersOnly = true, permission = "marriage.player.marry.self", helpHidden = true)
    public void MarryRightHand(CommandSender sender)
    {
        if (PlayerManager.isMarried(sender.getName()))
        {
            Marriage.sendMessage(sender, Marriage.getConfigs().getString("message.already-married"));
            return;
        }

        PlayerManager.setStatus(sender.getName(), Status.MARRIED_TO_RIGHT_HAND);

        String broadcast = Marriage.getConfigs().getString("message.broadcast").replace("{prefix}", Marriage
                .getConfigs().getString("message.prefix")).replace("{player1}", sender.getName()).replace("{player2}", "its right hand");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcast));

    }

    @Command(command = "marry", trigger = "player", args = {"{player}"}, playersOnly = true, permission = "marriage.player.marry.other", desc = "Send a marriage request to a player", usage = "/marry player <name>")
    public void sendMarriageRequestPlayer(CommandSender sender, String receiverName)
    {
        if (PlayerManager.isMarried(sender.getName()))
        {
            Marriage.sendMessage(sender, Marriage.getConfigs().getString("message.already-married"));
            return;
        }

        Player receiver = Bukkit.getServer().getPlayer(receiverName);
        if (receiver == null)
        {
            Marriage.sendMessage(sender, ChatColor.translateAlternateColorCodes('&', Marriage.getConfigs().getString
                    ("message.already-married").replace("{player}", receiverName)));
            return;
        }

        //TODO cooldowns

        // When you marry yourself, marry your left or right hand
        if (sender.getName().equals(receiver.getName()))
        {
            Marriage.sendMessage(sender, ChatColor.LIGHT_PURPLE + "Use " + ChatColor.DARK_PURPLE + "/marry left" +
                    ChatColor.LIGHT_PURPLE + " or " + ChatColor.DARK_PURPLE + "/marry right" + ChatColor.LIGHT_PURPLE
                    + " to marry yourself");
        }else{
            Marriage.sendMessage(sender, ChatColor.DARK_PURPLE + "You send a marriage request to " + receiverName);

            Marriage.sendMessage(receiver, ChatColor.LIGHT_PURPLE + sender.getName() + " wants to marry you!");
            Marriage.sendMessage(receiver, ChatColor.LIGHT_PURPLE + "Use " + ChatColor.DARK_PURPLE + "/marry accept" +
                    ChatColor.LIGHT_PURPLE + " or " + ChatColor.DARK_PURPLE + "/marry deny");

            this.offers.put(receiver.getName(), sender.getName());
        }
    }

    @Command(command = "marry", trigger = "divorce", args = {}, playersOnly = true, permission = "marriage.player.divorce", desc = "Divorce from your current partner", usage = "/marry divorce")
    public void divorceCommand(CommandSender sender)
    {
        if (PlayerManager.isMarried(sender.getName()))
        {
            try{
                divorcePlayer(sender.getName());
                Marriage.sendMessage(sender, Marriage.getConfigs().getString("message.divorce"));
            } catch (IllegalArgumentException e){
                sender.sendMessage(ChatColor.RED + e.getMessage());
            }
        }else{
            Marriage.sendMessage(sender, Marriage.getConfigs().getString("message.not-married"));
        }
    }

    public static boolean divorcePlayer(String player) throws IllegalArgumentException
    {
        player = player.toLowerCase();
        String partner = PlayerManager.getPartner(player);
        if (PlayerManager.getStatus(player).equals(Status.MARRIED_TO_PERSON))
        {
            PlayerManager.loadPlayer(partner);
            PlayerManager.setStatus(partner, Status.DIVORCED);
            PlayerManager.setPartner(partner, "");
            PlayerManager.setTrustsPartner(partner, false);
            //this.plugin.getPlayerManager().setHomeSet(partner, false); TODO
        }

        PlayerManager.setStatus(player, Status.DIVORCED);
        PlayerManager.setPartner(player, "");
        PlayerManager.setTrustsPartner(player, false);
        //this.plugin.getPlayerManager().setHomeSet(player, false); TODO

        return true;
    }

}
