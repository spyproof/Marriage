package be.spyproof.marriage.listeners;

import be.spyproof.marriage.handlers.CommandHandler;
import org.bukkit.command.*;
import org.bukkit.command.Command;

/**
 * Created by Spyproof on 4/05/2015.
 * Command handling inspired by https://github.com/Zenith-
 */
public class CommandListener implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
    {
        if (strings.length != 0)
        {
            String[] args = new String[strings.length - 1];
            System.arraycopy(strings, 1, args, 0, args.length);
            CommandHandler.getCommandHandler().callCommand(command.getName(), strings[0], commandSender, args);
        }else {
            CommandHandler.getCommandHandler().callCommand(command.getName(), "", commandSender, strings);
        }

        return true;
    }
}
