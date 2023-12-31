package com.timmmion.ForceItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import com.timmmion.Main;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public final class ForceItemManager implements Listener {
    boolean gameRunning = false;
    boolean endgame = false;
    int timeInSeconds = 3600; // TIME IN SECONDS
   
    BukkitTask task;
    ArrayList<Material> itemList = new ArrayList<>();
    ArrayList<String> players = new ArrayList<>();
    HashMap<String, ItemStack> activeItem = new HashMap<>();
    HashMap<String, BossBar> itemTitle = new HashMap<>();
    HashMap<String, Integer> points = new HashMap<>();
    HashMap<String, Integer> skipsLeft = new HashMap<>();
    
    String PREFIX = Main.PREFIX;
    String BOLD = ChatColor.BOLD + "";

    public void startGame() {
        gameRunning = true;
        players = new ArrayList<>(
                Arrays.asList(Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)));
        setSurvivalItems();

        for (int i = 0; i < players.size(); i++) {
            activeItem.put(players.get(i), new ItemStack(getRandomElement(itemList), 1));
            points.put(players.get(i), 0);
            skipsLeft.put(players.get(i), 3);
        }

        for (int i = 0; i < players.size(); i++) {
            System.out.println(activeItem.get(players.get(i)).getType().name());
            String name = activeItem.get(players.get(i)).getType().name().replace("_", " ").toUpperCase();
            BossBar bar = Bukkit.createBossBar(name, BarColor.YELLOW,
                    BarStyle.SOLID);
            bar.addPlayer(player(players.get(i)));
            itemTitle.put(players.get(i), bar);
        }

        task = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getPlugin(Main.class), new Runnable() {
            @Override
            public void run() {
                timer();
            }
        }, 20, 20);

    }

    Player player(String name) {
        return Bukkit.getPlayer(name);
    }

    void timer() {

        if (timeInSeconds > 0) {
            int hours = timeInSeconds / 3600;
            int minutes = (timeInSeconds % 3600) / 60;
            int seconds = timeInSeconds % 60;

            String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

            for (int i = 0; i < players.size(); i++) {
                if (player(players.get(i)) != null) {

                    player(players.get(i)).spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent(timeString));
                }
            }

            timeInSeconds--;
        } else {
            endGame();
        }
    }

    void endGame() {
        for (int i = 0; i < players.size(); i++) {
            Bukkit.broadcastMessage(PREFIX + ChatColor.DARK_GRAY + player(players.get(i)).getName() + ChatColor.GRAY
                    + " accomplished " + ChatColor.DARK_GRAY + points.get(players.get(i)) + ChatColor.GRAY + " Task/s");
            itemTitle.get(players.get(i)).setVisible(false);
        }
        Bukkit.getScheduler().cancelTask(task.getTaskId());
        Main.getPlugin(Main.class).createNewForceItemManager();
    }

    void setSurvivalItems() {
        for (Material material : Material.values()) {
            itemList.add(material);
        }
    }

    private static <T> T getRandomElement(ArrayList<T> list) {
        if (list != null && !list.isEmpty()) {
            Random random = new Random();
            int randomIndex = random.nextInt(list.size());
            return list.get(randomIndex);
        } else {
            return null;
        }
    }

    @EventHandler
    void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntityType() == EntityType.PLAYER))
            return;
        Player p = (Player) event.getEntity();
        for (int i = 0; i < players.size(); i++) {
            if (player(players.get(i)) == p
                    && event.getItem().getItemStack().getType() == activeItem.get(players.get(i)).getType()) {
                updateItem(players.get(i));
            }
        }
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event) {
        if (players.contains(event.getPlayer().getName())) {
            itemTitle.get(event.getPlayer().getName()).addPlayer(event.getPlayer());
        }
    }

    void updateItem(String player) {
        points.put(player, points.get(player) + 1);
        activeItem.put(player, new ItemStack(getRandomElement(itemList), 1));
        String name = activeItem.get(player).getType().name().replace("_", " ").toUpperCase();
        itemTitle.get(player).setTitle(name);
        player(player).sendMessage(PREFIX + "Task accomplished! Your next task is to collect '" + ChatColor.DARK_GRAY
                + name + ChatColor.GRAY + "'");
    }

    public void playerSkipEvent(CommandSender sender) {
        for (int i = 0; i < players.size(); i++) {
            if (sender.getName().equals(players.get(i))) {
                if (skipsLeft.get(players.get(i)) > 0) {
                    skipsLeft.put(players.get(i), skipsLeft.get(players.get(i)) - 1);
                    player(players.get(i)).sendMessage(PREFIX + ChatColor.DARK_GRAY + skipsLeft.get(players.get(i))
                            + ChatColor.GRAY + " skip/s left");
                    updateItem(players.get(i));
                } else {
                    player(players.get(i)).sendMessage(PREFIX + "You already used all of your skips!");
                }
            }
        }
    }

    public void skipIllegalItem(CommandSender sender) {
        for (int i = 0; i < players.size(); i++) {
            if (sender.getName().equals(players.get(i))) {
                activeItem.put(players.get(i), new ItemStack(getRandomElement(itemList), 1));
                String name = activeItem.get(players.get(i)).getType().name().replace("_", " ").toUpperCase();
                itemTitle.get(players.get(i)).setTitle(name);
                player(players.get(i)).sendMessage(
                        PREFIX + "Task skipped! Your next task is to collect '" + ChatColor.DARK_GRAY
                                + name + ChatColor.GRAY + "'");
                System.out.println(sender.getName() + " skipped " + name);
            }
        }
    }

}
