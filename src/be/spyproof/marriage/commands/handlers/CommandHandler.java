package be.spyproof.marriage.commands.handlers;

import be.spyproof.marriage.Marriage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nils on 4/05/2015.
 * Command handling inspired by https://github.com/Zenith-
 */
public class CommandHandler implements TabCompleter
{
    private static CommandHandler commandHandler = new CommandHandler();
    private static Map<Command, Method> commandMap = new HashMap<Command, Method>();
    private static Map<Method, Object> instances = new HashMap<Method, Object>();

    public static CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public void registerCommands(Class commandClass)
    {
        try
        {
            Object o = commandClass.newInstance();
            for (Method m : commandClass.getDeclaredMethods())
            {
                if (m.isAnnotationPresent(Command.class))
                {
                    commandMap.put(m.getAnnotation(Command.class), m);
                    instances.put(m, o);
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void showHelp(String cmd, CommandSender sender)
    {
        List<String> help = getHelp(cmd);
        for (String aHelp : help)
            Marriage.sendMessage(sender, aHelp);
    }

    public List<String> getHelp(String cmd)
    {
        List<String> help = new ArrayList<String>();
        help.add(ChatColor.BLUE + "" + ChatColor.BOLD + "====> " + ChatColor.DARK_GREEN + ChatColor.BOLD + "Marriage" + ChatColor.BLUE + ChatColor.BOLD + " <====");
        for (Command cmdinfo: commandMap.keySet())
        {
            if (cmd.equalsIgnoreCase(cmdinfo.command()) && !cmdinfo.helpHidden())
                help.add(ChatColor.AQUA + cmdinfo.usage() + ChatColor.RESET + " - " + ChatColor.GREEN + cmdinfo.desc().replace("{", "").replace("}", ""));
        }

        return help;
    }

    public void callCommand(String command, String trigger, CommandSender sender, String[] args)
    {
        Method method = getCommandMethod(trigger);
        if (method != null)
        {
            Command cmdInfo = getCommandInfo(method);
            if (cmdInfo != null)
            {
                if (cmdInfo.command().equalsIgnoreCase(command))
                {
                    if (args.length == cmdInfo.args().length)
                    {
                        if (sender instanceof Player && cmdInfo.playersOnly())
                        {
                            if (sender.hasPermission(cmdInfo.permission()) || cmdInfo.permission().equalsIgnoreCase("none"))
                            {
                                try {
                                    if (args.length == 0)
                                        method.invoke(instances.get(method), sender);
                                    else if (args.length == 1)
                                        method.invoke(instances.get(method), sender, args[0]);
                                    else
                                        method.invoke(instances.get(method), sender, args);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                                return;
                            }
                            else {
                                Marriage.sendMessage(sender, Marriage.getConfigs().getString("message.no-permission").replace("{perm}", cmdInfo.permission()));
                                return;
                            }
                        }
                        else
                        {
                            Marriage.sendMessage(sender, Marriage.getConfigs().getString("message.player-only"));
                            return;
                        }
                    }
                }
            }
        }
        showHelp(command, sender);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] args)
    {
        List<String> tabComplete = new ArrayList<String>();

        if (args.length != 1)
            return null;

        if (args.length == 1)
            for (Command cmdInfo : commandMap.keySet())
                if (cmdInfo.command().equalsIgnoreCase(command.getName()) && cmdInfo.trigger().startsWith(args[0].toLowerCase()) && !isHidden(cmdInfo.trigger()))
                    tabComplete.add(cmdInfo.trigger());


        return tabComplete;
    }

    /**
     * Private stuff
     */

    private String cleansHidden(String arg)
    {
        return arg.replace("{", "").replace("}", "");
    }

    private boolean isHidden(String arg)
    {
        if (arg.startsWith("{") && arg.endsWith("}"))
            return true;
        return false;
    }

    private Method getCommandMethod(String trigger)
    {
        for (Command cmdInfo : commandMap.keySet())
            if (cleansHidden(cmdInfo.trigger()).equalsIgnoreCase(trigger))
                return commandMap.get(cmdInfo);
        return null;
    }

    private Command getCommandInfo(Method method)
    {
        return method.getAnnotation(Command.class);
    }
}
