package be.spyproof.marriage;

/**
 * Created by Nils on 31/03/2015.
 */

import be.spyproof.marriage.commands.*;
import be.spyproof.marriage.commands.handlers.CommandHandler;
import be.spyproof.marriage.listeners.CommandListener;
import be.spyproof.marriage.datamanager.PlayerManager;
import be.spyproof.marriage.listeners.PlayerListener;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Marriage extends JavaPlugin
{
    private CommandListener commandListener;
    private PlayerListener playerListener;
    private static FileConfiguration config;
    private CommandHandler commandHandler;


    /**
     * statics
     */

    public static FileConfiguration getConfigs()
    {
        return config;
    }

    public static void sendMessage(CommandSender sender, String message)
    {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("\\n", "\n").replace
                ("{prefix}", Marriage.getConfigs().getString("message.prefix"))));
    }

    /**
     * Override
     */

    @Override
    public void onEnable()
    {
        config = getConfig();
        this.playerListener = new PlayerListener();
        this.commandListener = new CommandListener();
        this.commandHandler = new CommandHandler();
        registerListeners();
        registerCommands();
        registerTabComplete();
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
        this.commandHandler.registerCommands(CommandMarry.class);
        this.commandHandler.registerCommands(CommandAdmMarry.class);
    }

    private void registerTabComplete()
    {
        this.getCommand("marry").setTabCompleter(this.commandHandler);
        this.getCommand("admmarry").setTabCompleter(this.commandHandler);
    }
}
