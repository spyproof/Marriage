package be.spyproof.marriage.commands;

import be.spyproof.marriage.Gender;
import be.spyproof.marriage.Marriage;
import be.spyproof.marriage.annotations.Beta;
import be.spyproof.marriage.annotations.Command;
import be.spyproof.marriage.annotations.Default;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by Nils on 4/05/2015.
 * Command handling inspired by https://github.com/Zenith-
 */
public class CommandHandler implements TabCompleter
{
    private static CommandHandler commandHandler = new CommandHandler();
    private static Map<Command, Method> commandMap = new HashMap<Command, Method>();
    private static Map<Method, Object> instances = new HashMap<Method, Object>();
    private static List<String> addedCommands = new ArrayList<String>();

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
                    String command = m.getAnnotation(Command.class).command();
                    if (!addedCommands.contains(command))
                    {
                        addedCommands.add(command);
                        Marriage.plugin.getCommand(command).setTabCompleter(this);
                    }
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

    public void showHelp(String cmd, CommandSender sender, int page)
    {
        List<String> help = getHelp(cmd, sender);
        if (help.size() == 0)
        {
            Marriage.sendMessage(sender, Marriage.getSettings().getString("message.no-permission"));
            return;
        }

        Marriage.sendMessage(sender, "&b&l====> &2&l" + Marriage.plugin.getName() + "&b&l <====");

        double helpPerPage = 8.0;
        page = page - 1;
        if (page < 0)
        page = 0;

        for (int i = (int) (page*helpPerPage); i < ((int) ((page+1)*helpPerPage) < help.size() ? (int) ((page+1)*helpPerPage) : help.size()); i++)
            Marriage.sendMessage(sender, help.get(i));
        if (((int) ((page+1)*helpPerPage) < help.size()))
            Marriage.sendMessage(sender, ChatColor.YELLOW + "/" + cmd + " help " + (page + 2));
    }

    public List<String> getHelp(String cmd, CommandSender sender)
    {
        List<String> help = new ArrayList<String>();
        for (Command cmdInfo: commandMap.keySet())
            if (cmd.equalsIgnoreCase(cmdInfo.command()) && !cmdInfo.helpHidden() && hasPerm(sender, cmdInfo.permission()))
                if (!(sender instanceof Player) && cmdInfo.playersOnly())
                {

                }else
                {
                    boolean addHelp = false;
                    if (commandMap.get(cmdInfo).isAnnotationPresent(Beta.class))
                    {
                        if (Marriage.plugin.getConfig().getBoolean("settings.beta-testing"))
                            addHelp = true;
                    }else
                        addHelp = true;

                    if (addHelp)
                        help.add("&b" + cmdInfo.usage() + "&r -" + (commandMap.get(cmdInfo).isAnnotationPresent(Beta.class) ? " &r[&2&o&lBeta&f]" : "") + " &a" + cmdInfo.desc().replaceAll("[{}]", ""));
                }
        Collections.sort(help);
        return help;
    }

