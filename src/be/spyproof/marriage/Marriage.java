package be.spyproof.marriage;

/**
 * Created by Nils on 31/03/2015.
 */

import be.spyproof.marriage.commands.*;
import be.spyproof.marriage.commands.CommandHandler;
import be.spyproof.marriage.listeners.CommandListener;
import be.spyproof.marriage.datamanager.PlayerManager;
import be.spyproof.marriage.listeners.PlayerListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Marriage extends JavaPlugin
{
    private CommandListener commandListener;
    private PlayerListener playerListener;
    private CommandHandler commandHandler;

    private static List<String> debuggers = new ArrayList<String>();
    public static Marriage plugin;
    public static Economy eco;


    /**
     * statics
     */

    public static void toggleDebugger(String name)
    {
        System.out.println(name);
        if (debuggers.contains(name))
            debuggers.remove(name);
        else
            debuggers.add(name);
    }

    public static void sendDebugInfo(String message)
    {
        for (String p : debuggers)
        {
            CommandSender player = null;
            if (p.equals("CONSOLE"))
                player = plugin.getServer().getConsoleSender();
            else
                player = getPlayer(p);

            String prefix = "&e[&a&lDebug&e] &3";
            if (player != null)
                sendMessage(player, prefix + message.replace("\n", "\n"+prefix));
        }
    }

    public static FileConfiguration getSettings()
    {
        return plugin.getConfig();
    }

    @Nullable
    public static Player getPlayer(String player)
    {
        return plugin.getServer().getPlayer(player);
    }

    public static void sendMessage(CommandSender sender, String message)
    {
        message = message.replace("\\n", "\n").replace("{prefix}", Marriage.getSettings().getString("message.prefix"));

        /*if (PlayerManager.getGender(sender.getName()).equals(Gender.FEMALE))
            message = message.replaceAll("\bhim\b", "her").replaceAll("\bhis\b", "her").replaceAll("\bhe\b", "she");*/

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void sendMessage(String sender, String message)
    {
        Player p = getPlayer(sender);
        if (p != null)
            sendMessage(p, message);
    }

    /**
     * Override
     */

    @Override
    public void onEnable()
    {
        plugin = this;
        this.playerListener = new PlayerListener();
        this.commandListener = new CommandListener();
        this.commandHandler = new CommandHandler();
        setupEconomy();
        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable()
    {
        PlayerManager.saveAllPlayers();
    }

    /**
     * Private
     */

    private void registerListeners()
    {
        this.getServer().getPluginManager().registerEvents(this.playerListener, this);
    }

    private void registerCommands()
    {
        this.getCommand("marry").setExecutor(this.commandListener);
        this.getCommand("admmarry").setExecutor(this.commandListener);
        this.getCommand("partner").setExecutor(this.commandListener);
        this.commandHandler.registerCommands(CommandMarry.class);
        this.commandHandler.registerCommands(CommandAdmMarry.class);
        this.commandHandler.registerCommands(CommandPartner.class);
    }

    private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            eco = economyProvider.getProvider();
        }

        return (eco != null);
    }
}
