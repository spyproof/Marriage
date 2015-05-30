package be.spyproof.marriage.handlers;

import be.spyproof.marriage.Marriage;
import be.spyproof.marriage.exceptions.PermissionException;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Spyproof on 9/05/2015.
 */
public class Permissions
{
    /**
     * Permissions
     */
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
    public static final String partnerChest = "marriage.player.partner.chest";
    public static final String partnerTp = "marriage.player.partner.tp";
    public static final String partnerHome = "marriage.player.partner.home";
    public static final String partnerInventory = "marriage.player.partner.inventory";
    public static final String partnerMoney = "marriage.player.partner.money";

    public static final String perkPrefix = "marriage.player.perk.prefix";
    public static final String perkLoginMessage  = "marriage.player.perk.login-message";
    public static final String perkTeleportEffect = "marriage.player.perk.teleport-effects";
    public static final String perkNoSmite = "marriage.player.perk.no-smite-on-partner-dead";
    public static final String perkHearts = "marriage.player.perk.hearts";

    public static final String bypassCooldown = "marriage.bypass.cooldowns";
    public static final String bypassMarriageCosts = "marriage.bypass.marriagecosts";
    public static final String bypassCommandCosts = "marriage.bypass.commandcosts";

    private static Map<String, Integer> unlockCosts = new HashMap<String, Integer>(); //Permission + unlockCost
    private static Permission permission = null;

    public Permissions()
    {
        setupPermissions();
        unlockCosts.put(partnerChat, Marriage.config.getInt("unlock.command.chat"));
        unlockCosts.put(partnerChest, Marriage.config.getInt("unlock.command.chest"));
        unlockCosts.put(partnerHome, Marriage.config.getInt("unlock.command.home"));
        unlockCosts.put(partnerInfo, Marriage.config.getInt("unlock.command.info"));
        unlockCosts.put(partnerInventory, Marriage.config.getInt("unlock.command.inventory"));
        unlockCosts.put(partnerSeen, Marriage.config.getInt("unlock.command.seen"));
        unlockCosts.put(partnerTp, Marriage.config.getInt("unlock.command.yp"));
        unlockCosts.put(perkHearts, Marriage.config.getInt("unlock.perk.hearts"));
        unlockCosts.put(perkLoginMessage, Marriage.config.getInt("unlock.perk.login-message"));
        unlockCosts.put(perkNoSmite, Marriage.config.getInt("unlock.perk.no-smite-on-partner-dead"));
        unlockCosts.put(perkPrefix, Marriage.config.getInt("unlock.perk.prefix"));
        unlockCosts.put(perkTeleportEffect, Marriage.config.getInt("unlock.perk.teleport-effects"));
    }

    public static boolean hasPerm(CommandSender sender, String perm) throws PermissionException
    {
        if (sender.isOp() || perm.equalsIgnoreCase("none"))
            return true;

        boolean hasMoney = hasMoney(sender, perm);
        boolean hasPerm = permission.has(sender, perm);
        int money = 0;
        if (unlockCosts.containsKey(perm))
            money = unlockCosts.get(perm);

        if (!hasMoney || !hasPerm)
            throw new PermissionException(hasPerm, hasMoney, money);

        return true;
    }

    private static boolean hasMoney(CommandSender sender, String perm)
    {
        //Check if player bypasses the cost
        if (permission.has(sender, Permissions.bypassCommandCosts))
            return true;

        //Check if you need money for this permission
        if (!unlockCosts.containsKey(perm))
            return true;

        //Check if player has the money
        return Marriage.plugin.getPlayerManager().getBalance(sender.getName()) >= unlockCosts.get(perm);
    }

    public static int unlockCost(String perm)
    {
        if (unlockCosts.containsKey(perm))
            return unlockCosts.get(perm);
        else
            return 0;
    }

    private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = Marriage.plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }
}
