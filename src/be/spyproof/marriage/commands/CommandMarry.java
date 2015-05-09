package be.spyproof.marriage.commands;

import be.spyproof.marriage.*;
import be.spyproof.marriage.annotations.Command;
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
@SuppressWarnings("deprecation")
public class CommandMarry
{
	private PlayerManager playerManager;
    private Map<String, String> offers;

    public CommandMarry()
    {
    	this.playerManager = Marriage.plugin.getPlayerManager();
        this.offers = new HashMap<String, String>();
    }

    //TODO undo config messages?

    @Command(command = "marry", trigger = "deny", args = {}, playersOnly = true, permission = Permissions.playerMarryOther, desc = "Deny the marriage reuqest", usage = "/marry deny")
    public void DenyMarriage(CommandSender sender)
    {
        try{
            Player sadPlayer = Bukkit.getServer().getPlayer(this.offers.get(sender.getName())); //sadPartner = player who did /marry <player>

            Marriage.plugin.sendMessage(sender, ChatColor.RED + "You rejected " + sadPlayer.getName() + "'s marriage request");
            Marriage.plugin.sendMessage(sadPlayer, ChatColor.RED + sender.getName() + " rejected your marriage request ☹");

            this.offers.remove(sender.getName());

        }catch (IllegalArgumentException e){
            Marriage.plugin.sendMessage(sender, Messages.noProposal);
        }
    }

    @Command(command = "marry", trigger = "accept", args = {}, playersOnly = true, permission = Permissions.playerMarryOther, desc = "Accept the marriage request", usage = "/marry accept")
    public void AcceptMarriage(Player sender)
    {
        //TODO add a timer
        if (playerManager.isMarried(sender.getName()))
        {
            Marriage.plugin.sendMessage(sender, Messages.alreadyMarried);
            return;
        }

        String newPartner = this.offers.get(sender.getName()); //newPartner = player who did /marry <player>
        Player partner = Marriage.plugin.getPlayer(newPartner);

        if (playerManager.isMarried(newPartner))
        {
            Marriage.plugin.sendMessage(sender, Messages.alreadyMarriedOther.replace("{player}", newPartner));
            return;
        }

        //TODO economy & bypassing

        try{
            //TODO cooldowns
            playerManager.setStatus(sender.getName(), Status.MARRIED_TO_PERSON);
            playerManager.setStatus(newPartner, Status.MARRIED_TO_PERSON);
            playerManager.setPartner(sender.getName(), newPartner);
            playerManager.setPartner(newPartner, sender.getName());



            String broadcast = Messages.broadcast.replace("{prefix}", Messages.prefix).replace("{player1}", sender.getName()).replace("{player2}", partner.getDisplayName());
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcast));

            Marriage.plugin.sendMessage(sender, "&eUse &6&l/partner&e for your new perks!");
            Marriage.plugin.sendMessage(Marriage.plugin.getPlayer(newPartner), "&eUse &6&l/partner&e for your new perks!");

