package moe.vwinter.utils.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A specialized menu that uses an anvil interface to get text input from a player.
 * The player can type text by renaming the input item, and the result is captured
 * when they click the output slot.
 *
 * <p>Example usage:
 * <pre>{@code
 * AnvilInputMenu.create(player, "Enter your name", name -> {
 *     player.sendMessage("Your name is: " + name);
 * }).open(player);
 * }</pre>
 */
public class AnvilInputMenu implements Menu, Listener {

    private final UUID uuid;
    @SuppressWarnings("unused")
    private final Player player;
    private final Plugin plugin;
    private final String title;
    private final ItemStack inputItem;
    private final Consumer<String> inputHandler;
    private final Consumer<Player> cancelHandler;
    private final Set<Player> viewers;
    private Inventory inventory;
    private boolean registered;
    private boolean programmaticClose;

    /**
     * Creates a new AnvilInputMenu with a default paper item.
     *
     * @param player The player to show the input menu to.
     * @param title The title of the anvil menu (Note: not supported in 1.8.8).
     * @param inputHandler The callback to execute when text is submitted.
     * @param cancelHandler The callback to execute when the menu is closed without submission.
     */
    public AnvilInputMenu(Player player, String title, Consumer<String> inputHandler, Consumer<Player> cancelHandler) {
        this.uuid = UUID.randomUUID();
        this.player = player;
        this.plugin = MenuManager.getInstance().getPlugin();
        this.title = title;
        this.inputHandler = inputHandler;
        this.cancelHandler = cancelHandler;
        this.viewers = new HashSet<>();
        this.registered = false;
        this.programmaticClose = false;

        // Create default input item
        this.inputItem = new ItemStack(Material.PAPER);
        ItemMeta meta = inputItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("Enter text...");
            inputItem.setItemMeta(meta);
        }

