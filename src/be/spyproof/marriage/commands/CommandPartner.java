package be.spyproof.marriage.commands;

import be.spyproof.marriage.*;
import be.spyproof.marriage.annotations.Beta;
import be.spyproof.marriage.annotations.Command;
import be.spyproof.marriage.annotations.Default;
import be.spyproof.marriage.datamanager.PlayerManager;

import be.spyproof.marriage.handlers.CommandHandler;
import be.spyproof.marriage.handlers.Messages;
import be.spyproof.marriage.handlers.Permissions;
import com.earth2me.essentials.Essentials;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import de.slikey.effectlib.effect.AnimatedBallEffect;
import de.slikey.effectlib.util.ParticleEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Command(command = "partner", trigger = "info", args = {}, playersOnly = true, permission = Permissions.partnerInfo, desc = "Check on your partner", usage = "/partner info", unlockRequired = Permissions.unlockCommandInfo)
    public void getPartnerName(CommandSender sender)
    {
        Status status = playerManager.getStatus(sender.getName());

        if (status.equals(Status.MARRIED_TO_PERSON))
        {
            String partnerName = playerManager.getPartner(sender.getName());
            Player partner = Marriage.plugin.getPlayer(partnerName);
            Messages.sendMessage(sender, "&eYou are married to &6" + partnerName + "&e and is " + (partner == null ?
                    "&coffline" : "&aonline"));
            if(playerManager.isHomeSet(sender.getName()))
            {
                Location l = playerManager.getHomeLoc(sender.getName());
                Messages.sendMessage(sender, "&6Home: &eWorld: " + l.getWorld().getName() + "  X:" + l.getBlockX() + "  Y:" + l.getBlockY() + "  Z:" + l.getBlockZ());
            }
            else
                Messages.sendMessage(sender, "&6Home: &eHome is not set");
        }
        else if (status.equals(Status.MARRIED_TO_LEFT_HAND))
            Messages.sendMessage(sender, "&eYou are married to your left hand");
        else if (status.equals(Status.MARRIED_TO_RIGHT_HAND))
            Messages.sendMessage(sender, "&eYou are married to your right hand");
        else
            Messages.sendMessage(sender, Messages.notMarried);
    }

    @Command(command = "partner", trigger = "seen", args = {}, playersOnly = true, permission = Permissions.partnerSeen, desc = "Last time your partner was online", usage = "/partner seen", unlockRequired = Permissions.unlockCommandSeen)
    public void lastSeenPartner(CommandSender sender)
    {
        if (!playerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Messages.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        //TODO last seen on which server

        String partnerName = playerManager.getPartner(sender.getName());
        Player partner = Marriage.plugin.getPlayer(partnerName);
        if (partner != null )
        {
            Messages.sendMessage(sender, "&e" + partnerName + "&e is &aonline");
        }else{

            long timeDiff = (System.currentTimeMillis() - playerManager.getLastOnline(partnerName)) / 1000;
            Messages.sendMessage(sender, Messages.lastSeen.replace("{time}", Messages.timeformat(timeDiff)));
        }
    }

    @Command(command = "partner", trigger = "chat", args = {}, playersOnly = true, permission = Permissions.partnerChat, desc = "Chat privately with your partner", usage = "/partner chat", unlockRequired = Permissions.unlockCommandChat)
    public void chat(CommandSender sender)
    {
        if (!playerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Messages.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        if (playerManager.isPartnerChatOn(sender.getName()))
        {
            playerManager.setPartnerChat(sender.getName(), false);
            Messages.sendMessage(sender, "&dReturning to normal chat");
        }else
        {
            Player partner = Marriage.plugin.getPlayer(playerManager.getPartner(sender.getName()));
            if (partner == null)
                Messages.sendMessage(sender, Messages.notOnline.replace("{player}", playerManager.getPartner(sender.getName())));
            else
            {
                playerManager.setPartnerChat(sender.getName(), true);
                Messages.sendMessage(sender, "&dYou are now privately chatting with " + partner.getDisplayName());
            }
        }
    }

    @Command(command = "partner", trigger = "tp", args = {}, playersOnly = true, permission = Permissions.partnerTp, desc = "Teleport to your partner", usage = "/partner tp", unlockRequired = Permissions.unlockCommandTp)
    public void teleportToPartner(final Player sender)
    {
        if (!playerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Messages.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        //TODO check for pvp stuff
        final String partnerName = playerManager.getPartner(sender.getName());
        Player partner = Marriage.plugin.getPlayer(partnerName);
        if (partner == null)
        {
            Messages.sendMessage(sender, Messages.notOnline.replace("{player}", partnerName));
            return;
        }

        teleport(sender, partner);
    }

    @Command(command = "partner", trigger = "sethome", args = {}, playersOnly = true, permission = Permissions.partnerHome, desc = "Set the home location", usage = "/partner sethome", unlockRequired = Permissions.unlockCommandHome)
    public void setHome(Player sender)
    {
        if (!playerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Messages.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        Location l = sender.getLocation();
        this.playerManager.setHome(sender.getName(), l);
        Messages.sendMessage(sender, "&aYour home has been set!");
        Player partner = Marriage.plugin.getPlayer(this.playerManager.getPartner(sender.getName()));
        if (partner != null)
            Messages.sendMessage(partner, "&aYour home has been changed by your partner!");

    }

    @Command(command = "partner", trigger = "home", args = {}, playersOnly = true, permission = Permissions.partnerHome, desc = "Go to your home location", usage = "/partner home", unlockRequired = Permissions.unlockCommandHome)
    public void goHome(Player sender)
    {
        if (!playerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Messages.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        if (!playerManager.isHomeSet(sender.getName()))
        {
            Messages.sendMessage(sender, Messages.noHomeSet);
            return;
        }

        Location l = playerManager.getHomeLoc(sender.getName());
        teleport(sender, l);
    }

    @Beta
    @Command(command = "partner", trigger = "chest", args = {}, playersOnly = true, permission = Permissions.partnerInventory, desc = "Open a shared inventory", usage = "/partner chest", unlockRequired = Permissions.unlockCommandChest)
    public void openChest(Player sender)
    {
        if (!playerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Messages.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        String partner = playerManager.getPartner(sender.getName());
        if (playerManager.isSharedInvOpen(partner))
        {
            Messages.sendMessage(sender, Messages.invAlreadyOpen);
            return;
        }

        //TODO
    }

    @Command(command = "partner", trigger = "deposit", args = {"{int}"}, playersOnly = true, permission = Permissions.partnerMoney, desc = "Add money to the shared bank", usage = "/partner deposit <money>")
    public void moneyAdd(Player sender, String moneyString)
    {
        if (!playerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Messages.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        double money = 0;
        try {
            money = roundMoney(Double.parseDouble(moneyString));
        } catch (NumberFormatException e) {
            Messages.sendMessage(sender, "&c/partner deposit <money>");
            return;
        }

        if (money == 0)
            return;

        double balance = Marriage.eco.getBalance(sender);
        if (money > balance)
        {
            Messages.sendMessage(sender, Messages.notEnoughMoney);
            return;
        }

        Marriage.eco.withdrawPlayer(sender, money);
        playerManager.setBalance(sender.getName(), playerManager.getBalance(sender.getName()) + money);

        Messages.sendMessage(sender, "&eYou deposited &6$" + money + "&e in your shared bank");
        Player partner = Marriage.plugin.getPlayer(playerManager.getPartner(sender.getName()));
        if (partner != null)
            Messages.sendMessage(partner, "&e" + sender.getDisplayName() + "&e deposited &6$" + money + "&e in your shared bank");
    }

    @Command(command = "partner", trigger = "withdraw", args = {"{int}"}, playersOnly = true, permission = Permissions.partnerMoney, desc = "Take money from the shared bank", usage = "/partner withdraw <money>")
    public void moneyRemove(Player sender, String moneyString)
    {
        if (!playerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Messages.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        double money = 0;
        try {
            money = roundMoney(Double.parseDouble(moneyString));
        } catch (NumberFormatException e) {
            Messages.sendMessage(sender, "&c/partner withdraw <money>");
            return;
        }

        if (money == 0)
            return;

        double sharedBalance = playerManager.getBalance(sender.getName());

        if (money > sharedBalance)
        {
            Messages.sendMessage(sender, Messages.notEnoughMoney);
            return;
        }

        Marriage.eco.depositPlayer(sender, money);
        playerManager.setBalance(sender.getName(), playerManager.getBalance(sender.getName()) - money);

        Messages.sendMessage(sender, "&eYou withdrew &6$" + money + "&e from your shared bank");
        Player partner = Marriage.plugin.getPlayer(playerManager.getPartner(sender.getName()));
        if (partner != null)
            Messages.sendMessage(partner, "&e" + sender.getDisplayName() + "&e withdrew &6$" + money + "&e from your shared bank");
    }

    @Command(command = "partner", trigger = "balance", args = {}, playersOnly = true, permission = Permissions.partnerMoney, desc = "Check the balance of the shared bank", usage = "/partner balance")
    public void money(CommandSender sender)
    {
        if (!playerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Messages.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        double sharedBalance = playerManager.getBalance(sender.getName());

        Messages.sendMessage(sender, "&eCurrent balance: $" + sharedBalance);
    }

    @Command(command = "partner", trigger = "perks", args = {}, playersOnly = true, permission = Permissions.partnerMoney, desc = "Passive perks to unlock", usage = "/partner perks")
    public void passivePerks(Player sender)
    {
        if (!playerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Messages.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        Map<String, Integer> perks = new HashMap<String, Integer>();
        if (Marriage.config.getBoolean("smite-on-partner-dead"))
            perks.put(ChatColor.YELLOW + "Don't get smited when your partner dies", Marriage.config.getInt(Permissions.unlockPerkNoSmite));

        perks.put(ChatColor.YELLOW + "Chat prefix", Marriage.config.getInt(Permissions.unlockPerkPrefix));
        perks.put(ChatColor.YELLOW + "Heart effects when walking near each other", Marriage.config.getInt(Permissions.unlockPerkHearts));
        perks.put(ChatColor.YELLOW + "Receive a notification when your partner logs in", Marriage.config.getInt(Permissions.unlockPerkLoginMessage));
        perks.put(ChatColor.YELLOW + "Cool teleporting effect", Marriage.config.getInt(Permissions.unlockPerkTeleportEffect));

        int invSize = perks.size()/9;
        if (perks.size()%9 != 0)
            invSize = invSize+9;

        List<String> sortedPerks = Messages.sortMapByValue(perks);
        Inventory inv = Bukkit.createInventory(null, invSize, Messages.perkInvName);
        for (int i = 0; i < sortedPerks.size(); i++)
        {
            ItemStack item = new ItemStack(Material.WOOL);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<String>();

            if (Permissions.hasMoney(sender, perks.get(sortedPerks.get(i))))
            {
                lore.add(ChatColor.GREEN + "Unlocked ($" + perks.get(sortedPerks.get(i)) + ")");
                item.setDurability((short) 5);
            }else{
                lore.add(ChatColor.RED + "Shared balance required: $" + perks.get(sortedPerks.get(i)));
                item.setDurability((short) 14);
            }

            lore.add(ChatColor.GOLD + "Current balance: $" + Marriage.plugin.getPlayerManager().getBalance(sender.getName()));
            meta.setLore(lore);
            meta.setDisplayName(sortedPerks.get(i));
            item.setItemMeta(meta);

            inv.addItem(item);
        }

        sender.openInventory(inv);
    }

    @Default("1")
    @Command(command = "partner", trigger = "help", args = {"{int}"}, playersOnly = true, permission = Permissions.partnerMoney, helpHidden = true)
    public void help(CommandSender sender, String page)
    {
        if (!playerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Messages.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        int pageNr;
        try {
            pageNr = Integer.parseInt(page);
        } catch (NumberFormatException e) {
            pageNr = 1;
            return;
        }

        CommandHandler.getCommandHandler().showHelp("partner", sender, pageNr);
    }

    private double roundMoney(double value)
    {
        long factor = (long) Math.pow(10, 2);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    private void teleport(final Player sender, Player receiver)
    {
        try {
            preTeleport(sender);
        } catch (IllegalArgumentException e) {
            Messages.sendMessage(sender, "&c"+e.getMessage());
            return;
        }
        if (Marriage.plugin.isPluginEnabled(Messages.effectsLibPluginName) && Permissions.hasMoney(sender, Permissions.unlockCommandTp))
        {
            final String receiverName = receiver.getName();
            Messages.sendMessage(sender, "&ePrepairing to teleport!");

            AnimatedBallEffect effect = new AnimatedBallEffect(Marriage.plugin.effectManager);
            effect.iterations = 75;
            effect.particle = ParticleEffect.WITCH_MAGIC;
            effect.setEntity(sender);
            effect.yOffset = -0F;
            effect.callback = new Runnable() {
                @Override
                public void run()
                {
                    Player partner = Marriage.plugin.getPlayer(receiverName);
                    Location location = partner.getLocation();
                    sender.teleport(location);
                    Messages.sendMessage(sender, "&eTeleported to " + partner.getDisplayName());
                    Messages.sendMessage(partner, "&e" + sender.getDisplayName() + " &eteleported to you");
                }
            };
            effect.start();
        }else {
            sender.teleport(receiver);
            Messages.sendMessage(sender, "&eTeleported to " + receiver.getDisplayName());
            Messages.sendMessage(receiver, "&e" + sender.getDisplayName() + " &eteleported to you");
        }


    }

    private void teleport(final Player sender, final Location loc)
    {
        try {
            preTeleport(sender);
        } catch (IllegalArgumentException e) {
            Messages.sendMessage(sender, "&c"+e.getMessage());
            return;
        }
        if (Marriage.plugin.isPluginEnabled(Messages.effectsLibPluginName) && Permissions.hasMoney(sender, Permissions.unlockCommandTp))
        {
            Messages.sendMessage(sender, "&ePrepairing to teleport!");

            AnimatedBallEffect effect = new AnimatedBallEffect(Marriage.plugin.effectManager);
            effect.iterations = 75;
            effect.particle = ParticleEffect.WITCH_MAGIC;
            effect.setEntity(sender);
            effect.yOffset = -0F;
            effect.callback = new Runnable() {
                @Override
                public void run()
                {
                    sender.teleport(loc);
                    Messages.sendMessage(sender, "&eTeleported to your home");
                }
            };
            effect.start();
        }else {
            sender.teleport(loc);
            Messages.sendMessage(sender, "&eTeleported to your home");
        }


    }

    private boolean preTeleport(Player sender)
    {
        //Check if essentials jailed
        if (Marriage.plugin.isPluginEnabled(Messages.essentialsPluginName))
        {
            Essentials essentials = (Essentials) Marriage.plugin.getServer().getPluginManager().getPlugin(Messages.essentialsPluginName);
            if (essentials.getUser(sender).isJailed())
                throw new IllegalArgumentException("You can not teleport while jailed!");
            essentials.getUser(sender).setLastLocation();
        }

        //Check for WorldGuard pvp
        if (Marriage.plugin.isPluginEnabled(Messages.worldGuardPluginName) && Marriage.plugin.isPluginEnabled(Messages.worldEditPluginName))
        {
            WorldGuardPlugin wg = (WorldGuardPlugin) Marriage.plugin.getServer().getPluginManager().getPlugin(Messages.worldGuardPluginName);
            LocalPlayer p = wg.wrapPlayer(sender);
            ApplicableRegionSet regions =  wg.getRegionManager(sender.getWorld()).getApplicableRegions(p.getPosition());
            if (regions.allows(DefaultFlag.PVP, p))
                throw new IllegalArgumentException("You can not teleport in a PVP area!");
        }

        return true;
    }
}