            this.offers.remove(sender.getName());
        }catch (IllegalArgumentException e){
            Marriage.plugin.sendMessage(sender, Messages.noProposal);
        }
    }

    @Command(command = "marry", trigger = "{left}", args = {}, playersOnly = true, permission = Permissions.playerMarrySelf, helpHidden = true)
    public void MarryLeftHand(CommandSender sender)
    {
        if (playerManager.isMarried(sender.getName()))
        {
            Marriage.plugin.sendMessage(sender, Messages.alreadyMarried);
            return;
        }

        playerManager.setStatus(sender.getName(), Status.MARRIED_TO_LEFT_HAND);

        String broadcast = Messages.broadcast.replace("{prefix}", Messages.prefix).replace("{player1}", sender.getName())
                .replace("{player2}", "its left hand");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcast));
    }

    @Command(command = "marry", trigger = "{right}", args = {}, playersOnly = true, permission = Permissions.playerMarrySelf, helpHidden = true)
    public void MarryRightHand(CommandSender sender)
    {
        if (playerManager.isMarried(sender.getName()))
        {
            Marriage.plugin.sendMessage(sender, Messages.alreadyMarried);
            return;
        }

        playerManager.setStatus(sender.getName(), Status.MARRIED_TO_RIGHT_HAND);

        String broadcast = Messages.broadcast.replace("{prefix}", Messages.prefix).replace("{player1}", sender.getName())
                .replace("{player2}", "its right hand");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcast));

    }

    @Command(command = "marry", trigger = "player", args = {"{player}"}, playersOnly = true, permission = Permissions.playerMarryOther, desc = "Send a marriage request to a player", usage = "/marry player <name>")
    public void sendMarriageRequestPlayer(CommandSender sender, String receiverName)
    {
        if (playerManager.isMarried(sender.getName()))
        {
            Marriage.plugin.sendMessage(sender, Messages.alreadyMarried);
            return;
        }

        Player receiver = Bukkit.getServer().getPlayer(receiverName);
        //TODO wtf happens here?
        if (receiver == null)
        {
            Marriage.plugin.sendMessage(sender, Messages.alreadyMarriedOther.replace("{player}", receiverName));
            return;
        }

        //TODO cooldowns
        //TODO economy & bypassing

        // When you marry yourself, marry your left or right hand
        if (sender.getName().equals(receiverName))
        {
            Marriage.plugin.sendMessage(sender, ChatColor.LIGHT_PURPLE + "Use " + ChatColor.DARK_PURPLE + "/marry left" +
                    ChatColor.LIGHT_PURPLE + " or " + ChatColor.DARK_PURPLE + "/marry right" + ChatColor.LIGHT_PURPLE
                    + " to marry yourself");
        }else{
            Marriage.plugin.sendMessage(sender, ChatColor.DARK_PURPLE + "You send a marriage request to " + receiverName);

            Marriage.plugin.sendMessage(receiver, ChatColor.LIGHT_PURPLE + sender.getName() + " wants to marry you!");
            Marriage.plugin.sendMessage(receiver, ChatColor.LIGHT_PURPLE + "Use " + ChatColor.DARK_PURPLE + "/marry accept" +
                    ChatColor.LIGHT_PURPLE + " or " + ChatColor.DARK_PURPLE + "/marry deny");

            this.offers.put(receiver.getName(), sender.getName());
        }
    }

    @Command(command = "marry", trigger = "divorce", args = {}, playersOnly = true, permission = Permissions.playerDivorce, desc = "Divorce from your current partner", usage = "/marry divorce")
    public void divorceCommand(CommandSender sender)
    {
        if (playerManager.isMarried(sender.getName()))
        {
            try{
                divorcePlayer(sender.getName());
            } catch (IllegalArgumentException e){
                Marriage.plugin.sendMessage(sender, ChatColor.RED + e.getMessage());
            }
        }else{
            Marriage.plugin.sendMessage(sender, Messages.notMarried);
        }
    }

    @Command(command = "marry", trigger = "gender", args = {"{gender}"}, playersOnly = true, permission = Permissions.playerGender, desc = "Select your gender", usage = "/marry gender <male, female, hidden>")
    public void setGender(CommandSender sender, String genderString)
    {
        Gender gender = PlayerManager.genderFromString(genderString);
        if (gender != null)
        {
            playerManager.setGender(sender.getName(), gender);
            Marriage.plugin.sendMessage(sender, "&eYour gender is now " + gender.toString());
        }else
            Marriage.plugin.sendMessage(sender, "&cPossible genders are: male, female, hidden");
    }

    @Command(command = "marry", trigger = "info", args = {"{player}"}, playersOnly = true, permission = Permissions.playerInfo, desc = "Get the player information", usage = "/marry info <player>")
    public void getPlayerInfo(CommandSender sender, String player)
    {
        String status, gender, partner;
        try{
            status = playerManager.getStatus(player).toString();
            gender = playerManager.getGender(player).toString();
            partner = playerManager.getPartner(player);

            Marriage.plugin.sendMessage(sender, "&e------------&6&l" + player + "&e------------");

            if (gender.equalsIgnoreCase(Gender.MALE.toString()))
                Marriage.plugin.sendMessage(sender, "&6Gender:&b ♂ &e" + gender);
            else if(gender.equalsIgnoreCase(Gender.FEMALE.toString()))
                Marriage.plugin.sendMessage(sender, "&6Gender:&d ♀ &e" + gender);
            else
                Marriage.plugin.sendMessage(sender, "&6Gender: &e" + gender);

            Marriage.plugin.sendMessage(sender, "&6Status: &e" + status);
            if (status.equalsIgnoreCase(Status.MARRIED_TO_PERSON.toString()))
                Marriage.plugin.sendMessage(sender, "&6Partner: &e" + partner);
        }catch (IllegalArgumentException e){
            Marriage.plugin.sendMessage(sender, ChatColor.RED + e.getMessage());
        }
    }

    public static boolean divorcePlayer(String player) throws IllegalArgumentException
    {
        player = player.toLowerCase();
        String partner = Marriage.plugin.getPlayerManager().getPartner(player);
        if (Marriage.plugin.getPlayerManager().getStatus(player).equals(Status.MARRIED_TO_PERSON))
        {
        	Marriage.plugin.getPlayerManager().addPlayer(partner);
            Marriage.plugin.getPlayerManager().setStatus(partner, Status.DIVORCED);
            Marriage.plugin.getPlayerManager().setPartner(partner, "");
            Marriage.plugin.getPlayerManager().setTrustsPartner(partner, false);
            Marriage.plugin.getPlayerManager().removeHome(partner);
            Marriage.plugin.sendMessage(partner, Messages.divorce);
        }

        Marriage.plugin.getPlayerManager().setStatus(player, Status.DIVORCED);
        Marriage.plugin.getPlayerManager().setPartner(player, "");
        Marriage.plugin.getPlayerManager().setTrustsPartner(player, false);
        Marriage.plugin.getPlayerManager().removeHome(player);
        Marriage.plugin.sendMessage(player, Messages.divorce);

        return true;
    }

}
