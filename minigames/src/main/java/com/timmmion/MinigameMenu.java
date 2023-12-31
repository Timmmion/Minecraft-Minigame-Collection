package com.timmmion;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MinigameMenu implements CommandExecutor, Listener {

    Inventory invInterface = Bukkit.createInventory(null, 27, "Minigames");
    ItemStack forceItemSelector = new ItemStack(Material.CARROT);
    ItemStack emptySpace = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    String BOLD = ChatColor.BOLD + "";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        createItems();
        openMenu((Player) sender);
        return false;
    }

    public void createItems() {
        ItemMeta forceItemMeta = (ItemMeta) forceItemSelector.getItemMeta();
        forceItemMeta.setDisplayName(ChatColor.GREEN + BOLD + "ForceItem Battle");
        forceItemSelector.setItemMeta(forceItemMeta);

        ItemMeta emptySpaceMeta = (ItemMeta) emptySpace.getItemMeta();
        emptySpaceMeta.setDisplayName(" ");
        emptySpace.setItemMeta(emptySpaceMeta);

    }

    public void openMenu(Player p) {
        for (int i = 0; i < 27; i++) {
            invInterface.setItem(i, emptySpace);
        }
        invInterface.setItem(13, forceItemSelector);
        p.openInventory(invInterface);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        Inventory inventory = event.getInventory();
        if (inventory.getLocation() == invInterface.getLocation() && inventory != null) {
            if (clicked.getType() == forceItemSelector.getType()) {
                event.setCancelled(true);

                player.closeInventory();
                Main.getPlugin(Main.class).forceItemManager.startGame();
            } else {
                event.setCancelled(true);
            }
        }
    }

}
