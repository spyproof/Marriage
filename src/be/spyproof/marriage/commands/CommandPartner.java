package be.spyproof.marriage.commands;

import be.spyproof.marriage.*;
import be.spyproof.marriage.annotations.Beta;
import be.spyproof.marriage.annotations.Command;
import be.spyproof.marriage.datamanager.PlayerManager;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;

/**
 * Created by Spyproof on 5/05/2015.
 * Command handling inspired by https://github.com/Zenith-
 */
public class CommandPartner
{	
	private PlayerManager playerManager;

    public CommandPartner()
    {
    	this.playerManager = Marriage.plugin.getPlayerManager();
    }

    @Command(command = "partner", trigger = "info", args = {}, playersOnly = true, permission = Permissions.partnerInfo, desc = "Check on your partner", usage = "/partner info", unlockRequired = "unlock-command.info")
    public void getPartnerName(CommandSender sender)
    {
        Status status = playerManager.getStatus(sender.getName());

        if (status.equals(Status.MARRIED_TO_PERSON))
        {
            String partnerName = playerManager.getPartner(sender.getName());
            Player partner = Marriage.plugin.getPlayer(partnerName);
            Marriage.plugin.sendMessage(sender, "&eYou are married to &6" + partnerName + "&e and is " + (partner == null ? "&coffline" : "&aonline"));
            if(playerManager.isHomeSet(sender.getName()))
            {
                Location l = playerManager.getHomeLoc(sender.getName());
                Marriage.plugin.sendMessage(sender, "&6Home: &eWorld: " + l.getWorld().getName() + "  X:" + l.getBlockX() + "  Y:" + l.getBlockY() + "  Z:" + l.getBlockZ());
            }
            else
                Marriage.plugin.sendMessage(sender, "&6Home: &eHome is not set");
            Marriage.plugin.sendMessage(sender, "&eYou are &6" + (playerManager.trustsPartner(partnerName) ? "allowed" : "not allowed") + "&e to open " + (playerManager.getGender(partnerName).equals(Gender.FEMALE) ? "her" : "his") + " inventory");
        }
        else if (status.equals(Status.MARRIED_TO_LEFT_HAND))
            Marriage.plugin.sendMessage(sender, "&eYou are married to your left hand");
        else if (status.equals(Status.MARRIED_TO_RIGHT_HAND))
            Marriage.plugin.sendMessage(sender, "&eYou are married to your right hand");
        else
            Marriage.plugin.sendMessage(sender, Messages.notMarried);
    }

