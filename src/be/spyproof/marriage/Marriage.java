package be.spyproof.marriage;

/**
 * Created by Spyproof on 31/03/2015.
 * Contributors: Idlehumor
 */

import be.spyproof.marriage.commands.*;
import be.spyproof.marriage.datamanager.CooldownManager;
import be.spyproof.marriage.datamanager.MyConfig;
import be.spyproof.marriage.datamanager.PlayerData;
import be.spyproof.marriage.datamanager.PlayerManager;
import be.spyproof.marriage.handlers.CommandHandler;
import be.spyproof.marriage.handlers.Messages;
import be.spyproof.marriage.listeners.*;
import de.slikey.effectlib.EffectLib;
import de.slikey.effectlib.EffectManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Marriage extends JavaPlugin
{
    private PlayerManager playerManager;
    private CommandListener commandListener;
    private PlayerListener playerListener;
    private CommandHandler commandHandler;

    public static Marriage plugin;
    public static Economy eco;
    public static MyConfig config;

    public EffectLib lib = EffectLib.instance();
    public EffectManager effectManager = new EffectManager(lib);

    /**
     * TODO Random ideas
     *  - Family tree (implement family stuff)
     *  - Shared bank hooked into the economy
     *
     *  - Shared donator perks
     *    - heal
     *    - give exp to partner
     *    - give item
     */

    /**
     * Override
     */

    @Override
    public void onEnable()
    {
        plugin = this;
        this.saveDefaultConfig();
        config = new MyConfig(this.getConfig(), new File(this.getDataFolder(), "config.yml"));
        new Messages();
        new CooldownManager();
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
        for (PlayerData p : this.playerManager.getLoadedPlayers().values())
        {
            p.setLastSeen(System.currentTimeMillis());
            this.playerManager.getLoadedPlayers().put(p.getName().toLowerCase(), p);
        }

        this.playerManager.saveAllPlayers();
        this.playerManager.closeDB();
        //config.save();
    }

    /**
     * public
     */

    public PlayerManager getPlayerManager()
    {
    	return this.playerManager;
    }

    @SuppressWarnings("deprecation")
    public Player getPlayer(String player)
    {
        return this.getServer().getPlayer(player);
    }
    
    public List<Player> getOnlinePlayers()
    {
    	List<World> worlds = plugin.getServer().getWorlds();
    	List<Player> players = new ArrayList<Player>();
    	for (World w : worlds)
    		for (Player p : w.getPlayers())
    			if (!players.contains(p))
    				players.add(p);
    	return players;
    }

    public boolean isPluginEnabled(String pluginName)
    {
        return this.getServer().getPluginManager().isPluginEnabled(pluginName);
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
