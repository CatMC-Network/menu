package moe.vwinter.utils.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Represents a generic, interactive menu in a Bukkit inventory.
 * This interface defines the core functionality for all menu types,
 * including opening, closing, item management, and event handling.
 */
public interface Menu {

    /**
     * Opens the menu for the specified player, displaying the first page.
     *
     * @param player The player to open the menu for.
     */
    void open(Player player);

    /**
     * Opens the menu for the specified player at a specific page.
     *
     * @param player The player to open the menu for.
     * @param page The page number to open (1-based).
     */
    void open(Player player, int page);

    /**
     * Closes the menu for the specified player.
     *
     * @param player The player to close the menu for.
     */
    void close(Player player);

    /**
     * Gets the current page number the menu is displaying.
     *
     * @return The current page number (1-based).
     */
    int getCurrentPage();

    /**
     * Gets the total number of pages in the menu.
     *
     * @return The total number of pages.
     */
    int getTotalPages();

    /**
     * Gets the title of the menu inventory.
     *
     * @return The menu title.
     */
    String getTitle();

    /**
     * Sets the title of the menu inventory.
     * This may not be supported by all menu types after creation.
     *
     * @param title The new menu title.
     */
    void setTitle(String title);

    /**
     * Gets the size of the menu inventory (number of slots).
     *
     * @return The inventory size.
     */
    int getSize();

    /**
     * Gets the list of items to be displayed on the current page.
     *
     * @return A list of {@link ItemStack}s for the current page.
     */
    List<ItemStack> getPageItems();

    /**
     * Gets all items across all pages of the menu.
     *
     * @return A list of all {@link ItemStack}s in the menu.
     */
    List<ItemStack> getAllItems();

    /**
     * Sets the items for the menu, replacing any existing items.
     *
     * @param items The list of {@link ItemStack}s to display.
     */
    void setItems(List<ItemStack> items);

    /**
     * Adds a single item to the menu.
     *
     * @param item The {@link ItemStack} to add.
     */
    void addItem(ItemStack item);

    /**
     * Removes a specific item from the menu.
     *
     * @param item The {@link ItemStack} to remove.
     */
    void removeItem(ItemStack item);

    /**
     * Clears all items from the menu.
     */
    void clearItems();

    /**
     * Gets the map of navigation items and their corresponding slots.
     *
     * @return A map where keys are slot indices and values are navigation {@link ItemStack}s.
     */
    Map<Integer, ItemStack> getNavigationItems();

    /**
     * Sets a navigation item at a specific slot in the inventory.
     *
     * @param slot The inventory slot index.
     * @param item The navigation {@link ItemStack} to set.
     */
    void setNavigationItem(int slot, ItemStack item);

    /**
     * Gets the click action handler for a specific inventory slot.
     *
     * @param slot The inventory slot index.
     * @return The {@link Consumer} for the click event, or null if no action is set.
     */
    Consumer<InventoryClickEvent> getAction(int slot);

    /**
     * Sets a click action handler for a specific inventory slot.
     *
     * @param slot The inventory slot index.
     * @param action The {@link Consumer} to handle the click event.
     */
    void setAction(int slot, Consumer<InventoryClickEvent> action);

    /**
     * Sets a click action handler for a specific item.
     * Note: This may not be reliable if multiple identical items exist.
     *
     * @param item The {@link ItemStack} to associate the action with.
     * @param action The {@link Consumer} to handle the click event.
     */
    void setAction(ItemStack item, Consumer<InventoryClickEvent> action);

    /**
     * Refreshes the menu content for all viewers, re-displaying items for the current page.
     */
    void refresh();

    /**
     * A more generic update method, which may be used for more complex updates.
     * By default, this can be equivalent to {@link #refresh()}.
     */
    void update();

    /**
     * Gets the underlying Bukkit {@link Inventory} instance for this menu.
     *
     * @return The inventory instance.
     */
    Inventory getInventory();

    /**
     * Checks if the menu is currently open for any player.
     *
     * @return True if at least one player is viewing the menu, false otherwise.
     */
    boolean isOpen();

    /**
     * Gets the number of items displayed per page.
     *
     * @return The number of items per page.
     */
    int getItemsPerPage();

    /**
     * Sets the number of items to be displayed per page.
     *
     * @param itemsPerPage The number of items per page.
     */
    void setItemsPerPage(int itemsPerPage);

    /**
     * Gets the array of slot indices designated for content items.
     *
     * @return An array of content slot numbers.
     */
    int[] getContentSlots();

    /**
     * Sets the array of slot indices to be used for content items.
     *
     * @param slots An array of content slot numbers.
     */
    void setContentSlots(int[] slots);

    /**
     * Gets the array of slot indices designated for navigation controls.
     *
     * @return An array of navigation slot numbers.
     */
    int[] getNavigationSlots();

    /**
     * Sets the array of slot indices to be used for navigation controls.
     *
     * @param slots An array of navigation slot numbers.
     */
    void setNavigationSlots(int[] slots);

    /**
     * Navigates to the next page, if one is available.
     */
    void nextPage();

    /**
     * Navigates to the previous page, if one is available.
     */
    void previousPage();

    /**
     * Navigates to a specific page number.
     *
     * @param page The page number to navigate to (1-based).
     * @return True if the navigation was successful, false if the page does not exist.
     */
    boolean goToPage(int page);

    /**
     * Registers the menu's event listeners with the {@link MenuManager}.
     * This is often handled automatically but can be called manually if needed.
     */
    void register();

    /**
     * Unregisters the menu's event listeners from the {@link MenuManager}.
     * This should be called when the menu is no longer needed to prevent memory leaks.
     */
    void unregister();
}