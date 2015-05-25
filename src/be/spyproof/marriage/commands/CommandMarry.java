package be.spyproof.marriage.commands;

import be.spyproof.marriage.*;
import be.spyproof.marriage.annotations.Command;
import be.spyproof.marriage.datamanager.CooldownManager;
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
public class CommandMarry
{
	private PlayerManager playerManager;
    private Map<String, String> offers;// receiver - sender
    private Map<String, Long> timeout;// sender - cooldown
    private double cost = Marriage.config.getDouble("marriage-cost");
    private Long timeoutCooldown;

    public CommandMarry()
    {
    	this.playerManager = Marriage.plugin.getPlayerManager();
        this.offers = new HashMap<String, String>();
        this.timeout = new HashMap<String, Long>();
        this.timeoutCooldown = Marriage.config.getLong("cooldown.request") * 60 * 1000;
    }

    @Command(command = "marry", trigger = "deny", args = {}, playersOnly = true, permission = Permissions.playerMarryOther, desc = "Deny the marriage request", usage = "/marry deny")
    public void DenyMarriage(CommandSender sender)
    {
        try{
            Player sadPlayer = Marriage.plugin.getPlayer(this.offers.get(sender.getName().toLowerCase())); //sadPartner = player who did /marry <player>

            Marriage.plugin.sendMessage(sender, ChatColor.RED + "You rejected " + sadPlayer.getName() + "'s marriage request");
            Marriage.plugin.sendMessage(sadPlayer, ChatColor.RED + sender.getName() + " rejected your marriage request ☹");

            this.timeout.remove(this.offers.get(sender.getName().toLowerCase()));
            this.offers.remove(sender.getName().toLowerCase());

        }catch (IllegalArgumentException e){
            Marriage.plugin.sendMessage(sender, Messages.noProposal);
        }
    }

    @Command(command = "marry", trigger = "accept", args = {}, playersOnly = true, permission = Permissions.playerMarryOther, desc = "Accept the marriage request", usage = "/marry accept")
    public void AcceptMarriage(Player sender)
    {
        if (playerManager.isMarried(sender.getName()))
        {
            Marriage.plugin.sendMessage(sender, Messages.alreadyMarried);
            return;
        }

        String newPartner = this.offers.get(sender.getName().toLowerCase()); //newPartner = player who did /marry <player>
        Player partner = Marriage.plugin.getPlayer(newPartner);

        //Check if offer has not expired yet
        if (this.timeout.containsKey(newPartner.toLowerCase()))
            if (this.timeout.get(newPartner.toLowerCase()) < System.currentTimeMillis())
            {
                Marriage.plugin.sendMessage(sender, Messages.proposalExpired);
                this.timeout.remove(newPartner);
                return;
            }

        if (playerManager.isMarried(newPartner))
        {
            Marriage.plugin.sendMessage(sender, Messages.alreadyMarriedOther.replace("{player}", newPartner));
            return;
        }

        int cooldown = CooldownManager.cooldownManager.getCooldown(sender, "divorce");
        if (cooldown > 0)
        {
            Marriage.plugin.sendMessage(sender, Messages.onCooldown.replace("{time}", Messages.timeformat(cooldown)));
            return;
        }

        CooldownManager.cooldownManager.setCooldown(sender.getName(), "request");

        if (!Permissions.hasPerm(partner, Permissions.bypassMarriageCosts))
        {
            if (Marriage.eco.has(partner, this.cost))
            {
                Marriage.eco.withdrawPlayer(partner, this.cost);
                Marriage.plugin.sendMessage(partner, "Withdrew $" + this.cost + " for the marriage");
            }
            else {
                Marriage.plugin.sendMessage(partner, Messages.notEnoughMoney);
                Marriage.plugin.sendMessage(sender, Messages.notEnoughMoneyPartner.replace("{player}", partner.getDisplayName()));
                return;
            }
        }

        try{
            playerManager.setStatus(sender.getName(), Status.MARRIED_TO_PERSON);
            playerManager.setStatus(newPartner, Status.MARRIED_TO_PERSON);
            playerManager.setPartner(sender.getName(), newPartner);
            playerManager.setPartner(newPartner, sender.getName());



            String broadcast = Messages.broadcast.replace("{prefix}", Messages.prefix).replace("{player1}", sender
                    .getName()).replace("{player2}", partner.getDisplayName());
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcast));

