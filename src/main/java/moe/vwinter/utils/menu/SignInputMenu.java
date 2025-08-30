package moe.vwinter.utils.menu;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.PacketPlayOutOpenSignEditor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SignInputMenu implements Menu, Listener {

    private final UUID uuid;
    @SuppressWarnings("unused")
    private final Player player;
    private final Plugin plugin;
    private final String[] lines;
    private final Consumer<List<String>> inputHandler;
    private final Consumer<Player> cancelHandler;
    private final Set<Player> viewers;
    private Location signLocation;
    private boolean registered;

    public SignInputMenu(Player player, String[] lines, Consumer<List<String>> inputHandler, Consumer<Player> cancelHandler) {
        this.uuid = UUID.randomUUID();
        this.player = player;
        this.plugin = MenuManager.getInstance().getPlugin();
        this.lines = lines;
        this.inputHandler = inputHandler;
        this.cancelHandler = cancelHandler;
        this.viewers = new HashSet<>();
        this.registered = false;
    }

    @Override
    public void open(Player player) {
        open(player, 1);
    }

    @Override
    public void open(Player player, int page) {
        if (!registered) {
            register();
        }

        // Find a suitable location for the sign
        signLocation = player.getLocation().clone().add(0, 2, 0);
        Block block = signLocation.getBlock();
        block.setType(Material.SIGN_POST);

        Sign sign = (Sign) block.getState();
        for (int i = 0; i < lines.length; i++) {
            if (i < 4) {
                sign.setLine(i, lines[i]);
            }
        }
        sign.update();

        CraftPlayer craftPlayer = (CraftPlayer) player;
        BlockPosition blockPosition = new BlockPosition(signLocation.getBlockX(), signLocation.getBlockY(), signLocation.getBlockZ());
        PacketPlayOutOpenSignEditor packet = new PacketPlayOutOpenSignEditor(blockPosition);
        craftPlayer.getHandle().playerConnection.sendPacket(packet);

        viewers.add(player);
        MenuManager.getInstance().registerMenu(player, this);
    }

    @Override
    public void close(Player player) {
        if (viewers.contains(player)) {
            viewers.remove(player);
            MenuManager.getInstance().unregisterMenu(player);
            resetSign();
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (!viewers.contains(event.getPlayer()) || !event.getBlock().getLocation().equals(signLocation)) {
            return;
        }

        List<String> input = Arrays.stream(event.getLines())
                .map(line -> line.replaceAll("\"", ""))
                .collect(Collectors.toList());

        if (inputHandler != null) {
            Bukkit.getScheduler().runTask(plugin, () -> inputHandler.accept(input));
        }
        close(event.getPlayer());
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            if (viewers.contains(player)) {
                if (cancelHandler != null) {
                    cancelHandler.accept(player);
                }
                close(player);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (viewers.contains(event.getPlayer())) {
            if (cancelHandler != null) {
                cancelHandler.accept(event.getPlayer());
            }
            close(event.getPlayer());
        }
    }

    private void resetSign() {
        if (signLocation != null) {
            signLocation.getBlock().setType(Material.AIR);
            signLocation = null;
        }
    }

    @Override
    public void register() {
        if (!registered) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            registered = true;
        }
    }

    @Override
    public void unregister() {
        if (registered) {
            HandlerList.unregisterAll(this);
            registered = false;
        }
        viewers.clear();
        resetSign();
    }

    @Override
    public int getCurrentPage() {
        return 1;
    }

    @Override
    public int getTotalPages() {
        return 1;
    }

    @Override
    public String getTitle() {
        return ""; // Not applicable
    }

    @Override
    public void setTitle(String title) {
        throw new UnsupportedOperationException("Cannot change title of sign menu");
    }

    @Override
    public int getSize() {
        return 0; // Not applicable
    }

    @Override
    public List<ItemStack> getPageItems() {
        throw new UnsupportedOperationException("Sign menus don't support pagination");
    }

    @Override
    public List<ItemStack> getAllItems() {
        throw new UnsupportedOperationException("Sign menus don't support item lists");
    }

    @Override
    public void setItems(List<ItemStack> items) {
        throw new UnsupportedOperationException("Sign menus don't support setting items");
    }

    @Override
    public void addItem(ItemStack item) {
        throw new UnsupportedOperationException("Sign menus don't support adding items");
    }

    @Override
    public void removeItem(ItemStack item) {
        throw new UnsupportedOperationException("Sign menus don't support removing items");
    }

    @Override
    public void clearItems() {
        throw new UnsupportedOperationException("Sign menus don't support clearing items");
    }

    @Override
    public Map<Integer, ItemStack> getNavigationItems() {
        throw new UnsupportedOperationException("Sign menus don't support navigation items");
    }

    @Override
    public void setNavigationItem(int slot, ItemStack item) {
        throw new UnsupportedOperationException("Sign menus don't support navigation items");
    }

    @Override
    public Consumer<InventoryClickEvent> getAction(int slot) {
        return null; // Not applicable
    }

    @Override
    public void setAction(int slot, Consumer<InventoryClickEvent> action) {
        throw new UnsupportedOperationException("Sign menus don't support custom slot actions");
    }

    @Override
    public void setAction(ItemStack item, Consumer<InventoryClickEvent> action) {
        throw new UnsupportedOperationException("Sign menus don't support custom item actions");
    }

    @Override
    public void refresh() {
        // No-op
    }

    @Override
    public void update() {
        // No-op
    }

    @Override
    public Inventory getInventory() {
        return null; // Not applicable
    }

    @Override
    public boolean isOpen() {
        return !viewers.isEmpty();
    }

    @Override
    public int getItemsPerPage() {
        return 1;
    }

    @Override
    public void setItemsPerPage(int itemsPerPage) {
        throw new UnsupportedOperationException("Sign menus don't support pagination");
    }

    @Override
    public int[] getContentSlots() {
        return new int[0];
    }

    @Override
    public void setContentSlots(int[] slots) {
        throw new UnsupportedOperationException("Sign menus don't support content slots");
    }

    @Override
    public int[] getNavigationSlots() {
        return new int[0];
    }

    @Override
    public void setNavigationSlots(int[] slots) {
        throw new UnsupportedOperationException("Sign menus don't support navigation slots");
    }

    @Override
    public void nextPage() {
        throw new UnsupportedOperationException("Sign menus don't support pagination");
    }

    @Override
    public void previousPage() {
        throw new UnsupportedOperationException("Sign menus don't support pagination");
    }

    @Override
    public boolean goToPage(int page) {
        throw new UnsupportedOperationException("Sign menus don't support pagination");
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public static SignInputMenu create(Player player, String[] lines, Consumer<List<String>> inputHandler) {
        return new SignInputMenu(player, lines, inputHandler, null);
    }

    public static SignInputMenu create(Player player, String[] lines,
                                       Consumer<List<String>> inputHandler, Consumer<Player> cancelHandler) {
        return new SignInputMenu(player, lines, inputHandler, cancelHandler);
    }
}