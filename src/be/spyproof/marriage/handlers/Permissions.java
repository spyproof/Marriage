package be.spyproof.marriage.handlers;

import be.spyproof.marriage.Marriage;
import org.bukkit.command.CommandSender;

/**
 * Created by Spyproof on 9/05/2015.
 */
public class Permissions
{
    public static final String adminReload = "marriage.admin.reload";
    public static final String adminSave = "marriage.admin.save";
    public static final String adminRemove = "marriage.admin.remove";
    public static final String adminInfo = "marriage.admin.info";
    public static final String adminPlugin = "marriage.admin.plugin";
    public static final String adminSocialSpy = "marriage.admin.socialspy";
    public static final String adminResetCooldown = "marriage.admin.resetcooldown";

    public static final String playerMarryOther = "marriage.player.marry.other";
    public static final String playerMarrySelf = "marriage.player.marry.self";
    public static final String playerDivorce = "marriage.player.divorce";
    public static final String playerGender = "marriage.player.gender";
    public static final String playerInfo = "marriage.player.info";
    public static final String partnerInfo = "marriage.player.partner.info";
    public static final String partnerSeen = "marriage.player.partner.seen";
    public static final String partnerChat = "marriage.player.partner.chat";
    public static final String partnerTp = "marriage.player.partner.tp";
    public static final String partnerHome = "marriage.player.partner.home";
    public static final String partnerInventory = "marriage.player.partner.inventory";
    public static final String partnerMoney = "marriage.player.partner.money";

    public static final String unlockCommandSeen = "unlock.command.seen";
    public static final String unlockCommandInfo = "unlock.command.info";
    public static final String unlockCommandChat = "unlock.command.chat";
    public static final String unlockCommandTp = "unlock.command.tp";
    public static final String unlockCommandHome = "unlock.command.home";
    public static final String unlockCommandChest = "unlock.command.chest";
    public static final String unlockCommandInv = "unlock.command.inventory";

    public static final String unlockPerkPrefix = "unlock.perk.prefix";
    public static final String unlockPerkLoginMessage  = "unlock.perk.login-message";
    public static final String unlockPerkTeleportEffect = "unlock.perk.teleport-effects";
    public static final String unlockPerkNoSmite = "unlock.perk.no-smite-on-partner-dead";
    public static final String unlockPerkHearts = "unlock.perk.hearts";

    public static final String bypassCooldown = "marriage.perk.bypasscooldowns";
    public static final String bypassMarriageCosts = "marriage.perk.bypassmarriagecosts";
    public static final String bypassCommandCosts = "marriage.perk.bypasscommandcosts";

    //Check for player permission
    public static boolean hasPerm(CommandSender sender, String perm) //TODO handle -i.am.a.perm
    {
        if (sender.hasPermission(perm) || perm.equalsIgnoreCase("none") || sender.isOp())
            return true;
        else
        {
            boolean go = perm.contains(".");
            while (go)
            {
                perm = perm.replaceAll("\\.\\*$", "");
                perm = perm.replaceAll("\\.\\w*$", ".*");
                if (!perm.contains("."))
                    go = false;
                if (sender.hasPermission(perm) && go)
                    return true;
            }
            return false;
        }
    }

    public static boolean hasMoney(CommandSender sender, String money)
    {
        return hasMoney(sender, Marriage.config.getInt(money));
    }

    public static boolean hasMoney(CommandSender sender, int money)
    {
        if (Permissions.hasPerm(sender, Permissions.bypassCommandCosts))
            return true;
        boolean hasMoney = Marriage.plugin.getPlayerManager().getBalance(sender.getName()) >= money;
        if (!hasMoney)
            Messages.sendDebugInfo("&cNot have enough money");
        return hasMoney;
    }
}