        // Anvil titles are not supported in 1.8.8, this will use the default title
        this.inventory = Bukkit.createInventory(null, InventoryType.ANVIL);
        initializeInventory();
    }

    /**
     * Creates a new AnvilInputMenu with a custom input item.
     *
     * @param player The player to show the input menu to.
     * @param title The title of the anvil menu (Note: not supported in 1.8.8).
     * @param inputItem The item to be placed in the input slot.
     * @param inputHandler The callback to execute when text is submitted.
     * @param cancelHandler The callback to execute when the menu is closed without submission.
     */
    public AnvilInputMenu(Player player, String title, ItemStack inputItem,
                         Consumer<String> inputHandler, Consumer<Player> cancelHandler) {
        this.uuid = UUID.randomUUID();
        this.player = player;
        this.plugin = MenuManager.getInstance().getPlugin();
        this.title = title;
        this.inputItem = inputItem;
        this.inputHandler = inputHandler;
        this.cancelHandler = cancelHandler;
        this.viewers = new HashSet<>();
        this.registered = false;
        this.programmaticClose = false;

        // Anvil titles are not supported in 1.8.8, this will use the default title
        this.inventory = Bukkit.createInventory(null, InventoryType.ANVIL);
        initializeInventory();
    }

    private void initializeInventory() {
        // Set the input item in the first slot
        inventory.setItem(0, inputItem);
    }

    @Override
    public void open(Player player) {
        open(player, 1); // Page parameter is ignored for anvil menus
    }

    @Override
    public void open(Player player, int page) {
        if (!registered) {
            register();
        }

        programmaticClose = false;
        player.openInventory(inventory);
        viewers.add(player);
        MenuManager.getInstance().registerMenu(player, this);
    }

    @Override
    public void close(Player player) {
        if (viewers.contains(player)) {
            programmaticClose = true;
            player.closeInventory();
            viewers.remove(player);
            MenuManager.getInstance().unregisterMenu(player);
        }
    }

    /**
     * Handles clicks within the anvil inventory.
     * Captures the renamed item's name from the result slot.
     *
     * @param event The {@link InventoryClickEvent}.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player clicker = (Player) event.getWhoClicked();
        if (!viewers.contains(clicker) || !event.getInventory().equals(inventory)) {
            return;
        }

        event.setCancelled(true);

        // Check if this is an anvil inventory and the result slot was clicked
        if (event.getInventory() instanceof AnvilInventory && event.getRawSlot() == 2) {
            ItemStack result = event.getCurrentItem();
            if (result != null && result.hasItemMeta()) {
                ItemMeta meta = result.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String inputText = meta.getDisplayName();
                    if (inputHandler != null) {
                        inputHandler.accept(inputText);
                    }
                    close(clicker);
                }
            }
        }
    }

    /**
     * Handles the closing of the anvil inventory.
     * Triggers the cancel handler if the close was not initiated by the plugin.
     *
     * @param event The {@link InventoryCloseEvent}.
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player closer = (Player) event.getPlayer();
        if (viewers.contains(closer) && event.getInventory().equals(inventory)) {
            // Only trigger cancel handler if the menu wasn't closed programmatically
            if (cancelHandler != null && !programmaticClose) {
                cancelHandler.accept(closer);
            }
            unregister();
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
    }

    // The following methods are part of the Menu interface but are not applicable
    // to anvil menus, so they throw UnsupportedOperationException or return default values.

    @Override
    public int getCurrentPage() {
        return 1; // Anvil menus don't support pagination
    }

    @Override
    public int getTotalPages() {
        return 1; // Anvil menus don't support pagination
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        throw new UnsupportedOperationException("Cannot change title of anvil menu after creation");
    }

    @Override
    public int getSize() {
        return inventory.getSize();
    }

    @Override
    public List<ItemStack> getPageItems() {
        throw new UnsupportedOperationException("Anvil menus don't support pagination");
    }

    @Override
    public List<ItemStack> getAllItems() {
        throw new UnsupportedOperationException("Anvil menus don't support item lists");
    }

    @Override
    public void setItems(List<ItemStack> items) {
        throw new UnsupportedOperationException("Anvil menus don't support setting items");
    }

    @Override
    public void addItem(ItemStack item) {
        throw new UnsupportedOperationException("Anvil menus don't support adding items");
    }

    @Override
    public void removeItem(ItemStack item) {
        throw new UnsupportedOperationException("Anvil menus don't support removing items");
    }

    @Override
    public void clearItems() {
        throw new UnsupportedOperationException("Anvil menus don't support clearing items");
    }

    @Override
    public Map<Integer, ItemStack> getNavigationItems() {
        throw new UnsupportedOperationException("Anvil menus don't support navigation items");
    }

    @Override
    public void setNavigationItem(int slot, ItemStack item) {
        throw new UnsupportedOperationException("Anvil menus don't support navigation items");
    }

    @Override
    public Consumer<InventoryClickEvent> getAction(int slot) {
        return null; // Anvil menus handle clicks internally
    }

    @Override
    public void setAction(int slot, Consumer<InventoryClickEvent> action) {
        throw new UnsupportedOperationException("Anvil menus don't support custom slot actions");
    }

    @Override
    public void setAction(ItemStack item, Consumer<InventoryClickEvent> action) {
        throw new UnsupportedOperationException("Anvil menus don't support custom item actions");
    }

    @Override
    public void refresh() {
        // No-op for anvil menus
    }

    @Override
    public void update() {
        // No-op for anvil menus
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public boolean isOpen() {
        return !viewers.isEmpty();
    }

    @Override
    public int getItemsPerPage() {
        return 1; // Anvil menus don't support pagination
    }

    @Override
    public void setItemsPerPage(int itemsPerPage) {
        throw new UnsupportedOperationException("Anvil menus don't support pagination");
    }

    @Override
    public int[] getContentSlots() {
        return new int[0]; // Anvil menus don't have content slots
    }

    @Override
    public void setContentSlots(int[] slots) {
        throw new UnsupportedOperationException("Anvil menus don't support content slots");
    }

    @Override
    public int[] getNavigationSlots() {
        return new int[0]; // Anvil menus don't have navigation slots
    }

    @Override
    public void setNavigationSlots(int[] slots) {
        throw new UnsupportedOperationException("Anvil menus don't support navigation slots");
    }

    @Override
    public void nextPage() {
        throw new UnsupportedOperationException("Anvil menus don't support pagination");
    }

    @Override
    public void previousPage() {
        throw new UnsupportedOperationException("Anvil menus don't support pagination");
    }

    @Override
    public boolean goToPage(int page) {
        throw new UnsupportedOperationException("Anvil menus don't support pagination");
    }

    /**
     * Gets the unique ID of this menu instance.
     *
     * @return The menu's UUID.
     */
    public UUID getUniqueId() {
        return uuid;
    }

    /**
     * Gets a clone of the input item used in the anvil.
     *
     * @return The input item.
     */
    public ItemStack getInputItem() {
        return inputItem.clone();
    }

    /**
     * Sets the input item in the first slot of the anvil.
     *
     * @param inputItem The new input item.
     */
    public void setInputItem(ItemStack inputItem) {
        this.inventory.setItem(0, inputItem);
    }

    /**
     * Static factory method to create a simple AnvilInputMenu with only an input handler.
     *
     * @param player The player to show the menu to.
     * @param title The menu title.
     * @param inputHandler The callback for the submitted text.
     * @return A new {@link AnvilInputMenu} instance.
     */
    public static AnvilInputMenu create(Player player, String title, Consumer<String> inputHandler) {
        return new AnvilInputMenu(player, title, inputHandler, null);
    }

    /**
     * Static factory method to create an AnvilInputMenu with both input and cancel handlers.
     *
     * @param player The player to show the menu to.
     * @param title The menu title.
     * @param inputHandler The callback for the submitted text.
     * @param cancelHandler The callback for when the menu is cancelled.
     * @return A new {@link AnvilInputMenu} instance.
     */
    public static AnvilInputMenu create(Player player, String title,
                                       Consumer<String> inputHandler, Consumer<Player> cancelHandler) {
        return new AnvilInputMenu(player, title, inputHandler, cancelHandler);
    }

    /**
     * Static factory method to create an AnvilInputMenu with a custom input item.
     *
     * @param player The player to show the menu to.
     * @param title The menu title.
     * @param inputItem The custom item for the input slot.
     * @param inputHandler The callback for the submitted text.
     * @return A new {@link AnvilInputMenu} instance.
     */
    public static AnvilInputMenu create(Player player, String title, ItemStack inputItem,
                                       Consumer<String> inputHandler) {
        return new AnvilInputMenu(player, title, inputItem, inputHandler, null);
    }
}