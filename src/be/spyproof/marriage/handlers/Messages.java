package be.spyproof.marriage.handlers;

import be.spyproof.marriage.Marriage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Spyproof on 8/05/2015.
 */
public class Messages
{
	public static String prefix;
	public static String prefixMarried = "&f[&d\u2665&f]";
	public static String prefixRightHand = "&f[&d\u2666&f]";
	public static String prefixLeftHand = "&f[&d\u2666&f]";
	public static final String broadcast = "{prefix} &5{player1}&d is now married to &5{player2}";
	public static final String privateMessage = "&6{sender} &5\u2665&e {message}";
	public static final String privateMessageSocialspy = "&r{sender}&r -> &r{receiver}&r: {message}";
	public static final String playerOnly = "&cThis command can only be used by players";
	public static final String alreadyMarried = "&cYou are already married!\nTry to talk to each other before using /marry divorce";
	public static final String alreadyMarriedOther = "&c{player} is already married!";
	public static final String notMarried = "&cYou are not married!";
	public static final String notMarriedToPlayer = "&cYou need to be married to another person to use this!";
	public static final String notOnline = "&c{player}&c is not online";
	public static final String noPermission = "&cYou do not have permission to use this command";
    public static final String noProposal = "&cNo one has send a proposal to you yet";
    public static final String noHomeSet = "&cYou do not have a home set";
    public static final String proposalExpired = "&cThe last proposal has expired";
    public static final String activeProposal = "&c{player} is already has an open proposal";
	public static final String divorce = "&5You are now divorced";
	public static final String partnerJoined = "&a&e{player}&a came online";
	public static final String partnerDC = "&a&e{player}&a logged off";
	public static final String lastSeen = "&eYour partner was last seen &6{time}&e ago";
	public static final String betaCommand = "&b&oThis command is in testing fase! Please report all bugs!";
	public static final String onCooldown = "&cYou need to wait &e{time}&c before using this command!";
    public static final String notEnoughMoney = "&cYou do not have enough money!";
    public static final String notEnoughMoneyPartner = "&c{player} &cdoes not have enough money!";
    public static final String sharedMoneyNeeded = "&1You need ${money} in your shared bank";
    public static final String invAlreadyOpen = "&eYour partner already has the inventory open!";
    public static final String muted = "&cYou are muted!";
    public static final String perkInvName = ChatColor.BLUE + "Passive perks";

    public static final String banManagerPluginName = "BanManager";
    public static final String effectsLibPluginName = "EffectLib";
    public static final String essentialsPluginName = "Essentials";
    public static final String heroChatPluginName = "Herochat";
    public static final String vaultPluginName = "Vault";
    public static final String worldGuardPluginName = "WorldGuard";
    public static final String worldEditPluginName = "WorldEdit";

    private static List<String> debuggers = new ArrayList<String>();

    public Messages()
    {
        prefix = Marriage.config.getString("broadcast-prefix");
    }

    /**
     *
     * @param timeDiff Time difference in seconds
     * @return String: time in a format
     */
    public static String timeformat(long timeDiff)
    {
        int seconds = (int)timeDiff%60;
        timeDiff = timeDiff / 60;
        int minutes = (int)timeDiff%60;
        timeDiff = timeDiff / 60;
        int hours = (int)timeDiff%24;
        timeDiff = timeDiff / 24;
        int days = (int)timeDiff;

        String timeFormat;

        //Formatting
        if (days > 7)
            timeFormat = days + " days";
        else if (days > 0)
            timeFormat = days + "d " + hours + "h";
        else if (days == 0 && hours > 0){
            timeFormat = hours + "h " + minutes + "m " + seconds + "s";
        }else if (days == 0 && hours == 0 && minutes > 0){
            timeFormat = minutes + "m " + seconds + "s";
        }else{
            timeFormat = seconds + "s";
        }

            return timeFormat;
    }

    public static List sortMapByValue(Map<Object, Integer> map)
    {
        List<Object> sortedList = new ArrayList<Object>();
        int lastCost = -1;
        int currentCost = Integer.MAX_VALUE;
        Object objToAdd = "";
        while (sortedList.size() != map.size())
        {
            for (Object helpLine : map.keySet())
                if (!sortedList.contains(helpLine) && currentCost >= map.get(helpLine) && lastCost <= map.get(helpLine))
                {
                    objToAdd = helpLine;
                    currentCost = map.get(helpLine);
                }
            lastCost = map.get(objToAdd);
            sortedList.add(objToAdd);
            currentCost = Integer.MAX_VALUE;
            objToAdd = "";
        }

        return sortedList;
    }

    public static void sendMessage(CommandSender sender, String message)
    {
        message = message.replace("\\n", "\n").replace("{prefix}", Messages.prefix);

        if (sender instanceof Player)
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        else
            Marriage.plugin.getServer().getLogger().info(message.replaceAll("&[0-9a-fA-Fk-oK-OrR]", ""));
    }

    public static void sendMessage(String sender, String message)
    {
        Player p = Marriage.plugin.getPlayer(sender);
        if (p != null)
            sendMessage(p, message);
    }

    public static void sendDebugInfo(String message)
    {
        String prefix = "&e[&a&lDebug&e] &3";
        for (String p : debuggers)
        {
            CommandSender sender = null;
            if (p.equals("CONSOLE"))
                sender = Marriage.plugin.getServer().getConsoleSender();
            else
                sender = Marriage.plugin.getPlayer(p);

            if (sender != null)
                sendMessage(sender, prefix + message.replace("\n", "\n"+prefix));
        }
    }

    public static void toggleDebugger(String name)
    {
        if (debuggers.contains(name))
            debuggers.remove(name);
        else
            debuggers.add(name);
    }
}
