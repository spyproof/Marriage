package be.spyproof.marriage;

/**
 * Created by Spyproof on 8/05/2015.
 */
public class Messages
{
	public static String prefix = Marriage.config.getString("broadcast-prefix");
	public static String prefixMarried = "\u2665";
	public static String prefixRightHand = "\u2666";
	public static String prefixLeftHand = "\u2666";
	public static final String broadcast = "{prefix} &5{player1}&d is now married to &5{player2}";
	public static final String privateMessage = "&6{sender} &5\u2665&e {message}";
	public static final String privateMessageSocialspy = "&5{sender}&5 -> &5{receiver}&5:&d {message}";
	public static final String playerOnly = "&cThis command can only be used by players";
	public static final String alreadyMarried = "&cYou are already married!\nTry to talk to each other before using /marry divorce";
	public static final String alreadyMarriedOther = "&c{player} is already married!";
	public static final String notMarried = "&cYou are not married!";
	public static final String notMarriedToPlayer = "&cYou need to be married to another person to use this!";
	public static final String notOnline = "&c{player}&c is not online";
	public static final String noPermission = "&cYou do not have permission to use this command";
    public static final String noProposal = "&cNo one has send a proposal to you yet";
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
    public static final String notEnoughSharedMoney = "&cYou need to have ${money} in your shared bank to use this command!";
    public static final String sharedMoneyNeeded = "&1You need ${money} in your shared bank";

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
}
