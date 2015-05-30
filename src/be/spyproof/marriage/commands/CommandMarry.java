package be.spyproof.marriage.commands;

import be.spyproof.marriage.Gender;
import be.spyproof.marriage.Marriage;
import be.spyproof.marriage.Status;
import be.spyproof.marriage.annotations.Command;
import be.spyproof.marriage.annotations.Default;
import be.spyproof.marriage.annotations.SpecialArgs;
import be.spyproof.marriage.datamanager.CooldownManager;
import be.spyproof.marriage.datamanager.PlayerManager;
import be.spyproof.marriage.exceptions.PermissionException;
import be.spyproof.marriage.handlers.Messages;
import be.spyproof.marriage.handlers.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        this.timeoutCooldown = (long) (Marriage.config.getInt("cooldown.request") * 60 * 1000);
    }

    @Command(command = "marry", trigger = "deny", args = {}, playersOnly = true, permission = Permissions.playerMarryOther, desc = "Deny the marriage request", usage = "/marry deny")
    public void denyMarriage(CommandSender sender)
    {
        try{
            Player sadPlayer = Marriage.plugin.getPlayer(this.offers.get(sender.getName().toLowerCase())); //sadPartner = player who did /marry <player>

            Messages.sendMessage(sender, ChatColor.RED + "You rejected " + sadPlayer.getName() + "'s marriage request");
            if (sadPlayer != null)
                Messages.sendMessage(sadPlayer, ChatColor.RED + sender.getName() + " rejected your marriage request ☹");

            this.timeout.remove(this.offers.get(sender.getName().toLowerCase()));
            this.offers.remove(sender.getName().toLowerCase());

        }catch (IllegalArgumentException e){
            Messages.sendMessage(sender, Messages.noProposal);
        }
    }

    @Command(command = "marry", trigger = "accept", args = {}, playersOnly = true, permission = Permissions.playerMarryOther, desc = "Accept the marriage request", usage = "/marry accept")
    public void acceptMarriage(final Player sender)
    {
        if (playerManager.isMarried(sender.getName()))
        {
            Messages.sendMessage(sender, Messages.alreadyMarried);
            return;
        }

        final String newPartner = this.offers.get(sender.getName().toLowerCase()); //newPartner = player who did /marry <player>
        Player partner = Marriage.plugin.getPlayer(newPartner);

        if (newPartner == null)
        {
            Messages.sendMessage(sender, Messages.noProposal);
            return;
        }

        //Check if offer has not expired yet
        if (this.timeout.containsKey(newPartner.toLowerCase()))
        {
            if (this.timeout.get(newPartner.toLowerCase()) < System.currentTimeMillis())
            {
                Messages.sendMessage(sender, Messages.proposalExpired);
                return;
            }
        }

        if (playerManager.isMarried(newPartner))
        {
            Messages.sendMessage(sender, Messages.alreadyMarriedOther.replace("{player}", newPartner));
            return;
        }

        int cooldown = CooldownManager.cooldownManager.getCooldown(sender, "divorce");
        if (cooldown > 0)
        {
            Messages.sendMessage(sender, Messages.onCooldown.replace("{time}", Messages.timeformat(cooldown)));
            return;
        }

        CooldownManager.cooldownManager.setCooldown(sender.getName(), "request");

        //Check if player has enough money
        try {
            Permissions.hasPerm(partner, Permissions.bypassMarriageCosts);
        } catch (PermissionException e) {
            if (Marriage.eco.has(partner, this.cost))
            {
                Marriage.eco.withdrawPlayer(partner, this.cost);
                Messages.sendMessage(partner, "Withdrew $" + this.cost + " for the marriage");
            }
            else {
                Messages.sendMessage(partner, Messages.notEnoughMoney);
                Messages.sendMessage(sender, Messages.notEnoughMoneyPartner.replace("{player}", partner.getDisplayName()));
                return;
            }
        }

        try{
            playerManager.setStatus(sender.getName(), Status.MARRIED_TO_PERSON);
            playerManager.setStatus(newPartner, Status.MARRIED_TO_PERSON);
            playerManager.setPartner(sender.getName(), newPartner);
            playerManager.setPartner(newPartner, sender.getName());

            for (Player p : Marriage.plugin.getOnlinePlayers())
            {
                for (int i = 0; i < p.getInventory().getSize(); i++)
                {
                    if (p.getInventory().getItem(i) == null)
                    {
                        //Create a cake
                        ItemStack item = new ItemStack(Material.CAKE);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Wedding cake");
                        meta.setLore(new ArrayList<String>()
                        {{add(newPartner + " & " + sender.getName());}});
                        meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
                        item.setItemMeta(meta);

                        //givecake
                        p.getInventory().setItem(i, item);

                        break;
                    }
                }
            }

            String broadcast = Messages.broadcast.replace("{prefix}", Messages.prefix).replace("{player1}", sender
                    .getName()).replace("{player2}", partner.getDisplayName());
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcast));

            Messages.sendMessage(sender, "&eUse &6&l/partner&e for your new perks!");
            Messages.sendMessage(newPartner, "&eUse &6&l/partner&e for your new perks!");

            this.offers.remove(sender.getName().toLowerCase());
        }catch (IllegalArgumentException e){
            Messages.sendMessage(sender, Messages.noProposal);
        }
    }

    @Command(command = "marry", trigger = "left", args = {}, playersOnly = true, permission = Permissions.playerMarrySelf, hidden = true)
    public void marryLeftHand(CommandSender sender)
    {
        if (playerManager.isMarried(sender.getName()))
        {
            Messages.sendMessage(sender, Messages.alreadyMarried);
            return;
        }

        //Check for cooldown
        int cooldownRequest = CooldownManager.cooldownManager.getCooldown(sender, "request");
        int cooldownDivorce = CooldownManager.cooldownManager.getCooldown(sender, "divorce");
        int cooldown = cooldownDivorce > cooldownRequest ? cooldownDivorce : cooldownRequest;

        try {
            Permissions.hasPerm(sender, Permissions.bypassCooldown);
        } catch (PermissionException e)
        {
            if (cooldown > 0 && !e.hasPermission())
            {
                Messages.sendMessage(sender, Messages.onCooldown.replace("{time}", Messages.timeformat(cooldown)));
                return;
            }
        }

        playerManager.setStatus(sender.getName(), Status.MARRIED_TO_LEFT_HAND);

        String broadcast = Messages.broadcast.replace("{prefix}", Messages.prefix).replace("{player1}", sender.getName())
                .replace("{player2}", (playerManager.getGender(sender.getName()).equals(Gender.FEMALE) ? "her" : "his") + " left hand");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcast));
    }

    @Command(command = "marry", trigger = "right", args = {}, playersOnly = true, permission = Permissions.playerMarrySelf, hidden = true)
    public void marryRightHand(CommandSender sender)
    {
        if (playerManager.isMarried(sender.getName()))
        {
            Messages.sendMessage(sender, Messages.alreadyMarried);
            return;
        }

        int cooldownRequest = CooldownManager.cooldownManager.getCooldown(sender, "request");
        int cooldownDivorce = CooldownManager.cooldownManager.getCooldown(sender, "divorce");
        int cooldown = cooldownDivorce > cooldownRequest ? cooldownDivorce : cooldownRequest;

        if (cooldown > 0)
        {
            Messages.sendMessage(sender, Messages.onCooldown.replace("{time}", Messages.timeformat(cooldown)));
            return;
        }

        playerManager.setStatus(sender.getName(), Status.MARRIED_TO_RIGHT_HAND);

        String broadcast = Messages.broadcast.replace("{prefix}", Messages.prefix).replace("{player1}", sender.getName())
                .replace("{player2}", (playerManager.getGender(sender.getName()).equals(Gender.FEMALE) ? "her" : "his") + " right hand");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcast));

    }

    //TODO add money to desc somehow
    @Command(command = "marry", trigger = "{onlineplayer}", args = {}, playersOnly = true, permission = Permissions.playerMarryOther, desc = "Marry a player", usage = "/marry <name>")
    public void sendMarriageRequestPlayer(Player sender, String receiverName)
    {
        Player receiver = Marriage.plugin.getPlayer(receiverName);
        if (receiver == null)
        {
            Messages.sendMessage(sender, Messages.notOnline.replace("{player}", receiverName));
            return;
        }

        if (playerManager.isMarried(sender.getName()))
        {
            Messages.sendMessage(sender, Messages.alreadyMarried);
            return;
        }

        if (playerManager.isMarried(receiverName))
        {
            Messages.sendMessage(sender, Messages.alreadyMarriedOther.replace("{player}", receiverName));
            return;
        }

        //Check for pending marriage requests
        if (this.offers.containsKey(receiverName.toLowerCase()))
        {
            String key = this.offers.get(receiverName.toLowerCase());
            if (this.timeout.get(key) != null)
            {
                if (this.timeout.get(key) > System.currentTimeMillis()) {
                    Messages.sendMessage(sender, Messages.activeProposal.replace("{player}", receiverName));
                    return;
                }
                else {
                    this.timeout.remove(key);
                }
            }
        }

        //TODO check last divorce of other player

        int cooldownRequest = CooldownManager.cooldownManager.getCooldown(sender, "request");
        int cooldownDivorce = CooldownManager.cooldownManager.getCooldown(sender, "divorce");
        int cooldown = cooldownDivorce > cooldownRequest ? cooldownDivorce : cooldownRequest;

        if (cooldown > 0)
        {
            Messages.sendMessage(sender, Messages.onCooldown.replace("{time}", Messages.timeformat(cooldown)));
            return;
        }

        //Check if player has money
        try
        {
            Permissions.hasPerm(sender, Permissions.bypassMarriageCosts);
        } catch (PermissionException e)
        {
            if (!Marriage.eco.has(sender, this.cost) && !e.hasPermission())
            {
                Messages.sendMessage(sender, Messages.notEnoughMoney);
                return;
            }
        }

        // When you marry yourself, marry your left or right hand
        if (sender.getName().equals(receiverName))
        {
            Messages.sendMessage(sender, ChatColor.LIGHT_PURPLE + "Use " + ChatColor.DARK_PURPLE + "/marry left" +
                    ChatColor.LIGHT_PURPLE + " or " + ChatColor.DARK_PURPLE + "/marry right" + ChatColor.LIGHT_PURPLE
                    + " to marry yourself");
        }else{
            this.timeout.put(sender.getName().toLowerCase(), System.currentTimeMillis() + this.timeoutCooldown);
            CooldownManager.cooldownManager.setCooldown(sender, "request");
            Messages.sendMessage(sender, ChatColor.DARK_PURPLE + "You send a marriage request to " + receiverName);

            Messages.sendMessage(receiver, ChatColor.LIGHT_PURPLE + sender.getName() + " wants to marry you!");
            Messages.sendMessage(receiver, ChatColor.LIGHT_PURPLE + "Use " + ChatColor.DARK_PURPLE + "/marry accept" +
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
                Messages.sendMessage(sender, ChatColor.RED + e.getMessage());
            }
        }else{
            Messages.sendMessage(sender, Messages.notMarried);
        }
    }

    @Command(command = "marry", trigger = "gender", args = {"{gender}"}, playersOnly = true, permission = Permissions.playerGender, desc = "Select your gender", usage = "/marry gender <male, female, hidden>")
    public void setGender(CommandSender sender, String genderString)
    {
        Gender gender = Gender.fromString(genderString);
        if (gender != null)
        {
            playerManager.setGender(sender.getName(), gender);
            Messages.sendMessage(sender, "&eYour gender is now " + gender.toString());
        }else
            Messages.sendMessage(sender, "&cPossible genders are: male, female, hidden");
    }

    @Default({"{player}"})
    @Command(command = "marry", trigger = "info", args = {"{player}"}, playersOnly = true, permission = Permissions.playerInfo, desc = "Get the player information", usage = "/marry info <player>")
    public void getPlayerInfo(CommandSender sender, String player)
    {
        String status, gender, partner;
        try{
            status = playerManager.getStatus(player).toString();
            gender = playerManager.getGender(player).toString();
            partner = playerManager.getPartner(player);

            Messages.sendMessage(sender, "&e------------&6&l" + player + "&e------------");

            if (gender.equalsIgnoreCase(Gender.MALE.toString()))
                Messages.sendMessage(sender, "&6Gender:&b ♂ &e" + gender);
            else if(gender.equalsIgnoreCase(Gender.FEMALE.toString()))
                Messages.sendMessage(sender, "&6Gender:&d ♀ &e" + gender);
            else
                Messages.sendMessage(sender, "&6Gender: &e" + gender);

            Messages.sendMessage(sender, "&6Status: &e" + status);
            if (status.equalsIgnoreCase(Status.MARRIED_TO_PERSON.toString()))
                Messages.sendMessage(sender, "&6Partner: &e" + partner);
        }catch (IllegalArgumentException e){
            Messages.sendMessage(sender, ChatColor.RED + e.getMessage());
        }
    }

    @SpecialArgs("{player}")
    public List<String> argsPlayer()
    {
        return null;
    }

    @SpecialArgs("{onlineplayer}")
    public List<String> argsOnlinePlayer()
    {
        List<String> possibleArgs = new ArrayList<String>();

        for (Player p : Marriage.plugin.getOnlinePlayers())
            possibleArgs.add(p.getName());

        return possibleArgs;
    }

    @SpecialArgs("{gender}")
    public List<String> argsGender()
    {
        List<String> possibleArgs = new ArrayList<String>();

        for (Gender g : Gender.values())
            possibleArgs.add(g.toString());

        return possibleArgs;
    }

    public static boolean divorcePlayer(String player) throws IllegalArgumentException
    {
        player = player.toLowerCase();
        String partner = Marriage.plugin.getPlayerManager().getPartner(player);

        Marriage.plugin.getPlayerManager().divorcePlayer(player);

        if (Marriage.plugin.getPlayerManager().getStatus(player).equals(Status.MARRIED_TO_PERSON)) {
            Messages.sendMessage(partner, Messages.divorce);
        }

        Messages.sendMessage(player, Messages.divorce);

        return true;
    }

}
