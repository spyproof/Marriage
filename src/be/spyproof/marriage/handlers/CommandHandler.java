package be.spyproof.marriage.handlers;

import be.spyproof.marriage.Marriage;
import be.spyproof.marriage.annotations.Beta;
import be.spyproof.marriage.annotations.Command;
import be.spyproof.marriage.annotations.Default;

import be.spyproof.marriage.annotations.SpecialArgs;
import be.spyproof.marriage.exceptions.PermissionException;
import com.avaje.ebeaninternal.server.cluster.mcast.Message;
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
    private static List<Method> specialArgsMethods = new ArrayList<Method>();
    private static List<String> specialArgs = new ArrayList<String>();

    public static CommandHandler getCommandHandler() {
        return commandHandler;
    }

    //Register the command handling
    @SuppressWarnings("rawtypes")
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
                        //Register tab completion for the command
                        addedCommands.add(command);
                        Marriage.plugin.getCommand(command).setTabCompleter(this);
                    }
                    //Link the @Command with the method
                    commandMap.put(m.getAnnotation(Command.class), m);
                    //Link the method with the class
                    instances.put(m, o);
                }

                if (m.isAnnotationPresent(SpecialArgs.class))
                {
                    if (!specialArgsMethods.contains(m))
                    {
                        try
                        {
                            HashMap<String, List<String>> map = (HashMap<String, List<String>>) m.invoke(o);
                            for (String s : (map.keySet()))
                                specialArgs.add(s);

                            specialArgsMethods.add(m);
                            instances.put(m, o);
                        } catch (InvocationTargetException e)
                        {
                            e.printStackTrace();
                        }
                    }
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
            Messages.sendMessage(sender, Messages.noPermission);
            return;
        }

        Messages.sendMessage(sender, "&b&l====> &2&l" + Marriage.plugin.getName() + "&b&l <====");

        //Show 8 commands per help page
        double helpPerPage = 8.0;
        page = page - 1;
        if (page < 0)
        page = 0;

        //Devide the help section into multiple pages
        for (int i = (int) (page*helpPerPage); i < ((int) ((page+1)*helpPerPage) < help.size() ? (int) ((page+1)*helpPerPage) : help.size()); i++)
            Messages.sendMessage(sender, help.get(i));
        if (help.size() - ((int) ((page+1)*helpPerPage)) == 1)
            Messages.sendMessage(sender, help.get(help.size()-1));
        else if (((int) ((page+1)*helpPerPage) < help.size()))
            Messages.sendMessage(sender, ChatColor.YELLOW + "/" + cmd + " help " + (page + 2));
    }

    public List<String> getHelp(String cmd, CommandSender sender)
    {
        Map<Object, Integer> help = new HashMap<Object, Integer>();
        for (Command cmdInfo: commandMap.keySet())
        {
            //If the sender has the perm
            boolean perm;
            boolean isUnlocked = true;
            try {
                perm = Permissions.hasPerm(sender, cmdInfo.permission());
            } catch (PermissionException e) {
                perm = e.hasPermission();
                isUnlocked = e.isUnlocked();
            }

            //Only show help when: the command name is right & command is not hidden from the help menu
            if (cmd.equalsIgnoreCase(cmdInfo.command()) && !cmdInfo.hidden() && perm)
            {
                //Only show the help menu if the sender is a player & if the command is player only
                if (!(sender instanceof Player) && cmdInfo.playersOnly())
                {

                }
                else
                {
                    //When the method has @Beta, only add it to the help list if it is enabled in the config
                    boolean addHelp = false;
                    if (commandMap.get(cmdInfo).isAnnotationPresent(Beta.class))
                    {
                        if (Marriage.plugin.getConfig().getBoolean("beta-testing"))
                            addHelp = true;
                    }
                    else
                        addHelp = true;

                    if (addHelp)
                    {
                        int cost = Permissions.unlockCost(cmdInfo.permission());
                        help.put(String.format("&b%s&r - %s&a%s", cmdInfo.usage(), commandMap.get(cmdInfo).isAnnotationPresent(Beta.class) ? "[&2&o&lBeta&f] " : "", (isUnlocked ? cmdInfo.desc().replaceAll("[{}]", "") : Messages.sharedMoneyNeeded).replace("{money}", cost + "")), cost);
                    }
                }
            }

        }
        List<String> sortedHelp = new ArrayList<String>();
        sortedHelp.addAll(Messages.sortMapByValue(help));

        return sortedHelp;
    }

    public void callCommand(String command, String trigger, CommandSender sender, String[] args)
    {
        //arg = command + all arguments, only used for debugging
        String arg = command + " " + trigger;
        if (args != null)
            for (String s : args)
                arg += " " + s;
        Messages.sendDebugInfo(sender.getName() + " invoked the command\n&b/" + arg);

        //Find the method that matches the command
        Method method = getCommandMethod(command, trigger);

        if (method != null)
        {
            Command cmdInfo = method.getAnnotation(Command.class);
            //If the method has the @Command
            if (cmdInfo != null)
            {
                //Fill defaults based on @Default (does not overwrite)
                String[] newArgs = fillDefaults(method, args, sender.getName());
                //Check if the argument amount are the same
                if (newArgs.length == cmdInfo.args().length)
                {
                    //Check if its player only
                    if (!(sender instanceof Player) && cmdInfo.playersOnly())
                    {
                        Messages.sendMessage(sender, Messages.playerOnly);
                        Messages.sendDebugInfo(sender.getName() + " is not a player, the command is only for players");
                        return;
                    }else {
                        //Check for permissions
                        try {
                            Permissions.hasPerm(sender, cmdInfo.permission());
                        } catch (PermissionException e) {
                            Messages.sendMessage(sender, e.getMessage());
                            return;
                        }

                        try {
                            //Check if the command is in beta (has @beta)
                            if (method.isAnnotationPresent(Beta.class) && Marriage.plugin.getConfig().getBoolean("beta-testing"))
                                Messages.sendMessage(sender, method.getAnnotation(Beta.class).value());
                            else if (method.isAnnotationPresent(Beta.class) && !Marriage.plugin.getConfig().getBoolean("beta-testing"))
                            {
                                if (sender.getName().equals("TPNils") || sender.getName().equals("NotTP"))
                                    Messages.sendMessage(sender, "Bypassing beta command");
                                else
                                {
                                    if (sender.isOp())
                                        Messages.sendMessage(sender, "&cEnable beta testing to get access to this command");
                                    else
                                        Messages.sendMessage(sender, "&cThis command is in testing fase and therefor disabled");
                                    Messages.sendDebugInfo("Beta testing access only");
                                    return;
                                }
                            }

                            //Try to execute the command
                            if (newArgs.length == 0)
                            {
                                if (method.getGenericParameterTypes().length == 1)
                                    method.invoke(instances.get(method), sender);
                                else if (method.getGenericParameterTypes().length == 2)
                                    method.invoke(instances.get(method), sender, trigger);
                            }
                            else if (newArgs.length == 1)
                                method.invoke(instances.get(method), sender, newArgs[0]);
                            else
                                method.invoke(instances.get(method), sender, newArgs);
                            Messages.sendDebugInfo(sender.getName() + " successfully invoked the command!");
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                }
            }
        }

        //If no correct command usage is found, show the default help page
        //If the command has a trigger "help", it will override the default help page
        if (trigger.equalsIgnoreCase(""))
        {
            callCommand(command, "help", sender, args);
            return;
        }

        //Get the page number for the default help message
        int page = 1;

        if (args != null)
            if (args.length != 0)
                try {
                    page = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    page = 1;
                }

        Messages.sendDebugInfo("&c" + sender.getName() + " failed to invoke /" + arg);
        showHelp(command, sender, page);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] args)
    {
        //Will store the possible tab completions
        List<String> tabComplete = new ArrayList<String>();

        //Debug info
        String com = command.getName();
        for (String string : args)
            com += " " + string;
        Messages.sendDebugInfo(commandSender.getName() + " invoked TabCompletion: &b" + com);

        if (args.length == 0)
            return null;

        //If only a trigger is found
        if (args.length == 1)
        {
            for (Command cmdInfo : commandMap.keySet())
            {
                boolean perm;
                try {
                    perm = Permissions.hasPerm(commandSender, cmdInfo.permission());
                } catch (PermissionException e) {
                    perm = e.hasPermission();
                }

                if (cmdInfo.command().equalsIgnoreCase(command.getName()) && perm)
                {
                    if (isSpecial(cmdInfo.trigger()))
                    {
                        List<String> specials = specialArgs(cmdInfo.trigger());
                        if (specials!=null)
                            for (String special : specials)
                                if (special.startsWith(args[0].toLowerCase()))
                                    tabComplete.add(special);
                    }
                    else if (cmdInfo.trigger().startsWith(args[0].toLowerCase()))
                        tabComplete.add(cmdInfo.trigger());
                }
            }
            if ("help".startsWith(args[0].toLowerCase()) && !tabComplete.contains("help"))
                tabComplete.add("help");
        }

        //If a trigger and extra arguments are found
        if (args.length > 1)
        {
            //For each possible command
            for (Command cmdInfo : commandMap.keySet())
            {
                //Check if the @Command has the same amount of args
                if (cmdInfo.args().length+1 >= args.length) //+1 for the trigger
                {
                    //Check permission and if its the same command
                    boolean perm;
                    try {
                        perm = Permissions.hasPerm(commandSender, cmdInfo.permission());
                    } catch (PermissionException e) {
                        perm = e.hasPermission();
                    }
                    if (cmdInfo.command().equalsIgnoreCase(command.getName()) && perm)
                    {
                        //cmdArgs = trigger + arguments behind the trigger
                        String[] cmdArgs = new String[cmdInfo.args().length+1];
                        System.arraycopy(cmdInfo.args(), 0, cmdArgs, 1, cmdArgs.length-1);
                        cmdArgs[0] = cmdInfo.trigger();

                        boolean argsMatch = true;
                        //Check of the args match, except for the last (last needs to be tab completed)
                        for (int i = 0; i < args.length-1; i++)
                        {
                            //Special tabs example: {gender} will return {"male", "female", "HIDDEN"}
                            if (isSpecial(cmdArgs[i]))
                            {
                                Messages.sendDebugInfo("Found a special tab: " + cmdArgs[i]);
                                List<String> possibleArgs = specialArgs(cmdArgs[i]);
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
                                List<String> tabs = specialArgs(cmdArgs[args.length - 1]);
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

        //Debug info
        if (tabComplete.size() == 0)
        {
            tabComplete = null;
            Messages.sendDebugInfo("Found tab options: null");
        }
        else
        {
            String tabs = "";
            for (String s1 : tabComplete)
                tabs += " " + s1;
            Messages.sendDebugInfo("Found tab options: " + tabs);
        }

        return tabComplete;
    }

    /**
     * Private stuff
     */

    //Fills the missing arguments if @Default(values) is present
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

    private boolean isSpecial(String arg)
    {
        return specialArgs.contains(arg);
    }

    private List<String> specialArgs(String arg)
    {
        //Replace the special tabs
        List<String> tabs = new ArrayList<String>();

        for (Method m : specialArgsMethods)
        {
            try
            {
                HashMap<String, List<String>> args = (HashMap<String, List<String>>) m.invoke(instances.get(m));
                for (String s : args.keySet())
                    if (s.equalsIgnoreCase(arg))
                        if (args.get(s) != null)
                            tabs.addAll(args.get(s));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return tabs;
    }

    private Method getCommandMethod(String command, String trigger)
    {
        for (Command cmdInfo : commandMap.keySet())
        {
            if (isSpecial(cmdInfo.trigger()))
            {
                List<String> special = specialArgs(cmdInfo.trigger());
                for (String s : special)
                    if (s.equalsIgnoreCase(trigger) && cmdInfo.command().equalsIgnoreCase(command))
                        return commandMap.get(cmdInfo);
            }
            else
            {
                if (cmdInfo.trigger().equalsIgnoreCase(trigger) && cmdInfo.command().equalsIgnoreCase(command))
                    return commandMap.get(cmdInfo);
            }

        }
        return null;
    }
}
