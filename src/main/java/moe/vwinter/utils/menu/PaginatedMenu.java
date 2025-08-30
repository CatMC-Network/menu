package moe.vwinter.utils.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Consumer;

/**
 * An abstract implementation of a paginated menu.
 * This class provides the core logic for handling multiple pages of items,
 * navigation, and click events. Subclasses can extend this to create
 * specific menu types.
 */
public abstract class PaginatedMenu implements Menu {

    protected final String title;
    protected final int size;
    protected final UUID uuid;
    protected Inventory inventory;
    protected int currentPage = 1;
    protected List<ItemStack> items = new ArrayList<>();
    protected Map<Integer, Consumer<InventoryClickEvent>> actions = new HashMap<>();
    protected Map<Integer, ItemStack> navigationItems = new HashMap<>();
    protected Set<Player> viewers = new HashSet<>();
    protected int itemsPerPage = 28; // Default content area for 6-row inventory
    protected int[] contentSlots;
    protected int[] navigationSlots = {45, 46, 47, 48, 49, 50, 51, 52, 53}; // Default bottom row

    /**
     * Creates a new paginated menu with the specified title and size.
     *
     * @param title The title of the menu inventory.
     * @param size The size of the inventory (must be a multiple of 9).
     */
    public PaginatedMenu(String title, int size) {
        this.title = title;
        this.size = size;
        this.uuid = UUID.randomUUID();
        this.contentSlots = calculateContentSlots(size);
        this.inventory = Bukkit.createInventory(null, size, title);
    }

    /**
     * Creates a new paginated menu with the specified title and a default size of 54.
     *
     * @param title The title of the menu inventory.
     */
    public PaginatedMenu(String title) {
        this(title, 54);
    }

    @Override
    public void open(Player player) {
        open(player, 1);
    }

    @Override
    public void open(Player player, int page) {
        if (page < 1 || page > getTotalPages()) {
            page = 1;
        }
        this.currentPage = page;
        updateInventory();
        player.openInventory(inventory);
        viewers.add(player);
        MenuManager.getInstance().registerMenu(player, this);
    }

    @Override
    public void close(Player player) {
        if (viewers.contains(player)) {
            player.closeInventory();
            viewers.remove(player);
            MenuManager.getInstance().unregisterMenu(player);
        }
    }

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public int getTotalPages() {
        if (items.isEmpty()) return 1;
        return (int) Math.ceil((double) items.size() / itemsPerPage);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        // In Bukkit, the title of an inventory cannot be changed after it has been created.
        throw new UnsupportedOperationException("Cannot change title after menu creation");
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public List<ItemStack> getPageItems() {
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, items.size());
        return items.subList(startIndex, endIndex);
    }

    @Override
    public List<ItemStack> getAllItems() {
        return new ArrayList<>(items);
    }

    @Override
    public void setItems(List<ItemStack> items) {
        this.items = new ArrayList<>(items);
        updateInventory();
    }

    @Override
    public void addItem(ItemStack item) {
        items.add(item);
        updateInventory();
    }

    @Override
    public void removeItem(ItemStack item) {
        items.remove(item);
        updateInventory();
    }

    @Override
    public void clearItems() {
        items.clear();
        updateInventory();
    }

    @Override
    public Map<Integer, ItemStack> getNavigationItems() {
        return new HashMap<>(navigationItems);
    }

    @Override
    public void setNavigationItem(int slot, ItemStack item) {
        navigationItems.put(slot, item);
        updateInventory();
    }

    @Override
    public Consumer<InventoryClickEvent> getAction(int slot) {
        return actions.get(slot);
    }

    @Override
    public void setAction(int slot, Consumer<InventoryClickEvent> action) {
        actions.put(slot, action);
    }

