package moe.vwinter.utils.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages all active menus and handles Bukkit events related to them.
 * This class is a singleton and must be initialized by the plugin.
 * It tracks which menu is open for each player and forwards inventory events
 * to the appropriate menu instance.
 *
 * <p>Initialization example in your plugin's {@code onEnable}:
 * <pre>{@code
 * MenuManager.getInstance().initialize(this);
 * }</pre>
 */
public class MenuManager implements Listener {

    private static MenuManager instance;
    private final Map<UUID, Menu> playerMenus = new HashMap<>();
    private final Map<UUID, Menu> registeredMenus = new HashMap<>();
    private Plugin plugin;

    private MenuManager() {
        // Private constructor for singleton pattern
    }

    /**
     * Gets the singleton instance of the MenuManager.
     *
     * @return The single instance of MenuManager.
     */
    public static MenuManager getInstance() {
        if (instance == null) {
            instance = new MenuManager();
        }
        return instance;
    }

    /**
     * Initializes the MenuManager and registers its event listeners.
     * This method must be called once from your plugin's onEnable method.
     *
     * @param plugin The instance of your plugin.
     */
    public void initialize(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Registers a menu as being open for a specific player.
     * This is typically called from the {@link Menu#open(Player)} method.
     *
     * @param player The player who is viewing the menu.
     * @param menu The menu being viewed.
     */
    public void registerMenu(Player player, Menu menu) {
        playerMenus.put(player.getUniqueId(), menu);
    }

    /**
     * Registers a menu globally, allowing it to be accessed without a player context.
     * This is useful for menus that are not tied to a specific player's interaction.
     *
     * @param menu The menu to register.
     */
    public void registerMenu(Menu menu) {
        if (menu instanceof PaginatedMenu) {
            registeredMenus.put(((PaginatedMenu) menu).getUniqueId(), menu);
        } else if (menu instanceof AnvilInputMenu) {
            registeredMenus.put(((AnvilInputMenu) menu).getUniqueId(), menu);
        }
    }

    /**
     * Unregisters a menu for a specific player, typically when they close it.
     *
     * @param player The player who is no longer viewing the menu.
     */
    public void unregisterMenu(Player player) {
        playerMenus.remove(player.getUniqueId());
    }

    /**
     * Unregisters a globally registered menu.
     *
     * @param menu The menu to unregister.
     */
    public void unregisterMenu(Menu menu) {
        if (menu instanceof PaginatedMenu) {
            registeredMenus.remove(((PaginatedMenu) menu).getUniqueId());
        } else if (menu instanceof AnvilInputMenu) {
            registeredMenus.remove(((AnvilInputMenu) menu).getUniqueId());
        }
    }

    /**
     * Gets the menu that is currently open for a specific player.
     *
     * @param player The player to check.
     * @return The {@link Menu} the player has open, or null if none.
     */
    public Menu getOpenMenu(Player player) {
        return playerMenus.get(player.getUniqueId());
    }

    /**
     * Checks if a player currently has any menu open.
     *
     * @param player The player to check.
     * @return True if the player has a menu open, false otherwise.
     */
    public boolean hasMenuOpen(Player player) {
        return playerMenus.containsKey(player.getUniqueId());
    }

    /**
     * Closes all menus that are currently open for all online players.
     */
    public void closeAllMenus() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Menu menu = getOpenMenu(player);
            if (menu != null) {
                menu.close(player);
            }
        }
        playerMenus.clear();
    }

    /**
     * Unregisters all globally registered menus and closes all open menus.
     * Should be called in the plugin's {@code onDisable} method to prevent memory leaks.
     */
    public void unregisterAllMenus() {
        closeAllMenus();
        registeredMenus.values().forEach(Menu::unregister);
        registeredMenus.clear();
    }

    /**
     * Handles inventory click events and routes them to the appropriate menu.
     *
     * @param event The inventory click event.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Menu menu = getOpenMenu(player);

        if (menu != null && event.getInventory().equals(menu.getInventory())) {
            // The actual click handling is now done within the menu classes themselves
            // This listener just identifies the correct menu.
        }
    }

    /**
     * Handles inventory close events to clean up menu references.
     *
     * @param event The inventory close event.
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        Menu menu = getOpenMenu(player);

        if (menu != null && event.getInventory().equals(menu.getInventory())) {
            unregisterMenu(player);
        }
    }

    /**
     * Handles player quit events to clean up menu references.
     *
     * @param event The player quit event.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        unregisterMenu(player);
    }

    /**
     * Gets the plugin instance associated with the MenuManager.
     *
     * @return The plugin instance.
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Gets the number of menus currently open by players.
     *
     * @return The number of open menus.
     */
    public int getOpenMenuCount() {
        return playerMenus.size();
    }

    /**
     * Gets the number of globally registered menus.
     *
     * @return The number of registered menus.
     */
    public int getRegisteredMenuCount() {
        return registeredMenus.size();
    }

    /**
     * Runs a task on the main server thread using Bukkit's scheduler.
     * This is a convenience method for menu operations that need to be thread-safe.
     *
     * @param runnable The task to run.
     */
    public void runTask(Runnable runnable) {
        if (plugin != null) {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    /**
     * Runs a task asynchronously using Bukkit's scheduler.
     * Useful for performance-intensive operations like loading menu content
     * from a database or file.
     *
     * @param runnable The task to run asynchronously.
     */
    public void runTaskAsync(Runnable runnable) {
        if (plugin != null) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }

    /**
     * Runs a task on the main server thread after a specified delay.
     *
     * @param runnable The task to run.
     * @param delay The delay in server ticks (20 ticks = 1 second).
     */
    public void runTaskLater(Runnable runnable, long delay) {
        if (plugin != null) {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
        }
    }

    /**
     * Runs a task asynchronously after a specified delay.
     *
     * @param runnable The task to run.
     * @param delay The delay in server ticks (20 ticks = 1 second).
     */
    public void runTaskLaterAsync(Runnable runnable, long delay) {
        if (plugin != null) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
        }
    }
}