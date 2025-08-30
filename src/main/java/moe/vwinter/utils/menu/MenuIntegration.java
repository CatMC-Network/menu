package moe.vwinter.utils.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class MenuIntegration {

    private static MenuIntegration instance;
    private final Plugin plugin;
    private final MenuManager menuManager;

    /**
     * Creates a new MenuIntegration instance.
     *
     * @param plugin the plugin instance
     */
    private MenuIntegration(Plugin plugin) {
        this.plugin = plugin;
        this.menuManager = MenuManager.getInstance();
    }

    /**
     * Initializes the menu system for the plugin.
     * Must be called during plugin startup.
     *
     * @param plugin the plugin instance
     * @return the MenuIntegration instance
     */
    public static MenuIntegration initialize(Plugin plugin) {
        if (instance == null) {
            instance = new MenuIntegration(plugin);
            instance.menuManager.initialize(plugin);
        }
        return instance;
    }

    /**
     * Gets the MenuIntegration instance.
     *
     * @return the MenuIntegration instance
     * @throws IllegalStateException if not initialized
     */
    public static MenuIntegration getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MenuIntegration not initialized. Call initialize() first.");
        }
        return instance;
    }

    /**
     * Gets the MenuManager instance.
     *
     * @return the MenuManager instance
     */
    public MenuManager getMenuManager() {
        return menuManager;
    }

    /**
     * Gets the plugin instance.
     *
     * @return the plugin instance
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Shuts down the menu system.
     * Should be called during plugin shutdown.
     */
    public void shutdown() {
        menuManager.unregisterAllMenus();
        menuManager.closeAllMenus();
    }

    /**
     * Creates a new MenuBuilder with default settings.
     *
     * @return a new MenuBuilder
     */
    public MenuBuilder createMenu() {
        return MenuBuilder.create();
    }

    /**
     * Creates a new MenuBuilder with the specified title.
     *
     * @param title the menu title
     * @return a new MenuBuilder
     */
    public MenuBuilder createMenu(String title) {
        return MenuBuilder.create(title);
    }

    /**
     * Creates a new MenuBuilder with the specified title and size.
     *
     * @param title the menu title
     * @param size the menu size
     * @return a new MenuBuilder
     */
    public MenuBuilder createMenu(String title, int size) {
        return MenuBuilder.create(title, size);
    }

    /**
     * Creates a simple item list menu.
     *
     * @param title the menu title
     * @param items the items to display
     * @return a configured MenuBuilder
     */
    public MenuBuilder createItemListMenu(String title, java.util.List<org.bukkit.inventory.ItemStack> items) {
        return MenuBuilder.itemList(title, items);
    }

    /**
     * Creates a simple item list menu.
     *
     * @param title the menu title
     * @param items the items to display
     * @return a configured MenuBuilder
     */
    public MenuBuilder createItemListMenu(String title, org.bukkit.inventory.ItemStack... items) {
        return MenuBuilder.itemList(title, items);
    }

    /**
     * Creates a settings menu.
     *
     * @param title the menu title
     * @return a configured MenuBuilder for settings
     */
    public MenuBuilder createSettingsMenu(String title) {
        return MenuBuilder.settings(title);
    }

    /**
     * Creates a confirmation dialog menu.
     *
     * @param title the menu title
     * @param confirmAction the action to execute on confirm
     * @param cancelAction the action to execute on cancel
     * @return a configured MenuBuilder for confirmation
     */
    public MenuBuilder createConfirmationMenu(String title, java.util.function.Consumer<org.bukkit.entity.Player> confirmAction,
                                             java.util.function.Consumer<org.bukkit.entity.Player> cancelAction) {
        return MenuBuilder.confirmation(title, confirmAction, cancelAction);
    }

    /**
     * Gets the number of currently open menus.
     *
     * @return the number of open menus
     */
    public int getOpenMenuCount() {
        return menuManager.getOpenMenuCount();
    }

    /**
     * Gets the number of registered menus.
     *
     * @return the number of registered menus
     */
    public int getRegisteredMenuCount() {
        return menuManager.getRegisteredMenuCount();
    }

    /**
     * Creates an anvil input menu for text input.
     *
     * @param player the player to show the menu to
     * @param title the menu title
     * @param inputHandler the callback for when text is entered
     * @return a new AnvilInputMenu
     */
    public AnvilInputMenu createAnvilInputMenu(Player player, String title, Consumer<String> inputHandler) {
        return AnvilInputMenu.create(player, title, inputHandler);
    }

    /**
     * Creates an anvil input menu with both input and cancel handlers.
     *
     * @param player the player to show the menu to
     * @param title the menu title
     * @param inputHandler the callback for when text is entered
     * @param cancelHandler the callback for when the menu is cancelled
     * @return a new AnvilInputMenu
     */
    public AnvilInputMenu createAnvilInputMenu(Player player, String title,
                                              Consumer<String> inputHandler, Consumer<Player> cancelHandler) {
        return AnvilInputMenu.create(player, title, inputHandler, cancelHandler);
    }

    /**
     * Creates an anvil input menu with a custom input item.
     *
     * @param player the player to show the menu to
     * @param title the menu title
     * @param inputItem the custom input item
     * @param inputHandler the callback for when text is entered
     * @return a new AnvilInputMenu
     */
    public AnvilInputMenu createAnvilInputMenu(Player player, String title, ItemStack inputItem,
                                              Consumer<String> inputHandler) {
        return AnvilInputMenu.create(player, title, inputItem, inputHandler);
    }

    /**
     * Creates an anvil input menu with a custom input item and cancel handler.
     *
     * @param player the player to show the menu to
     * @param title the menu title
     * @param inputItem the custom input item
     * @param inputHandler the callback for when text is entered
     * @param cancelHandler the callback for when the menu is cancelled
     * @return a new AnvilInputMenu
     */
    public AnvilInputMenu createAnvilInputMenu(Player player, String title, ItemStack inputItem,
                                              Consumer<String> inputHandler, Consumer<Player> cancelHandler) {
        return new AnvilInputMenu(player, title, inputItem, inputHandler, cancelHandler);
    }

    /**
     * Closes all open menus.
     */
    public void closeAllMenus() {
        menuManager.closeAllMenus();
    }

    /**
     * Checks if a player has a menu open.
     *
     * @param player the player to check
     * @return true if the player has a menu open
     */
    public boolean hasMenuOpen(org.bukkit.entity.Player player) {
        return menuManager.hasMenuOpen(player);
    }

    /**
     * Gets the menu currently open for a player.
     *
     * @param player the player to check
     * @return the open menu, or null if none
     */
    public Menu getOpenMenu(org.bukkit.entity.Player player) {
        return menuManager.getOpenMenu(player);
    }

    /**
     * Runs a task on the main server thread.
     * Useful for menu operations that need to be thread-safe.
     *
     * @param runnable the task to run
     */
    public void runTask(Runnable runnable) {
        menuManager.runTask(runnable);
    }

    /**
     * Runs a task asynchronously.
     * Useful for loading menu content from external sources.
     *
     * @param runnable the task to run
     */
    public void runTaskAsync(Runnable runnable) {
        menuManager.runTaskAsync(runnable);
    }

    /**
     * Runs a task after a delay on the main server thread.
     *
     * @param runnable the task to run
     * @param delay the delay in ticks
     */
    public void runTaskLater(Runnable runnable, long delay) {
        menuManager.runTaskLater(runnable, delay);
    }

    /**
     * Runs a task asynchronously after a delay.
     *
     * @param runnable the task to run
     * @param delay the delay in ticks
     */
    public void runTaskLaterAsync(Runnable runnable, long delay) {
        menuManager.runTaskLaterAsync(runnable, delay);
    }
}