            Marriage.plugin.sendMessage(sender, "&eUse &6&l/partner&e for your new perks!");
            Marriage.plugin.sendMessage(Marriage.plugin.getPlayer(newPartner), "&eUse &6&l/partner&e for your new perks!");

            this.offers.remove(sender.getName().toLowerCase());
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

        int cooldownRequest = CooldownManager.cooldownManager.getCooldown(sender, "request");
        int cooldownDivorce = CooldownManager.cooldownManager.getCooldown(sender, "divorce");
        int cooldown = cooldownDivorce > cooldownRequest ? cooldownDivorce : cooldownRequest;

        if (cooldown > 0 && !Permissions.hasPerm(sender, Permissions.bypassCooldown))
        {
            Marriage.plugin.sendMessage(sender, Messages.onCooldown.replace("{time}", Messages.timeformat(cooldown)));
            return;
        }

        playerManager.setStatus(sender.getName(), Status.MARRIED_TO_LEFT_HAND);

        String broadcast = Messages.broadcast.replace("{prefix}", Messages.prefix).replace("{player1}", sender.getName())
                .replace("{player2}", (playerManager.getGender(sender.getName()).equals(Gender.FEMALE) ? "her" : "his") + " left hand");
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

        int cooldownRequest = CooldownManager.cooldownManager.getCooldown(sender, "request");
        int cooldownDivorce = CooldownManager.cooldownManager.getCooldown(sender, "divorce");
        int cooldown = cooldownDivorce > cooldownRequest ? cooldownDivorce : cooldownRequest;

        if (cooldown > 0)
        {
            Marriage.plugin.sendMessage(sender, Messages.onCooldown.replace("{time}", Messages.timeformat(cooldown)));
            return;
        }

        playerManager.setStatus(sender.getName(), Status.MARRIED_TO_RIGHT_HAND);