    @Override
    public void setAction(ItemStack item, Consumer<InventoryClickEvent> action) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack != null && stack.equals(item)) {
                actions.put(i, action);
                break;
            }
        }
    }

    @Override
    public void refresh() {
        updateInventory();
    }

    @Override
    public void update() {
        updateInventory();
        viewers.forEach(player -> {
            if (player.getOpenInventory().getTopInventory().equals(inventory)) {
                player.updateInventory();
            }
        });
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
        return itemsPerPage;
    }

    @Override
    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
        this.contentSlots = calculateContentSlots(size);
        updateInventory();
    }

    @Override
    public int[] getContentSlots() {
        return contentSlots.clone();
    }

    @Override
    public void setContentSlots(int[] slots) {
        this.contentSlots = slots.clone();
        this.itemsPerPage = slots.length;
        updateInventory();
    }

    @Override
    public int[] getNavigationSlots() {
        return navigationSlots.clone();
    }

    @Override
    public void setNavigationSlots(int[] slots) {
        this.navigationSlots = slots.clone();
        updateInventory();
    }

    @Override
    public void nextPage() {
        if (currentPage < getTotalPages()) {
            goToPage(currentPage + 1);
        }
    }

    @Override
    public void previousPage() {
        if (currentPage > 1) {
            goToPage(currentPage - 1);
        }
    }

    @Override
    public boolean goToPage(int page) {
        if (page < 1 || page > getTotalPages()) {
            return false;
        }
        this.currentPage = page;
        updateInventory();
        return true;
    }

    @Override
    public void register() {
        MenuManager.getInstance().registerMenu(this);
    }

    @Override
    public void unregister() {
        MenuManager.getInstance().unregisterMenu(this);
        new ArrayList<>(viewers).forEach(this::close);
    }

    /**
     * Internal handler for all click events within this menu.
     * It determines whether the click was on a content item, navigation item,
     * or a custom action slot and calls the appropriate handler.
     *
     * @param event The {@link InventoryClickEvent}.
     * @param player The player who clicked.
     */
    public void handleClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        // First, check for a specific action on the slot
        Consumer<InventoryClickEvent> action = actions.get(slot);
        if (action != null) {
            action.accept(event);
            return;
        }

        // Then, check if it's a navigation or content slot
        if (Arrays.stream(navigationSlots).anyMatch(s -> s == slot)) {
            handleNavigationClick(slot, player);
        } else if (Arrays.stream(contentSlots).anyMatch(s -> s == slot)) {
            handleContentClick(slot, player, event);
        }
    }

    /**
     * Handles clicks on navigation slots. This can be overridden by subclasses
     * for custom navigation behavior.
     *
     * @param slot The slot that was clicked.
     * @param player The player who clicked.
     */
    protected void handleNavigationClick(int slot, Player player) {
        // This is a basic implementation. For more complex menus,
        // it's better to set actions directly using setAction().
        if (slot == navigationSlots[0] && currentPage > 1) {
            previousPage();
        } else if (slot == navigationSlots[navigationSlots.length - 1] && currentPage < getTotalPages()) {
            nextPage();
        } else if (navigationSlots.length > 1 && slot == navigationSlots[navigationSlots.length - 2]) {
            close(player);
        }
    }

    /**
     * Handles clicks on content slots. This can be overridden for custom behavior.
     * By default, it checks if there's a specific action for the clicked item.
     *
     * @param slot The slot that was clicked.
     * @param player The player who clicked.
     * @param event The {@link InventoryClickEvent}.
     */
    protected void handleContentClick(int slot, Player player, InventoryClickEvent event) {
        // The primary click handling is now done via the actions map in handleClick.
        // This method can be used for broader content-wide logic if needed.
    }

    /**
     * Clears and redraws the entire inventory with the current page's items and navigation.
     */
    protected void updateInventory() {
        inventory.clear();

        // Add content items for the current page
        List<ItemStack> pageItems = getPageItems();
        for (int i = 0; i < Math.min(pageItems.size(), contentSlots.length); i++) {
            inventory.setItem(contentSlots[i], pageItems.get(i));
        }

        // Add custom navigation items
        navigationItems.forEach(inventory::setItem);

        // Add default navigation if no custom items are set
        addDefaultNavigation();

        // Force update for all viewers
        viewers.forEach(player -> {
            if (player.getOpenInventory().getTopInventory().equals(inventory)) {
                player.updateInventory();
            }
        });
    }

    /**
     * Adds the default navigation controls (previous, next, page info, close)
     * if no custom navigation items have been set via {@link #setNavigationItem(int, ItemStack)}.
     */
    protected void addDefaultNavigation() {
        if (navigationItems.isEmpty() && navigationSlots.length >= 3) {
            // Previous page button
            if (currentPage > 1) {
                inventory.setItem(navigationSlots[0], MenuItem.previousPage().build());
                setAction(navigationSlots[0], event -> previousPage());
            }

            // Page info
            inventory.setItem(navigationSlots[4], MenuItem.pageInfo(currentPage, getTotalPages()).build());

            // Next page button
            if (currentPage < getTotalPages()) {
                inventory.setItem(navigationSlots[navigationSlots.length - 1], MenuItem.nextPage().build());
                setAction(navigationSlots[navigationSlots.length - 1], event -> nextPage());
            }

            // Close button
            inventory.setItem(navigationSlots[navigationSlots.length - 2], MenuItem.closeButton().build());
            setAction(navigationSlots[navigationSlots.length - 2], event -> close((Player) event.getWhoClicked()));
        }
    }

    /**
     * A utility method to create a simple ItemStack for navigation.
     *
     * @param material The material of the item.
     * @param name The display name of the item.
     * @param lore The lore lines for the item.
     * @return The created {@link ItemStack}.
     */
    protected ItemStack createNavigationItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Calculates the default content slots based on the inventory size,
     * typically reserving the last row for navigation.
     *
     * @param size The total size of the inventory.
     * @return An array of slot indices for content.
     */
    protected int[] calculateContentSlots(int size) {
        int rows = size / 9;
        int contentRows = rows > 1 ? rows - 1 : 1; // Reserve last row for navigation if possible
        int[] slots = new int[contentRows * 9];

        for (int i = 0; i < slots.length; i++) {
            slots[i] = i;
        }
        return slots;
    }

    /**
     * Gets the unique identifier for this menu instance.
     *
     * @return The UUID of the menu.
     */
    public UUID getUniqueId() {
        return uuid;
    }

    /**
     * Gets a set of all players currently viewing this menu.
     *
     * @return A new {@link Set} containing the viewers.
     */
    public Set<Player> getViewers() {
        return new HashSet<>(viewers);
    }
}