package be.spyproof.marriage.datamanager;

import be.spyproof.marriage.Marriage;
import be.spyproof.marriage.Permissions;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Spyproof on 24/05/2015.
 */
public class CooldownManager
{
    private List<String> playerNames;
    private List<String> catagory;
    private List<Long> cooldown; //timestamp when its passed seconds
    private Map<String, Integer> catagoryCooldowns; // in miliseconds

    public static CooldownManager cooldownManager;

    public CooldownManager()
    {
        cooldownManager = this;
        this.playerNames = new ArrayList<String>();
        this.catagory = new ArrayList<String>();
        this.cooldown = new ArrayList<Long>();
        this.catagoryCooldowns = new HashMap<String, Integer>();

        this.catagoryCooldowns.put("request", Marriage.plugin.getConfig().getInt("cooldown.request")*60*1000);
        this.catagoryCooldowns.put("divorce", Marriage.plugin.getConfig().getInt("cooldown.divorce")*60*1000);
    }

    public int getCooldown(CommandSender sender, String cat)
    {
        if (Permissions.hasPerm(sender, Permissions.bypassCooldown))
            return 0;

        for (int i = 0; i < this.playerNames.size(); i++)
        {
            if (this.playerNames.get(i).equals(sender.getName().toLowerCase()) && this.catagory.get(i).equals(cat))
            {
                int timeRemaining = (int) (this.cooldown.get(i) - System.currentTimeMillis());
                return timeRemaining/1000;
            }
        }

        return 0;
    }

    public void setCooldown(CommandSender sender, String cat)
    {
        if (Permissions.hasPerm(sender, Permissions.bypassCooldown))
            return;

        int currentCooldown = getCooldown(sender, cat);

        if (currentCooldown < 0)
            removeCooldown(sender.getName(), cat);

        long cooldownTimestamp = System.currentTimeMillis() + catagoryCooldowns.get(cat.toLowerCase());
        this.playerNames.add(sender.getName().toLowerCase());
        this.catagory.add(cat);
        this.cooldown.add(cooldownTimestamp);
    }

    public int getCooldown(String sender, String cat)
    {
        sender = sender.toLowerCase();

        for (int i = 0; i < this.playerNames.size(); i++)
        {
            if (this.playerNames.get(i).equals(sender) && this.catagory.get(i).equals(cat))
            {
                int timeRemaining = (int) (this.cooldown.get(i) - System.currentTimeMillis());
                return timeRemaining/1000;
            }
        }

        return 0;
    }

    public void setCooldown(String sender, String cat)
    {
        sender = sender.toLowerCase();

        int currentCooldown = getCooldown(sender, cat);

        if (currentCooldown < 0)
            removeCooldown(sender, cat);

        long cooldownTimestamp = System.currentTimeMillis() + catagoryCooldowns.get(cat.toLowerCase());
        this.playerNames.add(sender.toLowerCase());
        this.catagory.add(cat);
        this.cooldown.add(cooldownTimestamp);
    }

    public void removeCooldown(String sender, String cat)
    {
        String name = sender.toLowerCase();
        for (int i = 0; i < this.playerNames.size(); i++)
        {
            if (this.playerNames.get(i).equals(name) && (this.catagory.get(i).equals(cat) || cat.equals("all")))
            {
                this.playerNames.remove(i);
                this.catagory.remove(i);
                this.cooldown.remove(i);
            }
        }
    }
}