    @Command(command = "partner", trigger = "seen", args = {}, playersOnly = true, permission = Permissions.partnerSeen, desc = "Last time your partner was online", usage = "/partner seen", unlockRequired = "unlock-command.seen")
    public void lastSeenPartner(CommandSender sender)
    {
        if (!playerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Marriage.plugin.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        //TODO last seen on which server

        String partnerName = playerManager.getPartner(sender.getName());
        Player partner = Marriage.plugin.getPlayer(partnerName);
        if (partner != null )
        {
            Marriage.plugin.sendMessage(sender, "&e" + partnerName + "&e is &aonline");
        }else{

            long timeDiff = (System.currentTimeMillis() - playerManager.getLastOnline(partnerName)) / 1000;
            Marriage.plugin.sendMessage(sender, Messages.lastSeen.replace("{time}", Messages.timeformat(timeDiff)));
        }
    }

    @Command(command = "partner", trigger = "chat", args = {}, playersOnly = true, permission = Permissions.partnerChat, desc = "Chat privately with your partner", usage = "/partner chat", unlockRequired = "unlock-command.chat")
    public void chat(CommandSender sender)
    {
        if (!playerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Marriage.plugin.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        if (playerManager.isPartnerChatOn(sender.getName()))
        {
            playerManager.setPartnerChat(sender.getName(), false);
            Marriage.plugin.sendMessage(sender, "&dReturning to normal chat");
        }else
        {
            Player partner = Marriage.plugin.getPlayer(playerManager.getPartner(sender.getName()));
            if (partner == null)
                Marriage.plugin.sendMessage(sender, Messages.notOnline.replace("{player}", playerManager.getPartner(sender.getName())));
            else
            {
                playerManager.setPartnerChat(sender.getName(), true);
                Marriage.plugin.sendMessage(sender, "&dYou are now privately chatting with " + partner.getDisplayName());
            }
        }
    }

    @Command(command = "partner", trigger = "tp", args = {}, playersOnly = true, permission = Permissions.partnerTp, desc = "Teleport to your partner", usage = "/partner tp", unlockRequired = "unlock-command.tp")
    public void teleport(Player sender)
    {
        if (!playerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Marriage.plugin.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        //TODO check for pvp stuff
        String partnerName = playerManager.getPartner(sender.getName());
        Player partner = Marriage.plugin.getPlayer(partnerName);
        if (partner == null)
        {
            Marriage.plugin.sendMessage(sender, Messages.notOnline.replace("{player}", partnerName));
            return;
        }

        Location location = partner.getLocation();
        sender.teleport(location);
        Marriage.plugin.sendMessage(sender, "&eTeleported to " + partner.getDisplayName());
        Marriage.plugin.sendMessage(partner, "&e" + sender.getDisplayName() + " &eteleported to you");
    }

    @Command(command = "partner", trigger = "sethome", args = {}, playersOnly = true, permission = Permissions.partnerHome, desc = "Set the home location", usage = "/partner sethome", unlockRequired = "unlock-command.home")
    public void setHome(Player sender)
    {
        if (!playerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Marriage.plugin.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        Location l = sender.getLocation();
        this.playerManager.setHome(sender.getName(), l);
        Marriage.plugin.sendMessage(sender, "&aYour home has been set!");
        Player partner = Marriage.plugin.getPlayer(this.playerManager.getPartner(sender.getName()));
        if (partner != null)
            Marriage.plugin.sendMessage(partner, "&aYour home has been changed by your partner!");

    }

    @Command(command = "partner", trigger = "home", args = {}, playersOnly = true, permission = Permissions.partnerHome, desc = "Go to your home location", usage = "/partner home", unlockRequired = "unlock-command.home")
    public void goHome(Player sender)
    {
        if (!playerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Marriage.plugin.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        //TODO check for pvp stuff
        Location l = playerManager.getHomeLoc(sender.getName());
        sender.teleport(l);
        Marriage.plugin.sendMessage(sender, "&eYou have been teleported to your home");
    }

    @Beta
    @Command(command = "partner", trigger = "inv", args = {}, playersOnly = true, permission = Permissions.partnerInventory, desc = "Open your partner's inventory", usage = "/partner inv", unlockRequired = "unlock-command.inventory")
    public void openInventory(Player sender)
    {
        /*if (!playerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Marriage.plugin.sendMessage(sender, Messages.notMarriedToPlayer);
            return; //TODO check if he trusts the inv
        }

        String partnerName = Marriage.plugin.getPlayerManager().getPartner(sender.getName());
        Player partner = Marriage.plugin.getPlayer(partnerName);
        if (partner == null)
        {
            Marriage.plugin.sendMessage(sender, Messages.notOnline.replace("{player}", partnerName));
            return;
        }*/

        Player partner = Marriage.plugin.getPlayer(playerManager.getPartner(sender.getName()));
        if (partner == null)
        {
            Marriage.plugin.sendMessage(sender, Messages.notOnline.replace("{player}", playerManager.getPartner(sender.getName())));
            return;
        }

        Inventory inv = partner.getInventory();
        sender.openInventory(inv);
    }

    @Beta
    @Command(command = "partner", trigger = "trustinv", args = {}, playersOnly = true, permission = Permissions.partnerInventory, desc = "Let's your partner open your inventory", usage = "/partner trustinv", unlockRequired = "unlock-command.inventory")
    public void trustInventory(CommandSender sender)
    {
        if (!playerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Marriage.plugin.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        boolean trustsPartner = playerManager.trustsPartner(sender.getName());
        if (trustsPartner)
        {
            playerManager.setTrustsPartner(sender.getName(), false);
            Marriage.plugin.sendMessage(sender, "&bYou no longer allow your partner to open your inventory");
        }else
        {
            playerManager.setTrustsPartner(sender.getName(), true);
            Marriage.plugin.sendMessage(sender, "&bYou allow your partner to open your inventory!\n" +
                    "&lYour partner can take anything! \n" +
                    "&6&lThey can take something even when you told them not to!\n" +
                    "&bUse &3&l/partner trustinv&b to undo this");
        }
    }

    @Command(command = "partner", trigger = "deposit", args = {"{int}"}, playersOnly = true, permission = Permissions.partnerMoney, desc = "Add money to the shared bank", usage = "/partner deposit <money>")
    public void moneyAdd(Player sender, String moneyString)
    {
        if (!playerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Marriage.plugin.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        double money = 0;
        try {
            money = roundMoney(Double.parseDouble(moneyString));
        } catch (NumberFormatException e) {
            Marriage.plugin.sendMessage(sender, "&c/partner deposit <money>");
            return;
        }

        if (money == 0)
            return;

        double balance = Marriage.eco.getBalance(sender);
        if (money > balance)
        {
            Marriage.plugin.sendMessage(sender, Messages.notEnoughMoney);
            return;
        }

        Marriage.eco.withdrawPlayer(sender, money);
        playerManager.setBalance(sender.getName(), playerManager.getBalance(sender.getName()) + money);

        Marriage.plugin.sendMessage(sender, "&eYou deposited &6$" + money + "&e in your shared bank");
        Player partner = Marriage.plugin.getPlayer(playerManager.getPartner(sender.getName()));
        if (partner != null)
            Marriage.plugin.sendMessage(partner, "&e" + sender.getDisplayName() + "&e deposited &6$" + money + "&e in your shared bank");
    }

    @Command(command = "partner", trigger = "withdraw", args = {"{int}"}, playersOnly = true, permission = Permissions.partnerMoney, desc = "Take money from the shared bank", usage = "/partner withdraw <money>")
    public void moneyRemove(Player sender, String moneyString)
    {
        if (!playerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Marriage.plugin.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        double money = 0;
        try {
            money = roundMoney(Double.parseDouble(moneyString));
        } catch (NumberFormatException e) {
            Marriage.plugin.sendMessage(sender, "&c/partner withdraw <money>");
            return;
        }

        if (money == 0)
            return;

        double sharedBalance = playerManager.getBalance(sender.getName());

        if (money > sharedBalance)
        {
            Marriage.plugin.sendMessage(sender, Messages.notEnoughMoney);
            return;
        }

        Marriage.eco.depositPlayer(sender, money);
        playerManager.setBalance(sender.getName(), playerManager.getBalance(sender.getName()) - money);

        Marriage.plugin.sendMessage(sender, "&eYou withdrew &6$" + money + "&e from your shared bank");
        Player partner = Marriage.plugin.getPlayer(playerManager.getPartner(sender.getName()));
        if (partner != null)
            Marriage.plugin.sendMessage(partner, "&e" + sender.getDisplayName() + "&e withdrew &6$" + money + "&e from your shared bank");
    }

    @Command(command = "partner", trigger = "balance", args = {}, playersOnly = true, permission = Permissions.partnerMoney, desc = "Check the balance of the shared bank", usage = "/partner balance")
    public void money(CommandSender sender)
    {
        if (!playerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Marriage.plugin.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        double sharedBalance = playerManager.getBalance(sender.getName());

        Marriage.plugin.sendMessage(sender, "&eCurrent balance: $" + sharedBalance);
    }

    private double roundMoney(double value)
    {
        long factor = (long) Math.pow(10, 2);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