        String broadcast = Messages.broadcast.replace("{prefix}", Messages.prefix).replace("{player1}", sender.getName())
                .replace("{player2}", (playerManager.getGender(sender.getName()).equals(Gender.FEMALE) ? "her" : "his") + " right hand");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcast));

    }

    //TODO add money to desc
    @Command(command = "marry", trigger = "player", args = {"{player}"}, playersOnly = true, permission = Permissions.playerMarryOther, desc = "Marry a player ($10'000)", usage = "/marry player <name>")
    public void sendMarriageRequestPlayer(Player sender, String receiverName)
    {
        Player receiver = Marriage.plugin.getPlayer(receiverName);
        if (receiver == null)
        {
            Marriage.plugin.sendMessage(sender, Messages.notOnline.replace("{player}", receiverName));
            return;
        }

        if (playerManager.isMarried(sender.getName()))
        {
            Marriage.plugin.sendMessage(sender, Messages.alreadyMarried);
            return;
        }

        if (playerManager.isMarried(receiverName))
        {
            Marriage.plugin.sendMessage(sender, Messages.alreadyMarriedOther.replace("{player}", receiverName));
            return;
        }

        //Check for pending marriage requests
        if (this.offers.containsKey(receiverName.toLowerCase()))
        {
            String key = this.offers.get(receiverName.toLowerCase());
            if (this.timeout.get(key) > System.currentTimeMillis()) {
                Marriage.plugin.sendMessage(sender, Messages.activeProposal.replace("{player}", receiverName));
                return;
            }else {
                this.timeout.remove(key);
            }
        }

        //TODO check last divorce of other player

        int cooldownRequest = CooldownManager.cooldownManager.getCooldown(sender, "request");
        int cooldownDivorce = CooldownManager.cooldownManager.getCooldown(sender, "divorce");
        int cooldown = cooldownDivorce > cooldownRequest ? cooldownDivorce : cooldownRequest;

        if (cooldown > 0)
        {
            Marriage.plugin.sendMessage(sender, Messages.onCooldown.replace("{time}", Messages.timeformat(cooldown)));
            return;
        }

        if (!Permissions.hasPerm(sender, Permissions.bypassMarriageCosts))
        {
            if (!Marriage.eco.has(sender, this.cost))
            {
                Marriage.plugin.sendMessage(sender, Messages.notEnoughMoney);
                return;
            }
        }

        // When you marry yourself, marry your left or right hand
        if (sender.getName().equals(receiverName))
        {
            Marriage.plugin.sendMessage(sender, ChatColor.LIGHT_PURPLE + "Use " + ChatColor.DARK_PURPLE + "/marry left" +
                    ChatColor.LIGHT_PURPLE + " or " + ChatColor.DARK_PURPLE + "/marry right" + ChatColor.LIGHT_PURPLE
                    + " to marry yourself");
        }else{
            this.timeout.put(sender.getName().toLowerCase(), System.currentTimeMillis() + this.timeoutCooldown);
            CooldownManager.cooldownManager.setCooldown(sender, "request");
            Marriage.plugin.sendMessage(sender, ChatColor.DARK_PURPLE + "You send a marriage request to " + receiverName);

            Marriage.plugin.sendMessage(receiver, ChatColor.LIGHT_PURPLE + sender.getName() + " wants to marry you!");
            Marriage.plugin.sendMessage(receiver, ChatColor.LIGHT_PURPLE + "Use " + ChatColor.DARK_PURPLE + "/marry accept" +
                    ChatColor.LIGHT_PURPLE + " or " + ChatColor.DARK_PURPLE + "/marry deny");

            this.offers.put(receiver.getName().toLowerCase(), sender.getName().toLowerCase());
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
        Gender gender = Gender.fromString(genderString);
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

    @SuppressWarnings("deprecation")
    public static boolean divorcePlayer(String player) throws IllegalArgumentException
    {
        player = player.toLowerCase();
        String partner = Marriage.plugin.getPlayerManager().getPartner(player);

        if (Marriage.plugin.getPlayerManager().getStatus(player).equals(Status.MARRIED_TO_PERSON))
        {
            double balance = Marriage.plugin.getPlayerManager().getBalance(player);
            Marriage.eco.depositPlayer(player, balance/2);
            Marriage.eco.depositPlayer(partner, balance/2);
            Marriage.plugin.getPlayerManager().setBalance(player, 0D);
            Marriage.plugin.getPlayerManager().setBalance(partner, 0D);

            CooldownManager.cooldownManager.setCooldown(player, "divorce");
            CooldownManager.cooldownManager.setCooldown(partner, "divorce");

        	Marriage.plugin.getPlayerManager().addPlayer(partner);
            Marriage.plugin.getPlayerManager().setStatus(partner, Status.DIVORCED);
            Marriage.plugin.getPlayerManager().setPartner(partner, "");
            Marriage.plugin.getPlayerManager().setTrustsPartner(partner, false);
            Marriage.plugin.getPlayerManager().removeHome(partner);
            Marriage.plugin.getPlayerManager().setPartnerChat(partner, false);
            Marriage.plugin.sendMessage(partner, Messages.divorce);
        }

        Marriage.plugin.getPlayerManager().setStatus(player, Status.DIVORCED);
        Marriage.plugin.getPlayerManager().setPartner(player, "");
        Marriage.plugin.getPlayerManager().setTrustsPartner(player, false);
        Marriage.plugin.getPlayerManager().removeHome(player);
        Marriage.plugin.getPlayerManager().setPartnerChat(player, false);
        Marriage.plugin.sendMessage(player, Messages.divorce);

        return true;
    }

}
