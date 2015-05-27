package be.spyproof.marriage.commands;

import be.spyproof.marriage.Marriage;
import be.spyproof.marriage.datamanager.CooldownManager;
import be.spyproof.marriage.datamanager.PlayerManager;
import be.spyproof.marriage.handlers.CommandHandler;
import be.spyproof.marriage.handlers.Messages;
import be.spyproof.marriage.handlers.Permissions;
import be.spyproof.marriage.Status;
import be.spyproof.marriage.annotations.Command;
import be.spyproof.marriage.annotations.Default;

import com.avaje.ebeaninternal.server.cluster.mcast.Message;
import com.earth2me.essentials.Essentials;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectLib;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.EffectType;
import de.slikey.effectlib.effect.*;
import de.slikey.effectlib.util.ParticleEffect;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.entity.Player;

/**
 * Created by Nils on 3/04/2015.
 * Command handling inspired by https://github.com/Zenith-
 */
public class CommandAdmMarry
{
	private PlayerManager playerManager;

    public CommandAdmMarry()
    {
    	this.playerManager = Marriage.plugin.getPlayerManager();
    }

    @Command(command = "admmarry", trigger = "reload", args = {}, playersOnly = false, permission = Permissions.adminReload, desc = "Reload the player config", usage = "/admmarry reload")
    public void forceReload(CommandSender sender)
    {
        playerManager.reload();
        Marriage.config.load();
        sender.sendMessage(ChatColor.DARK_GREEN + "Reloading player config... Loaded " + playerManager.getLoadedPlayers().size() + " players");
    }

    @Command(command = "admmarry", trigger = "save", args = {}, playersOnly = false, permission = Permissions.adminSave, desc = "Save the config manually", usage = "/admmarry save")
    public void forceSave(CommandSender sender)
    {
        playerManager.saveAllPlayers();
        sender.sendMessage(ChatColor.DARK_GREEN + "Saved " + playerManager.getLoadedPlayers().size() + " players");
    }

