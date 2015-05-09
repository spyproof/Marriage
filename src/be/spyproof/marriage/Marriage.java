package be.spyproof.marriage;

/**
 * Created by Spyproof on 31/03/2015.
 * Contributors: Idlehumor
 */

import be.spyproof.marriage.commands.*;
import be.spyproof.marriage.commands.CommandHandler;
import be.spyproof.marriage.listeners.CommandListener;
import be.spyproof.marriage.datamanager.PlayerManager;
import be.spyproof.marriage.listeners.PlayerListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.World;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Marriage extends JavaPlugin
{
    private PlayerManager playerManager;
    private CommandListener commandListener;
    private PlayerListener playerListener;
    private CommandHandler commandHandler;

    private static List<String> debuggers = new ArrayList<String>();
    public static Marriage plugin;
    public static Economy eco;


    /**
     * public
     */

    public PlayerManager getPlayerManager()
    {
    	return this.playerManager;
    }
    
    public void toggleDebugger(String name)
    {
        if (debuggers.contains(name))
            debuggers.remove(name);
        else
            debuggers.add(name);
    }

    public void sendDebugInfo(String message)
    {
        for (String p : debuggers)
        {
            CommandSender player = null;
            if (p.equals("CONSOLE"))
                player = this.getServer().getConsoleSender();
            else
                player = getPlayer(p);

            String prefix = "&e[&a&lDebug&e] &3";
            if (player != null)
                sendMessage(player, prefix + message.replace("\n", "\n"+prefix));
        }
    }

    @SuppressWarnings("deprecation")
    public Player getPlayer(String player)
    {
        return this.getServer().getPlayer(player);
    }
    
    public static List<Player> getOnlinePlayers()
    {
    	List<World> worlds = plugin.getServer().getWorlds();
    	List<Player> players = new ArrayList<Player>();
    	for (World w : worlds)
    		for (Player p : w.getPlayers())
    			if (!players.contains(p))
    				players.add(p);
    	return players;
    }

    public void sendMessage(CommandSender sender, String message)
    {
        message = message.replace("\\n", "\n").replace("{prefix}", Messages.prefix);

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public void sendMessage(String sender, String message)
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
    	Marriage.plugin = this;
    	this.playerManager = new PlayerManager();
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
        this.playerManager.saveAllPlayers();
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
