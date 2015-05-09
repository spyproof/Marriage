package be.spyproof.marriage.commands;

import be.spyproof.marriage.*;
import be.spyproof.marriage.annotations.Beta;
import be.spyproof.marriage.annotations.Command;
import be.spyproof.marriage.annotations.Default;
import be.spyproof.marriage.datamanager.PlayerManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

import java.util.Date;

/**
 * Created by Spyproof on 5/05/2015.
 * Command handling inspired by https://github.com/Zenith-
 */
public class CommandPartner
{
    @Command(command = "partner", trigger = "info", args = {}, playersOnly = true, permission = Permissions.partnerInfo, desc = "Check on your partner", usage = "/partner info")
    public void getPartnerName(CommandSender sender)
    {
        Status status = PlayerManager.getStatus(sender.getName());

        if (status.equals(Status.MARRIED_TO_PERSON))
        {
            String partnerName = PlayerManager.getPartner(sender.getName());
            Player partner = Marriage.getPlayer(partnerName);
            Marriage.sendMessage(sender, "&eYou are married to &6" + partnerName + "&e and is " + (partner == null ? "&cfffline" : "&aonline"));
            //TODO home location + balance
            Marriage.sendMessage(sender, "&eYou are &6" + (PlayerManager.trustsPartner(partnerName) ? "allowed" : "not allowed") + "&e to open " + (PlayerManager.getGender(partnerName).equals(Gender.FEMALE) ? "her" : "his") + " inventory");
        }
        else if (status.equals(Status.MARRIED_TO_LEFT_HAND))
            Marriage.sendMessage(sender, "&eYou are married to your left hand");
        else if (status.equals(Status.MARRIED_TO_RIGHT_HAND))
            Marriage.sendMessage(sender, "&eYou are married to your right hand");
        else
            Marriage.sendMessage(sender, Messages.notMarried);
    }