    @Command(command = "admmarry", trigger = "remove", args = {"{player}"}, playersOnly = false, permission = Permissions.adminRemove, desc = "Reset a player", usage = "/admmarry remove <player>")
    public void removePlayer(CommandSender sender, String player)
    {
        try{
            try {
                String partner = playerManager.getPartner(player);

                if(!partner.equals("") && !partner.isEmpty())
                    CommandMarry.divorcePlayer(partner);
            } catch (IllegalArgumentException ignored) {}

            playerManager.resetPlayer(player);
            sender.sendMessage(ChatColor.DARK_GREEN + "Removed " + player + "'s data");
        }catch(IllegalArgumentException e){
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
    }

    @Default({"{player}"})
    @Command(command = "admmarry", trigger = "info", args = {"{player}"}, playersOnly = false, permission = Permissions.adminInfo, desc = "Get the player information", usage = "/admmarry info <player>")
    public void getPlayerInfo(CommandSender sender, String player)
    {
        String status, gender, partner;
        boolean isHomeSet, trustsPartner;
        double balance;
        Location l;
        try{
            status = playerManager.getStatus(player).toString();
            gender = playerManager.getGender(player).toString();
            partner = playerManager.getPartner(player);
            balance = playerManager.getBalance(player);
            isHomeSet = playerManager.isHomeSet(player);
            l = playerManager.getHomeLoc(player);

            Messages.sendMessage(sender, "&e------------&6&l" + player + "&e------------");

            Messages.sendMessage(sender, "&6Gender: &e" + gender);
            Messages.sendMessage(sender, "&6Status: &e" + status);

            if (!playerManager.getStatus(sender.getName()).equals(Status.MARRIED_TO_PERSON))
                return;

            Messages.sendMessage(sender, "&6Partner: &e" + partner);
            Messages.sendMessage(sender, "&6Balance: &e$" + balance);

            if(isHomeSet)
                Messages.sendMessage(sender, "&6Home: &eWorld: " + l.getWorld().getName() + "  X:" + l.getBlockX() + "  Y:" + l.getBlockY() + "  Z:" + l.getBlockZ());
            else
                Messages.sendMessage(sender, "&6Home: &eHome is not set");
        }catch (IllegalArgumentException e){
            Messages.sendMessage(sender, ChatColor.RED + e.getMessage());
        }
    }

    @Command(command = "admmarry", trigger = "{debug}", args = {}, playersOnly = false, helpHidden = true)
    public void getDebug(CommandSender sender)
    {
        //When enabled, show the player debug information
        if (!sender.isOp())
            return;
        Messages.toggleDebugger(sender.getName());
    }

    @Command(command = "admmarry", trigger = "plugin", args = {}, playersOnly = false, permission = Permissions.adminPlugin, desc = "Get more info about the plugin", usage = "/admmarry plugin")
    public void getPluginInfo(CommandSender sender)
    {
        PluginDescriptionFile description = Marriage.plugin.getDescription();
        Messages.sendMessage(sender, "&eThe current version of " + Marriage.plugin.getName() + " is &6" + description.getVersion());
        if (description.getAuthors() != null)
            if (description.getAuthors().size() != 0)
            {
                String authors = "&eAuthors:&6";
                for (String s : description.getAuthors())
                    authors += " " + s;
                Messages.sendMessage(sender, authors);
            }
        if (description.getWebsite() != null)
            Messages.sendMessage(sender, "&eThe developer's website is &6&n" + description.getWebsite());
    }

    @Command(command = "admmarry", trigger = "socialspy", args = {}, playersOnly = false, permission = Permissions.adminSocialSpy, desc = "See the partner chat", usage = "/admmarry socialspy")
    public void socialspy(CommandSender sender)
    {
    	Essentials essentials = (Essentials) Marriage.plugin.getServer().getPluginManager().getPlugin("Essentials");
    	if (essentials != null)
    		if (essentials.isEnabled())
    		{
    			Messages.sendMessage(sender, "&eThis plugin is hooked into the socialspy of essentials");
    			return;
    		}

    	Messages.sendMessage(sender, "&eSocial spy is enabled for you &o" + Permissions.adminSocialSpy);
    }

    @Command(command = "admmarry", trigger = "cooldown", args = {"{player}"}, playersOnly = false, permission = Permissions.adminResetCooldown, desc = "Reset the player's cooldown", usage = "/admmarry cooldown <player>")
    public void clearCooldown(CommandSender sender, String player)
    {
        CooldownManager.cooldownManager.removeCooldown(player, "all");
        Messages.sendMessage(sender, "&eYou have reset the cooldowns of " + player);
    }

    @Default({"1", "1", "1"})
    @Command(command = "admmarry", trigger = "effect", args = {"{int}", "{int}", "{int}"}, helpHidden = true, playersOnly = true)
    public void effects(final Player sender, String[] args)
    {
        if (!sender.isOp())
            return;

        if (!Marriage.plugin.isPluginEnabled(Messages.effectsLibPluginName))
        {
            Messages.sendMessage(sender, "&cEffectsLib is not installed!");
            return;
        }

        EffectLib lib = EffectLib.instance();
        EffectManager manager = new EffectManager(lib);

        int effectID = 0;
        int iteration = 0;
        int particleID = 0;

        if(args[0].equalsIgnoreCase("list"))
        {
            for (int i = 0; i < ParticleEffect.values().length; i++)
                Messages.sendMessage(sender, "&3" + i + ": " + ParticleEffect.values()[i]);
            return;
        }


        try {
            effectID = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            effectID = 1;
        }
        try {
            particleID = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            particleID = 1;
        }
        try {
            iteration = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            iteration = 1;
        }


        Effect effect = getEffect(effectID, particleID);

        effect.iterations = iteration;
        effect.delay = 0;
        effect.setEntity(sender);
        effect.start();
    }

    private Effect getEffect(int effectID, int particleID)
    {
        ParticleEffect particleEffect = getParticleEffect(particleID);
        switch (effectID)
        {
            case 1:
                Messages.sendDebugInfo("&3" + effectID + ": Animated Ball");
                AnimatedBallEffect effect = new AnimatedBallEffect(Marriage.plugin.effectManager);
                effect.particle = particleEffect;
                return effect;
            case 2:
                Messages.sendDebugInfo("&3" + effectID + ": Arc");
                ArcEffect effect1 = new ArcEffect(Marriage.plugin.effectManager);
                effect1.particle = particleEffect;
                return effect1;
            case 3:
                Messages.sendDebugInfo("&3" + effectID + ": Atom");
                AtomEffect effect2 = new AtomEffect(Marriage.plugin.effectManager);
                return effect2;
            case 4:
                Messages.sendDebugInfo("&3" + effectID + ": BigBang");
                BigBangEffect effect3 = new BigBangEffect(Marriage.plugin.effectManager);
                return effect3;
            case 5:
                Messages.sendDebugInfo("&3" + effectID + ": Bleed");
                BleedEffect effect4 = new BleedEffect(Marriage.plugin.effectManager);
                return effect4;
            case 6:
                Messages.sendDebugInfo("&3" + effectID + ": Cone");
                ConeEffect effect5 = new ConeEffect(Marriage.plugin.effectManager);
                effect5.particle = particleEffect;
                return effect5;
            case 7:
                Messages.sendDebugInfo("&3" + effectID + ": Cube");
                CubeEffect effect6 = new CubeEffect(Marriage.plugin.effectManager);
                effect6.particle = particleEffect;
                return effect6;
            case 8:
                Messages.sendDebugInfo("&3" + effectID + ": Cylinder");
                CylinderEffect effect7 = new CylinderEffect(Marriage.plugin.effectManager);
                effect7.particle = particleEffect;
                return effect7;
            case 9:
                Messages.sendDebugInfo("&3" + effectID + ": Dna");
                DnaEffect effect8 = new DnaEffect(Marriage.plugin.effectManager);
                return effect8;
            case 10:
                Messages.sendDebugInfo("&3" + effectID + ": Donut");
                DonutEffect effect9 = new DonutEffect(Marriage.plugin.effectManager);
                effect9.particle = particleEffect;
                return effect9;
            case 11:
                Messages.sendDebugInfo("&3" + effectID + ": Dragon");
                DragonEffect effect10 = new DragonEffect(Marriage.plugin.effectManager);
                return effect10;
            case 12:
                Messages.sendDebugInfo("&3" + effectID + ": Earth");
                EarthEffect effect11 = new EarthEffect(Marriage.plugin.effectManager);
                return effect11;
            case 13:
                Messages.sendDebugInfo("&3" + effectID + ": Explode");
                ExplodeEffect effect12 = new ExplodeEffect(Marriage.plugin.effectManager);
                return effect12;
            case 14:
                Messages.sendDebugInfo("&3" + effectID + ": Flame");
                FlameEffect effect13 = new FlameEffect(Marriage.plugin.effectManager);
                return effect13;
            case 15:
                Messages.sendDebugInfo("&3" + effectID + ": Grid");
                GridEffect effect14 = new GridEffect(Marriage.plugin.effectManager);
                effect14.particle = particleEffect;
                return effect14;
            case 16:
                Messages.sendDebugInfo("&3" + effectID + ": Heart");
                HeartEffect effect15 = new HeartEffect(Marriage.plugin.effectManager);
                effect15.particle = particleEffect;
                return effect15;
            case 17:
                Messages.sendDebugInfo("&3" + effectID + ": Helix");
                HelixEffect effect16 = new HelixEffect(Marriage.plugin.effectManager);
                effect16.particle = particleEffect;
                return effect16;
            case 18:
                Messages.sendDebugInfo("&3" + effectID + ": Hill");
                HillEffect effect17 = new HillEffect(Marriage.plugin.effectManager);
                effect17.particle = particleEffect;
                return effect17;
            case 19:
                Messages.sendDebugInfo("&3" + effectID + ": Icon");
                IconEffect effect18 = new IconEffect(Marriage.plugin.effectManager);
                effect18.particle = particleEffect;
                return effect18;
            case 20:
                Messages.sendDebugInfo("&3" + effectID + ": Jump");
                JumpEffect effect19 = new JumpEffect(Marriage.plugin.effectManager);
                return effect19;
            case 21:
                Messages.sendDebugInfo("&3" + effectID + ": Line");
                LineEffect effect20 = new LineEffect(Marriage.plugin.effectManager);
                effect20.particle = particleEffect;
                return effect20;
            case 22:
                Messages.sendDebugInfo("&3" + effectID + ": Love");
                LoveEffect effect21 = new LoveEffect(Marriage.plugin.effectManager);
                effect21.particle = particleEffect;
                return effect21;
            case 23:
                Messages.sendDebugInfo("&3" + effectID + ": Music");
                MusicEffect effect22 = new MusicEffect(Marriage.plugin.effectManager);
                return effect22;
            case 24:
                Messages.sendDebugInfo("&3" + effectID + ": Shield");
                ShieldEffect effect23 = new ShieldEffect(Marriage.plugin.effectManager);
                effect23.particle = particleEffect;
                return effect23;
            case 25:
                Messages.sendDebugInfo("&3" + effectID + ": Sky Rocket");
                SkyRocketEffect effect24 = new SkyRocketEffect(Marriage.plugin.effectManager);
                return effect24;
            case 26:
                Messages.sendDebugInfo("&3" + effectID + ": Smoke");
                SmokeEffect effect25 = new SmokeEffect(Marriage.plugin.effectManager);
                effect25.particle = particleEffect;
                return effect25;
            case 27:
                Messages.sendDebugInfo("&3" + effectID + ": Sphere");
                SphereEffect effect26 = new SphereEffect(Marriage.plugin.effectManager);
                return effect26;
            case 28:
                Messages.sendDebugInfo("&3" + effectID + ": Star");
                StarEffect effect27 = new StarEffect(Marriage.plugin.effectManager);
                effect27.particle = particleEffect;
                return effect27;
            case 29:
                Messages.sendDebugInfo("&3" + effectID + ": Text");
                TextEffect effect28 = new TextEffect(Marriage.plugin.effectManager);
                effect28.particle = particleEffect;
                return effect28;
            case 30:
                Messages.sendDebugInfo("&3" + effectID + ": Trace");
                TraceEffect effect29 = new TraceEffect(Marriage.plugin.effectManager);
                effect29.particle = particleEffect;
                return effect29;
            case 31:
                Messages.sendDebugInfo("&3" + effectID + ": Turn");
                TurnEffect effect30 = new TurnEffect(Marriage.plugin.effectManager);
                return effect30;
            case 32:
                Messages.sendDebugInfo("&3" + effectID + ": Vortex");
                VortexEffect effect31 = new VortexEffect(Marriage.plugin.effectManager);
                effect31.particle = particleEffect;
                return effect31;
            case 33:
                Messages.sendDebugInfo("&3" + effectID + ": Warp");
                WarpEffect effect32 = new WarpEffect(Marriage.plugin.effectManager);
                effect32.particle = particleEffect;
                return effect32;
            case 34:
                Messages.sendDebugInfo("&3" + effectID + ": Wave");
                WaveEffect effect33 = new WaveEffect(Marriage.plugin.effectManager);
                effect33.particle = particleEffect;
                return effect33;
            default:
                Messages.sendDebugInfo("&3" + effectID + ": Default: Animated Ball");
                ArcEffect effectDefault = new ArcEffect(Marriage.plugin.effectManager);
                effectDefault.particle = particleEffect;
                return effectDefault;
        }
    }

    private ParticleEffect getParticleEffect(int arg)
    {
        if (arg > ParticleEffect.values().length) {
            Messages.sendDebugInfo("&3Default: " + ParticleEffect.FLAME.getName());
            return ParticleEffect.FLAME;
        }else{
            Messages.sendDebugInfo("&3" + arg + ": " + ParticleEffect.values()[arg]);
            return ParticleEffect.values()[arg];
        }
    }
}
