package be.spyproof.marriage;

/**
 * Created by Spyproof on 31/03/2015.
 * Contributors: Idlehumor
 */

import be.spyproof.marriage.commands.*;
import be.spyproof.marriage.datamanager.*;
import be.spyproof.marriage.handlers.*;
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
    private PlayerListener playerListener;
    private CommandHandler commandHandler;
    private HerochatListener herochatListener;

    public static Marriage plugin;
    public static Economy eco;
    public static MyConfig config;

    public EffectLib lib;
    public EffectManager effectManager;

    /**
     * TODO Random ideas
     *  - Family tree (implement family stuff)
     *  - Shared bank hooked into the economy
     *  - give exp to partner
     *  - give item
     *  - firework on marriage
     *
     *  - Shared donator perks
     *    - heal
     *    - feed
     *    - Give vote tokens
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
        new Permissions();
        new Messages();
        new CooldownManager();
        this.playerManager = new PlayerManager();
        this.playerListener = new PlayerListener();
        this.herochatListener = new HerochatListener();
        this.commandHandler = new CommandHandler();
        setupEconomy();
        registerListeners();
        registerCommands();
        setupEffectsLib();

        for (Player p : getOnlinePlayers())
            playerManager.addPlayer(p.getName());
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


    public Player getPlayer(String player)
    {
        for (Player p : getOnlinePlayers())
            if (p.getName().toLowerCase().startsWith(player.toLowerCase()))
                return p;
        return null;
    }
    
    public List<Player> getOnlinePlayers()
    {
    	List<World> worlds = this.getServer().getWorlds();
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
        this.getServer().getPluginManager().registerEvents(this.herochatListener, this);
    }

    private void registerCommands()
    {
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

    private void setupEffectsLib()
    {
        if (!isPluginEnabled(Messages.effectsLibPluginName))
            return;

        this.lib = EffectLib.instance();
        this.effectManager = new EffectManager(lib);
    }
}