    @Beta
    @Command(command = "partner", trigger = "seen", args = {}, playersOnly = true, permission =  Permissions.partnerSeen, desc = "Last time your partner was online", usage = "/partner seen")
    public void lastSeenPartner(CommandSender sender)
    {
        if (!PlayerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Marriage.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        String partnerName = PlayerManager.getPartner(sender.getName());
        Player partner = Marriage.getPlayer(partnerName);
        if (partner != null )
        {
            Marriage.sendMessage(sender, "&e" + partnerName + "&e is &aonline");
        }else{
            long timeDiff = (System.currentTimeMillis() - PlayerManager.getLastOnline(partnerName)) / 1000;
            int seconds = (int)timeDiff%60;
            timeDiff = timeDiff / 60;
            int minutes = (int)timeDiff%60;
            timeDiff = timeDiff / 60;
            int hours = (int)timeDiff%24;
            timeDiff = timeDiff / 24;
            int days = (int)timeDiff;

            //Last seen formatting
            if (days > 7)
            {
                String message = Messages.lastSeenOver7Days.replace("{days}", days + "");
                Marriage.sendMessage(sender, message);
            }
            else if (days > 0)
            {
                String message = Messages.lastSeenOver1Day.replace("{days}", days + "").replace("{hours}", hours + "");
                Marriage.sendMessage(sender, message);
            }else{
                String message = Messages.lastSeen.replace("{hours}", hours + "").replace("{minutes}", minutes + "")
                        .replace("{seconds}", seconds + "");
                Marriage.sendMessage(sender, message);
            }
        }
    }

    @Command(command = "partner", trigger = "chat", args = {}, playersOnly = true, permission =  Permissions.partnerChat, desc = "Chat privately with your partner", usage = "/partner chat")
    public void chat(CommandSender sender)
    {
        if (!PlayerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Marriage.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        if (PlayerManager.isPartnerChatOn(sender.getName()))
        {
            PlayerManager.setPartnerChat(sender.getName(), false);
            Marriage.sendMessage(sender, "&dReturning to normal chat");
        }else
        {
            Player partner = Marriage.getPlayer(PlayerManager.getPartner(sender.getName()));
            if (partner == null)
                Marriage.sendMessage(sender, Messages.notOnline.replace("{player}", PlayerManager.getPartner(sender.getName())));
            else
            {
                PlayerManager.setPartnerChat(sender.getName(), true);
                Marriage.sendMessage(sender, "&dYou are now privately chatting with " + partner.getDisplayName());
            }
        }
    }

    @Command(command = "partner", trigger = "tp", args = {}, playersOnly = true, permission =  Permissions.partnerTp, desc = "Teleport to your partner", usage = "/partner tp")
    public void teleport(Player sender)
    {
        if (!PlayerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Marriage.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        //TODO check for pvp stuff
        String partnerName = PlayerManager.getPartner(sender.getName());
        Player partner = Marriage.getPlayer(partnerName);
        if (partner == null)
        {
            Marriage.sendMessage(sender, Messages.notOnline.replace("{player}", partnerName));
            return;
        }

        Location location = partner.getLocation();
        sender.teleport(location);
        Marriage.sendMessage(sender, "&eTeleported to " + partner.getDisplayName());
        Marriage.sendMessage(partner, "&e" + sender.getDisplayName() + " &eteleported to you");
    }

    @Beta
    @Command(command = "partner", trigger = "sethome", args = {}, playersOnly = true, permission =  Permissions.partnerHome, desc = "Set the home location", usage = "/partner sethome")
    public void setHome(Player sender)
    {
        if (!PlayerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Marriage.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        Location l = sender.getLocation();
        PlayerManager.setHome(sender.getName(), l);
        Marriage.sendMessage(sender, "&1Not implemented yet");
    }

    @Beta
    @Command(command = "partner", trigger = "home", args = {}, playersOnly = true, permission = Permissions.partnerHome, desc = "Go to your home location", usage = "/partner home")
    public void goHome(CommandSender sender)
    {
        if (!PlayerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Marriage.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        Marriage.sendMessage(sender, "&1Not implemented yet");
    }

    @Beta
    @Command(command = "partner", trigger = "inv", args = {}, playersOnly = true, permission = Permissions.partnerInventory, desc = "Open your partner's inventory", usage = "/partner inv")
    public void openInventory(CommandSender sender)
    {
        if (!PlayerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Marriage.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        Marriage.sendMessage(sender, "&1Not implemented yet");
    }

    @Command(command = "partner", trigger = "trustinv", args = {}, playersOnly = true, permission = Permissions.partnerInventory, desc = "Let's your partner open your inventory", usage = "/partner trustinv")
    public void trustInventory(CommandSender sender)
    {
        if (!PlayerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
        {
            Marriage.sendMessage(sender, Messages.notMarriedToPlayer);
            return;
        }

        boolean trustsPartner = PlayerManager.trustsPartner(sender.getName());
        if (trustsPartner)
        {
            PlayerManager.setTrustsPartner(sender.getName(), false);
            Marriage.sendMessage(sender, "&bYou no longer allow your partner to open your inventory");
        }else
        {
            PlayerManager.setTrustsPartner(sender.getName(), true);
            Marriage.sendMessage(sender, "&bYou allow your partner to open your inventory!\n" +
                    "&lYour partner can take anything! \n" +
                    "&6&lThey can take something even when you told them not to!\n" +
                    "&bUse &3&l/partner trustinv&b to undo this");
        }
    }

    @Beta
    @Default({"1"})
    @Command(command = "partner", trigger = "help", args = {"{int}"}, playersOnly = true, helpHidden = true)
    public void help(CommandSender sender, String pageNr)
    {
        int page;
        try {
            page = Integer.parseInt(pageNr);
        } catch (NumberFormatException e) {
            page = 1;
        }

        CommandHandler.getCommandHandler().showHelp("partner", sender, page);
    }
}