    public void callCommand(String command, String trigger, CommandSender sender, String[] args)
    {
        String arg = command + " " + trigger;
        if (args != null)
            for (String s : args)
                arg += " " + s;
        Marriage.sendDebugInfo(sender.getName() + " invoked the command\n&b/" + arg);

        Method method = getCommandMethod(command, trigger);
        if (method != null)
        {
            Command cmdInfo = getCommandInfo(method);
            if (cmdInfo != null)
            {
                if (cmdInfo.command().equalsIgnoreCase(command))
                {
                    //Fill defaults based on @Default
                    args = fillDefaults(method, args, sender.getName());
                    if (args.length == cmdInfo.args().length)
                    {
                        if (!(sender instanceof Player) && cmdInfo.playersOnly())
                        {
                            Marriage.sendMessage(sender, Marriage.getSettings().getString("message.player-only"));
                            Marriage.sendDebugInfo(sender.getName() + " is not a player, the command is only for players");
                            return;
                        }else {
                            if (hasPerm(sender, cmdInfo.permission()))
                            {
                                try {
                                    if (method.isAnnotationPresent(Beta.class) && Marriage.plugin.getConfig().getBoolean("settings.beta-testing"))
                                        Marriage.sendMessage(sender, "&b&oThis command is in testing fase! Please report all bugs!");
                                    else if (method.isAnnotationPresent(Beta.class) && !Marriage.plugin.getConfig().getBoolean("settings.beta-testing"))
                                    {
                                        Marriage.sendMessage(sender, "&cEnable beta testing to get access to this " +
                                                "command");
                                        Marriage.sendDebugInfo("Beta testing access only");
                                        return;
                                    }
                                    if (args.length == 0)
                                        method.invoke(instances.get(method), sender);
                                    else if (args.length == 1)
                                        method.invoke(instances.get(method), sender, args[0]);
                                    else
                                        method.invoke(instances.get(method), sender, args);
                                    Marriage.sendDebugInfo(sender.getName() + " successfully invoked the command!");
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                                return;
                            }else{
                                Marriage.sendMessage(sender, Marriage.getSettings().getString("message.no-permission"));
                                Marriage.sendDebugInfo("&c" + sender.getName() + " does not have the permission:\n&o" + cmdInfo.permission());
                                return;
                            }
                        }
                    }
                }
            }
        }

        if (trigger.equalsIgnoreCase("help"))
        {
            int page = 1;

            if (args != null)
                if (args.length != 0)
                    page = Integer.parseInt(args[0]);

            showHelp(command, sender, page);
            return;
        }

        Marriage.sendDebugInfo("&c" + sender.getName() + " failed to invoke /" + arg);
        showHelp(command, sender, 1);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] args)
    {
        //TODO implement the help menu
        List<String> tabComplete = new ArrayList<String>();
        String com = command.getName();
        for (String string : args)
            com += " " + string;
        Marriage.sendDebugInfo(commandSender.getName() + " invoked TabCompletion: &b" + com);

        if (args.length == 0)
            return null;

        if (args.length == 1)
        {
            for (Command cmdInfo : commandMap.keySet())
            {
                if (cmdInfo.command().equalsIgnoreCase(command.getName()) && hasPerm(commandSender, cmdInfo.permission()))
                {
                    if (isSpecial(cmdInfo.trigger()))
                        tabComplete = addToList(tabComplete, specialTabs(cmdInfo.trigger()));
                    else if (cmdInfo.trigger().startsWith(args[0].toLowerCase()))
                        tabComplete.add(cmdInfo.trigger());
                }
            }
            if ("help".startsWith(args[0].toLowerCase()) && !tabComplete.contains("help"))
                tabComplete.add("help");
        }

        if (args.length > 1)
        {
            //For each possible command
            for (Command cmdInfo : commandMap.keySet())
            {
                //Check if the @Command has the same amount of args
                if (cmdInfo.args().length+1 >= args.length) //+1 for the trigger
                {
                    //Check permission and if its the same command
                    if (cmdInfo.command().equalsIgnoreCase(command.getName()) && hasPerm(commandSender, cmdInfo.permission()))
                    {
                        String[] cmdArgs = new String[cmdInfo.args().length+1];
                        System.arraycopy(cmdInfo.args(), 0, cmdArgs, 1, cmdArgs.length-1);
                        cmdArgs[0] = cmdInfo.trigger();

                        boolean argsMatch = true;
                        //Check of the args match, except for the last
                        for (int i = 0; i < args.length-1; i++)
                        {
                            if (isSpecial(cmdArgs[i]))
                            {
                                Marriage.sendDebugInfo("Found a special tab: " + cmdArgs[i]);
                                List<String> possibleArgs = specialTabs(cmdArgs[i]);
                                if (possibleArgs != null)
                                    if (!possibleArgs.contains(args[i]))
                                        argsMatch = false;
                            }else if (!args[i].equalsIgnoreCase(cmdArgs[i]))
                            {
                                argsMatch = false;
                            }
                        }

                        //If the arguments match, find the last tab complete
                        if (argsMatch)
                        {
                            if (isSpecial(cmdArgs[args.length-1]))
                            {
                                List<String> tabs = specialTabs(cmdArgs[args.length-1]);
                                if (tabs != null)
                                    for (String possibleTab : tabs)
                                        if (possibleTab.toLowerCase().startsWith(args[args.length-1].toLowerCase()))
                                            tabComplete.add(possibleTab);
                            }else if (cmdArgs[args.length-1].toLowerCase().startsWith(args[args.length-1].toLowerCase()))
                                tabComplete.add(cmdArgs[args.length - 1]);
                        }
                    }
                }
            }
        }

        if (tabComplete.size() == 0)
        {
            tabComplete = null;
            Marriage.sendDebugInfo("Found tab options: null");
        }
        else
        {
            String tabs = "";
            for (String s1 : tabComplete)
                tabs += " " + s1;
            Marriage.sendDebugInfo("Found tab options: " + tabs);
        }

        return tabComplete;
    }

    /**
     * Private stuff
     */

    private String[] fillDefaults(Method method, String[] args, String name)
    {
        if (method.isAnnotationPresent(Default.class) && method.isAnnotationPresent(Command.class))
        {
            String[] def = method.getAnnotation(Default.class).value();
            int length = args.length > def.length ? args.length : def.length;
            String[] newArgs = new String[length];

            for (int i = 0; i < newArgs.length; i++)
            {
                if (def.length > i)
                    if (!def[i].equals("") && def[i] != null)
                        newArgs[i] = def[i].replace("{player}", name);
                if (args.length > i)
                    if (!args[i].equals("") && args[i] != null)
                        newArgs[i] = args[i];
            }

            return newArgs;
        }
        return args;
    }

    private boolean hasPerm(CommandSender sender, String perm)
    {
        if (sender.hasPermission(perm) || perm.equalsIgnoreCase("none") || sender.isOp())
            return true;
        else
        {
            boolean go = perm.contains(".");
            while (go)
            {
                perm = perm.replaceAll("\\.\\*$", "");
                perm = perm.replaceAll("\\.\\w*$", ".*");
                if (!perm.contains("."))
                    go = false;
                if (sender.hasPermission(perm) && go)
                    return true;
            }
            return false;
        }
    }

    private boolean isSpecial(String arg)
    {
        return arg.startsWith("{") && arg.endsWith("}");
    }

    private List<String> specialTabs(String arg)
    {
        List<String> tabs = new ArrayList<String>();
        if (arg.equalsIgnoreCase("{player}"))
            return null; // Will complete with player names
        else if (arg.equalsIgnoreCase("{gender}"))
        {
            for (Gender g : Gender.values())
                tabs.add(g.toString());
            return tabs;
        }

        return null;
    }

    private List<String> addToList(List<String> original, List<String> extra)
    {
        if (extra == null)
            return original;
        for (String s : extra)
            original.add(s);
        return original;
    }

    private Method getCommandMethod(String command, String trigger)
    {
        for (Command cmdInfo : commandMap.keySet())
            if ((cmdInfo.trigger().replaceAll("[{}]", "")).equalsIgnoreCase(trigger) && cmdInfo.command().equalsIgnoreCase(command))
                return commandMap.get(cmdInfo);
        return null;
    }

    private Command getCommandInfo(Method method)
    {
        return method.getAnnotation(Command.class);
    }
}
