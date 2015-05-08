package be.spyproof.marriage.commands;

import be.spyproof.marriage.Gender;
import be.spyproof.marriage.Marriage;
import be.spyproof.marriage.Messages;
import be.spyproof.marriage.Status;
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
@SuppressWarnings("deprecated")
public class CommandMarry
{
    private Map<String, String> offers;

    public CommandMarry()
    {
        this.offers = new HashMap<String, String>();
    }

    //TODO undo config messages?

    @Command(command = "marry", trigger = "deny", args = {}, playersOnly = true, permission = "marriage.player.marry.other", desc = "Deny the marriage reuqest", usage = "/marry deny")
    public void DenyMarriage(CommandSender sender)
    {
        try{
            Player sadPlayer = Bukkit.getServer().getPlayer(this.offers.get(sender.getName())); //sadPartner = player who did /marry <player>

            Marriage.sendMessage(sender, ChatColor.RED + "You rejected " + sadPlayer.getName() + "'s marriage request");
            Marriage.sendMessage(sadPlayer, ChatColor.RED + sender.getName() + " rejected your marriage request ☹");

            this.offers.remove(sender.getName());

        }catch (IllegalArgumentException e){
            Marriage.sendMessage(sender, Messages.noProposal);
        }
    }

    @Command(command = "marry", trigger = "accept", args = {}, playersOnly = true, permission = "marriage.player.marry.other", desc = "Accept the marriage request", usage = "/marry accept")
    public void AcceptMarriage(Player sender)
    {
        //TODO add a timer
        if (PlayerManager.isMarried(sender.getName()))
        {
            Marriage.sendMessage(sender, Messages.alreadyMarried);
            return;
        }

        String newPartner = this.offers.get(sender.getName()); //newPartner = player who did /marry <player>
        Player partner = Marriage.getPlayer(newPartner);

        if (PlayerManager.isMarried(newPartner))
        {
            Marriage.sendMessage(sender, Messages.alreadyMarriedOther.replace("{player}", newPartner));
            return;
        }

        //TODO economy & bypassing

        try{
            //TODO cooldowns
            PlayerManager.setStatus(sender.getName(), Status.MARRIED_TO_PERSON);
            PlayerManager.setStatus(newPartner, Status.MARRIED_TO_PERSON);
            PlayerManager.setPartner(sender.getName(), newPartner);
            PlayerManager.setPartner(newPartner, sender.getName());



            String broadcast = Messages.broadcast.replace("{prefix}", Messages.prefix).replace("{player1}", sender.getName()).replace("{player2}", partner.getDisplayName());
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcast));

            Marriage.sendMessage(sender, "&eUse &6&l/partner&e for your new perks!");
            Marriage.sendMessage(Marriage.getPlayer(newPartner), "&eUse &6&l/partner&e for your new perks!");

            this.offers.remove(sender.getName());
        }catch (IllegalArgumentException e){
            Marriage.sendMessage(sender, Messages.noProposal);
        }
    }

    @Command(command = "marry", trigger = "{left}", args = {}, playersOnly = true, permission = "marriage.player.marry.self", helpHidden = true)
    public void MarryLeftHand(CommandSender sender)
    {
        if (PlayerManager.isMarried(sender.getName()))
        {
            Marriage.sendMessage(sender, Messages.alreadyMarried);
            return;
        }

        PlayerManager.setStatus(sender.getName(), Status.MARRIED_TO_LEFT_HAND);

        String broadcast = Messages.broadcast.replace("{prefix}", Messages.prefix).replace("{player1}", sender.getName())
                .replace("{player2}", "its left hand");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcast));
    }

    @Command(command = "marry", trigger = "{right}", args = {}, playersOnly = true, permission = "marriage.player.marry.self", helpHidden = true)
    public void MarryRightHand(CommandSender sender)
    {
        if (PlayerManager.isMarried(sender.getName()))
        {
            Marriage.sendMessage(sender, Messages.alreadyMarried);
            return;
        }

        PlayerManager.setStatus(sender.getName(), Status.MARRIED_TO_RIGHT_HAND);

        String broadcast = Messages.broadcast.replace("{prefix}", Messages.prefix).replace("{player1}", sender.getName())
                .replace("{player2}", "its right hand");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcast));

    }

    @Command(command = "marry", trigger = "player", args = {"{player}"}, playersOnly = true, permission = "marriage.player.marry.other", desc = "Send a marriage request to a player", usage = "/marry player <name>")
    public void sendMarriageRequestPlayer(CommandSender sender, String receiverName)
    {
        if (PlayerManager.isMarried(sender.getName()))
        {
            Marriage.sendMessage(sender, Messages.alreadyMarried);
            return;
        }

        Player receiver = Bukkit.getServer().getPlayer(receiverName);
        //TODO wtf happens here?
        if (receiver == null)
        {
            Marriage.sendMessage(sender, Messages.alreadyMarriedOther.replace("{player}", receiverName));
            return;
        }

        //TODO cooldowns
        //TODO economy & bypassing

        // When you marry yourself, marry your left or right hand
        if (sender.getName().equals(receiverName))
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
            } catch (IllegalArgumentException e){
                Marriage.sendMessage(sender, ChatColor.RED + e.getMessage());
            }
        }else{
            Marriage.sendMessage(sender, Messages.notMarried);
        }
    }

    @Command(command = "marry", trigger = "gender", args = {"{gender}"}, playersOnly = true, permission = "marriage.player.gender", desc = "Select your gender", usage = "/marry gender <male, female, hidden>")
    public void setGender(CommandSender sender, String genderString)
    {
        Gender gender = Gender.fromString(genderString);
        if (gender != null)
        {
            PlayerManager.setGender(sender.getName(), gender);
            Marriage.sendMessage(sender, "&eYour gender is now " + gender.toString());
        }else
            Marriage.sendMessage(sender, "&cPossible genders are: male, female, hidden");
    }

    @Command(command = "marry", trigger = "info", args = {"{player}"}, playersOnly = true, permission = "marriage.player.info", desc = "Get the player information", usage = "/marry info <player>")
    public void getPlayerInfo(CommandSender sender, String player)
    {
        String status, gender, partner;
        try{
            status = PlayerManager.getStatus(player).toString();
            gender = PlayerManager.getGender(player).toString();
            partner = PlayerManager.getPartner(player);

            Marriage.sendMessage(sender, "&e------------&6&l" + player + "&e------------");

            if (gender.equalsIgnoreCase(Gender.MALE.toString()))
                Marriage.sendMessage(sender, "&6Gender:&b ♂ &e" + gender);
            else if(gender.equalsIgnoreCase(Gender.FEMALE.toString()))
                Marriage.sendMessage(sender, "&6Gender:&d ♀ &e" + gender);
            else
                Marriage.sendMessage(sender, "&6Gender: &e" + gender);

            Marriage.sendMessage(sender, "&6Status: &e" + status);
            if (status.equalsIgnoreCase(Status.MARRIED_TO_PERSON.toString()))
                Marriage.sendMessage(sender, "&6Partner: &e" + partner);
        }catch (IllegalArgumentException e){
            Marriage.sendMessage(sender, ChatColor.RED + e.getMessage());
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
            PlayerManager.removeHome(partner);
            Marriage.sendMessage(partner, Messages.divorce);
        }

        PlayerManager.setStatus(player, Status.DIVORCED);
        PlayerManager.setPartner(player, "");
        PlayerManager.setTrustsPartner(player, false);
        PlayerManager.removeHome(player);
        Marriage.sendMessage(player, Messages.divorce);

        return true;
    }

}
