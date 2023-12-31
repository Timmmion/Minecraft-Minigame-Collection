package com.timmmion.ForceItem;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.timmmion.Main;

public class Skip implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Main.getPlugin(Main.class).forceItemManager.playerSkipEvent(sender);
        return false;
    }
}
