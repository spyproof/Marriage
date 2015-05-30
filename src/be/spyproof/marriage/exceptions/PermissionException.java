package be.spyproof.marriage.exceptions;

import be.spyproof.marriage.handlers.Messages;

/**
 * Created by Spyproof on 29/05/2015.
 */
public class PermissionException extends Exception
{
    private boolean hasPermission;
    private boolean isUnlocked;
    private int money;

    public PermissionException(boolean hasPermission, boolean isUnlocked, int money)
    {
        this.hasPermission = hasPermission;
        this.isUnlocked = isUnlocked;
        this.money = money;
    }

    @Override
    public String getMessage()
    {
        if (!hasPermission)
            return Messages.noPermission;
        else if (!isUnlocked)
            return Messages.sharedMoneyNeeded.replace("{money}", money + "");
        else
            return super.getMessage();
    }

    public boolean hasPermission()
    {
        return hasPermission;
    }

    public boolean isUnlocked()
    {
        return isUnlocked;
    }

    public int getMoney()
    {
        return money;
    }
}
