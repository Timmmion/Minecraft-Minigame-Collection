package com.timmmion;

import java.util.Objects;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import com.timmmion.ForceItem.ForceItemManager;
import com.timmmion.ForceItem.IllegalitemSkip;
import com.timmmion.ForceItem.Skip;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin {
  private static final Logger LOGGER = Logger.getLogger("minigames");
  public ForceItemManager forceItemManager = new ForceItemManager();
  public static String PREFIX = ChatColor.YELLOW + "[Catlorant] " + ChatColor.GRAY + "";

  public void onEnable() {
    LOGGER.info("minigame plugin by Timmmion enabled");
    registerCommands();
    registerEvents();
  }

  public void registerEvents() {
    getServer().getPluginManager().registerEvents(new MinigameMenu(), this);
    getServer().getPluginManager().registerEvents(forceItemManager, this);
  }

  public void registerCommands() {
    Objects.requireNonNull(getCommand("minigames")).setExecutor(new MinigameMenu());
    Objects.requireNonNull(getCommand("itemSkip")).setExecutor(new Skip());
    Objects.requireNonNull(getCommand("illegal")).setExecutor(new IllegalitemSkip());
  }

  public void onDisable() {
    LOGGER.info("minigame plugin by Timmmion disabled");
  }

  public void createNewForceItemManager(){
    forceItemManager = new ForceItemManager();
  }

}
