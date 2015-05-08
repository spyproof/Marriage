package be.spyproof.marriage;

/**
 * Created by Spyproof on 8/05/2015.
 */
public class Messages
{
    public static String prefix = Marriage.plugin.getConfig().getString("broadcast-prefix");
    public static String broadcast = "{prefix} &5{player1}&d is now married to &5{player2}";
    public static String privateMessage = "&5{sender}&5 -> &5{receiver}&5:&d {message}";
    public static String playerOnly = "&cThis command can only be used by players";
    public static String alreadyMarried = "&cYou are already married!\nTry to talk to each other before using /marry divorce";
    public static String alreadyMarriedOther = "&c{player} is already married!";
    public static String notMarried = "&cYou are not married!";
    public static String notMarriedToPlayer = "&cYou need to be married to another person to use this!";
    public static String notOnline = "&c{player}&c is not online";
    public static String noProposal = "&cNo one has send a proposal to you yet";
    public static String noPermission = "&cYou do not have permission to use this command";
    public static String divorce = "&5You are now divorced";
    public static String partnerJoined = "&aYour partner &e{player}&a came online";
    public static String partnerDC = "&aYour partner &e{player}&a logged off";
}
