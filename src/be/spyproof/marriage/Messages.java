package be.spyproof.marriage;

/**
 * Created by Spyproof on 8/05/2015.
 */
public class Messages
{
    public static String prefix = Marriage.plugin.getConfig().getString("broadcast-prefix");
    public static String prefixMarried = "\u2665";
    public static String prefixRightHand = "\u2666";
    public static String prefixLeftHand = "\u2666";
    public static final String broadcast = "{prefix} &5{player1}&d is now married to &5{player2}";
    public static final String privateMessage = "&5{sender}&5:&d {message}";
    public static final String privateMessageSocialspy = "&5{sender}&5 -> &5{receiver}&5:&d {message}";
    public static final String playerOnly = "&cThis command can only be used by players";
    public static final String alreadyMarried = "&cYou are already married!\nTry to talk to each other before using /marry divorce";
    public static final String alreadyMarriedOther = "&c{player} is already married!";
    public static final String notMarried = "&cYou are not married!";
    public static final String notMarriedToPlayer = "&cYou need to be married to another person to use this!";
    public static final String notOnline = "&c{player}&c is not online";
    public static final String noProposal = "&cNo one has send a proposal to you yet";
    public static final String noPermission = "&cYou do not have permission to use this command";
    public static final String divorce = "&5You are now divorced";
    public static final String partnerJoined = "&aYour partner &e{player}&a came online";
    public static final String partnerDC = "&aYour partner &e{player}&a logged off";
    public static final String lastSeen = "&eYour partner was last seen &6{hours}h {minutes}m {seconds}s&e ago";
    public static final String lastSeenOver1Day = "&eYour partner was last seen &6{days}d {hours}h&e ago";
    public static final String lastSeenOver7Days = "&eYour partner was last seen &6{days} days&e ago";
    public static final String betaCommand = "&b&oThis command is in testing fase! Please report all bugs!";
}
