package com.timmmion.ForceItem;

import java.util.*;

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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import com.timmmion.Main;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public final class ForceItemManager implements Listener {
    boolean gameRunning = false;
    boolean endgame = false;
    boolean paused = false;
    int timeInSeconds = 3600;
    int startingTime = 0;
    int skips;

    BukkitTask task;
    ArrayList<Material> itemList = new ArrayList<>();
    ArrayList<String> players = new ArrayList<>();
    HashMap<String, ItemStack> activeItem = new HashMap<>();
    HashMap<String, BossBar> itemTitle = new HashMap<>();
    HashMap<String, Integer> points = new HashMap<>();
    HashMap<String, Integer> skipsLeft = new HashMap<>();
    HashMap<String, ArrayList<ItemInfo>> accomplishedItems = new HashMap<String, ArrayList<ItemInfo>>();

    String PREFIX = Main.PREFIX;
    String PRIMARY = Main.PRIMARY;
    String SECONDARY = Main.SECONDARY;
    String BOLD = ChatColor.BOLD + "";

    Inventory invInterface = Bukkit.createInventory(null, 27, "Settings");
    ItemStack startButton = new ItemStack(Material.GREEN_WOOL);
    ItemStack pauseButton = new ItemStack(Material.ORANGE_WOOL);
    ItemStack stopButton = new ItemStack(Material.RED_WOOL);
    ItemStack addTimeButton = new ItemStack(Material.LIME_DYE);
    ItemStack clock = new ItemStack(Material.CLOCK);
    ItemStack removeTimeButton = new ItemStack(Material.RED_DYE);
    ItemStack emptySpace = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);

    public void openMenu(Player player) {
        createItems();

        for (int i = 0; i < 27; i++) {
            invInterface.setItem(i, emptySpace);
        }
        invInterface.setItem(10, startButton);
        invInterface.setItem(11, pauseButton);
        invInterface.setItem(12, stopButton);
        invInterface.setItem(14, addTimeButton);
        invInterface.setItem(15, clock);
        invInterface.setItem(16, removeTimeButton);
        player.openInventory(invInterface);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(event.getClick() == null) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        Inventory inventory = event.getInventory();
        if (inventory.getLocation() == invInterface.getLocation() && inventory != null) {
            if (clicked.getType() == startButton.getType()) {
                event.setCancelled(true);
                player.closeInventory();
                startGame();
            } else if (clicked.getType() == pauseButton.getType()) {
                event.setCancelled(true);
                player.closeInventory();
                pauseGame();
            } else if (clicked.getType() == stopButton.getType()) {
                event.setCancelled(true);
                player.closeInventory();
                endGame();
            } else if (clicked.getType() == addTimeButton.getType()) {
                event.setCancelled(true);
                if (timeInSeconds < 43200) {
                    timeInSeconds = timeInSeconds + 1800;
                    ItemStack customclock = new ItemStack(Material.CLOCK);
                    ItemMeta clockMeta = (ItemMeta) customclock.getItemMeta();
                    clockMeta.setDisplayName(ChatColor.GOLD + BOLD + (timeInSeconds / 60f / 60f) + " hours");
                    customclock.setItemMeta(clockMeta);
                    invInterface.setItem(15, customclock);
                }
            } else if (clicked.getType() == removeTimeButton.getType()) {
                event.setCancelled(true);
                if (timeInSeconds > 1800) {
                    timeInSeconds = timeInSeconds - 1800;
                    ItemStack customclock = new ItemStack(Material.CLOCK);
                    ItemMeta clockMeta = (ItemMeta) customclock.getItemMeta();
                    clockMeta.setDisplayName(ChatColor.GOLD + BOLD + (timeInSeconds / 60f / 60f) + " hours");
                    customclock.setItemMeta(clockMeta);
                    invInterface.setItem(15, customclock);
                }
            }
        }
    }

    public void createItems() {
        ItemMeta startButtonMeta = (ItemMeta) startButton.getItemMeta();
        startButtonMeta.setDisplayName(ChatColor.GREEN + BOLD + "START");
        startButton.setItemMeta(startButtonMeta);

        ItemMeta pauseButtonMeta = (ItemMeta) pauseButton.getItemMeta();
        pauseButtonMeta.setDisplayName(ChatColor.GOLD + BOLD + "PAUSE");
        pauseButton.setItemMeta(pauseButtonMeta);

        ItemMeta stopButtonMeta = (ItemMeta) stopButton.getItemMeta();
        stopButtonMeta.setDisplayName(ChatColor.RED + BOLD + "STOP");
        stopButton.setItemMeta(stopButtonMeta);

        ItemMeta addTimeButtonMeta = (ItemMeta) addTimeButton.getItemMeta();
        addTimeButtonMeta.setDisplayName(ChatColor.GREEN + BOLD + "+30 MIN");
        addTimeButton.setItemMeta(addTimeButtonMeta);

        ItemMeta removeTimeButtonMeta = (ItemMeta) removeTimeButton.getItemMeta();
        removeTimeButtonMeta.setDisplayName(ChatColor.RED + BOLD + "-30 MIN");
        removeTimeButton.setItemMeta(removeTimeButtonMeta);

        ItemMeta clockMeta = (ItemMeta) clock.getItemMeta();
        clockMeta.setDisplayName(ChatColor.GOLD + BOLD + (timeInSeconds / 60f / 60f) + " hours");
        clock.setItemMeta(clockMeta);

        ItemMeta emptySpaceMeta = (ItemMeta) emptySpace.getItemMeta();
        emptySpaceMeta.setDisplayName(" ");
        emptySpace.setItemMeta(emptySpaceMeta);
    }

    // MANAGING GAMEPHASE

    void startGame() {
        if (gameRunning)
            return;

        startingTime = timeInSeconds;

        gameRunning = true;
        players = new ArrayList<>(
                Arrays.asList(Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new)));
        setSurvivalItems();

        skips = startingTime / 3600 * 3;

        for (int i = 0; i < players.size(); i++) {
            activeItem.put(players.get(i), new ItemStack(getRandomElement(itemList), 1));
            points.put(players.get(i), 0);
            skipsLeft.put(players.get(i), skips);
            accomplishedItems.put(players.get(i), new ArrayList<ItemInfo>());
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

    void pauseGame() {
        if (gameRunning) {
            if (!paused) {
                paused = true;
            } else {
                paused = false;
            }
        }
    }

    void endGame() {
        if (!gameRunning)
            return;
        gameRunning = false;

        System.out.println("GAME END TRIGGERD");
        Bukkit.getScheduler().cancelTask(task.getTaskId());
        
        for(int i = 0; i < players.size(); i++){
            itemTitle.get(players.get(i)).setVisible(false);
        }

        ArrayList<String> orderedListPlayers = new ArrayList<>(points.keySet());

        Collections.sort(orderedListPlayers, (player1, player2) -> points.get(player1).compareTo(points.get(player2)));

        int[] playerCounter = { 0 };
        int timeBefore = 0;

        for (String p : orderedListPlayers) {

            int ranking = (players.size() - playerCounter[0]);
            final BukkitTask[] task2 = { null };
            ArrayList<ItemInfo> infoList = accomplishedItems.get(p);
            Inventory results = Bukkit.createInventory(null, 54, "#" + ranking + " " + player(p).getName() + " results");

            ItemStack greenSpace = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta greenSpaceMeta = (ItemMeta) greenSpace.getItemMeta();
            greenSpaceMeta.setDisplayName(" ");
            greenSpace.setItemMeta(greenSpaceMeta);

            for (int u = 0; u < 9; u++) {
                results.setItem(u, greenSpace);
                results.setItem(53 - u, greenSpace);

            }

            int[] itemId = { 0 };
            int[] slotNumber = { 9 };

            task2[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(Main.class), () -> {
                for (int i = 0; i < players.size(); i++) {
                    player(players.get(i)).openInventory(results);
                }

                if (infoList.size() - 1 >= itemId[0]) {
                    if (slotNumber[0] < 44) {
                        results.setItem(slotNumber[0], infoToItem(infoList.get(itemId[0])));
                        itemId[0]++;
                        slotNumber[0]++;
                    } else {
                        for (int i = 0; i < 35; i++) {
                            results.clear(i + 9);
                        }

                        slotNumber[0] = 9;
                        results.setItem(slotNumber[0], infoToItem(infoList.get(itemId[0])));
                        slotNumber[0]++;
                        itemId[0]++;
                    }
                } else {
                    for (int i = 0; i < players.size(); i++) {

                        player(players.get(i)).closeInventory();
                        player(players.get(i)).sendTitle(ChatColor.GREEN + "#" + ranking,
                                ChatColor.GREEN + player(p).getName() + " acquired " + infoList.size() + " items", 10,
                                70,
                                10);
                    }
                    task2[0].cancel();
                }

            }, timeBefore, 20);

            timeBefore += infoList.size() * 20 + 100;

            playerCounter[0]++;
        }

        Main.getPlugin(Main.class).createNewForceItemManager();
    }

    void timer() {
        if (timeInSeconds > 0) {
            int hours = timeInSeconds / 3600;
            int minutes = (timeInSeconds % 3600) / 60;
            int seconds = timeInSeconds % 60;

            String timeString = String.format(ChatColor.YELLOW + BOLD +  " >>> %02d:%02d:%02d <<<", hours, minutes, seconds);

            for (int i = 0; i < players.size(); i++) {
                if (player(players.get(i)) != null) {

                    player(players.get(i)).spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent(timeString));
                }
            }

            if (paused)
                return;
            timeInSeconds--;
        } else {
            endGame();
        }
    }

    void updateItem(String player) {
        points.put(player, points.get(player) + 1);
        activeItem.put(player, new ItemStack(getRandomElement(itemList), 1));
        String name = activeItem.get(player).getType().name().replace("_", " ").toUpperCase();
        itemTitle.get(player).setTitle(name);
        player(player).sendMessage(PREFIX + "Task accomplished! Your next task is to collect '" + PRIMARY
                + name + SECONDARY + "'");
    }

    void playerSkipEvent(CommandSender sender) {

        for (int i = 0; i < players.size(); i++) {
            if (sender.getName().equals(players.get(i))) {
                if (skipsLeft.get(players.get(i)) > 0) {
                skipsLeft.put(players.get(i), skipsLeft.get(players.get(i)) - 1);
                player(players.get(i)).sendMessage(PREFIX + PRIMARY + skipsLeft.get(players.get(i))
                        + SECONDARY + " skip/s left");
                ItemInfo info = new ItemInfo();
                info.setTime(startingTime - timeInSeconds);
                info.setSkipped(true);
                info.setItem(activeItem.get(sender.getName()));
                ArrayList<ItemInfo> list = accomplishedItems.get(players.get(i));
                list.add(info);
                accomplishedItems.put(players.get(i), list);

                updateItem(players.get(i));
                } else {
                player(players.get(i)).sendMessage(PREFIX + "You already used all of your skips!");
                }
            }
        }
    }

    void skipIllegalItem(CommandSender sender) {
        for (int i = 0; i < players.size(); i++) {
            if (sender.getName().equals(players.get(i))) {
                activeItem.put(players.get(i), new ItemStack(getRandomElement(itemList), 1));
                String name = activeItem.get(players.get(i)).getType().name().replace("_", " ").toUpperCase();
                itemTitle.get(players.get(i)).setTitle(name);
                player(players.get(i)).sendMessage(
                        PREFIX + "Task skipped! Your next task is to collect '" + PRIMARY
                                + name + SECONDARY + "'");
                System.out.println(sender.getName() + " skipped " + name);
            }
        }
    }

    // EVENTS

    @EventHandler
    void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntityType() == EntityType.PLAYER))
            return;
        Player p = (Player) event.getEntity();
        for (int i = 0; i < players.size(); i++) {
            if (player(players.get(i)) == p
                    && event.getItem().getItemStack().getType() == activeItem.get(players.get(i)).getType()) {
                ItemInfo info = new ItemInfo();
                info.setTime(startingTime - timeInSeconds);
                info.setSkipped(false);
                info.setItem(event.getItem().getItemStack());
                ArrayList<ItemInfo> list = accomplishedItems.get(players.get(i));
                list.add(info);
                accomplishedItems.put(players.get(i), list);
                updateItem(players.get(i));
            }
        }
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event) {
        if (players.contains(event.getPlayer().getName()))
            itemTitle.get(event.getPlayer().getName()).addPlayer(event.getPlayer());
    }

    @EventHandler
    void stopPlayerMoveWhilePaused(PlayerMoveEvent event) {
        if (paused)
            event.setCancelled(true);
    }

    @EventHandler
    void stopDamageWhilePaused(EntityDamageEvent event) {
        if (paused)
            event.setCancelled(true);
    }

    // GENERIC FUNCTIONS

    void setSurvivalItems() {
        for (Material material : Material.values()) {
            itemList.add(material);
        }
    }

    static <T> T getRandomElement(ArrayList<T> list) {
        if (list != null && !list.isEmpty()) {
            Random random = new Random();
            int randomIndex = random.nextInt(list.size());
            return list.get(randomIndex);
        } else {
            return null;
        }
    }

    Player player(String name) {
        return Bukkit.getPlayer(name);
    }

    ItemStack infoToItem(ItemInfo info) {
        ItemStack item = info.getItem();
        ItemMeta itemMeta = (ItemMeta) item.getItemMeta();

        if (info.getSkipped()) {
            itemMeta.setDisplayName(ChatColor.RED + "SKIPPED");
        } else {
            int time = info.getTime();
            int hours = time / 3600;
            int minutes = (time % 3600) / 60;
            int seconds = time % 60;

            String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

            itemMeta.setDisplayName(ChatColor.GREEN + timeString);
        }

        item.setItemMeta(itemMeta);

        return item;
    }

}

class ItemInfo {
    private int timeAccomplished;
    private boolean skipped;
    private ItemStack item;

    void setTime(int time) {
        timeAccomplished = time;
    }

    void setSkipped(boolean skip) {
        skipped = skip;
    }

    void setItem(ItemStack itemStack) {
        item = itemStack;
    }

    int getTime() {
        return timeAccomplished;
    }

    boolean getSkipped() {
        return skipped;
    }

    ItemStack getItem() {
        return item;
    }
}
