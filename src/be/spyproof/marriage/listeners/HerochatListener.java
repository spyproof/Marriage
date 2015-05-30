package be.spyproof.marriage.listeners;

import be.spyproof.marriage.Marriage;
import be.spyproof.marriage.Status;
import be.spyproof.marriage.exceptions.PermissionException;
import be.spyproof.marriage.handlers.Messages;
import be.spyproof.marriage.handlers.Permissions;
import com.dthielke.herochat.ChannelChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by Spyproof on 30/05/2015.
 */
public class HerochatListener implements Listener
{
    @EventHandler
    public void onHeroChat(ChannelChatEvent e)
    {
        //Handle the prefix
        try {
            Permissions.hasPerm(e.getSender().getPlayer(), Permissions.perkPrefix);
        } catch (PermissionException e1)
        {
            e.setFormat(e.getFormat().replace("{marry}", ""));
            return;
        }

        Status status = Marriage.plugin.getPlayerManager().getStatus(e.getSender().getName());
        String prefix = null;

        if (status.equals(Status.MARRIED_TO_PERSON))
            prefix = "&r" + Messages.prefixMarried + " &r";
        else if (status.equals(Status.MARRIED_TO_LEFT_HAND))
            prefix = "&r" + Messages.prefixLeftHand + " &r";
        else if (status.equals(Status.MARRIED_TO_RIGHT_HAND))
            prefix = "&r" + Messages.prefixRightHand + " &r";

        if (prefix == null)
            prefix = "";

        e.setFormat(e.getFormat().replace("{marry}", prefix));
    }
}
