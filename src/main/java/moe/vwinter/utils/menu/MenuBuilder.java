package moe.vwinter.utils.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * A fluent builder for creating paginated menus with ease.
 * This builder simplifies the process of creating complex menus by providing
 * a chainable interface for setting properties and adding items.
 *
 * <p>Example usage:
 * <pre>{@code
 * MenuBuilder.create("My Awesome Menu", 54)
 *     .items(myItemList)
 *     .onOpen(player -> player.sendMessage("Welcome!"))
 *     .onClose(player -> player.sendMessage("Goodbye!"))
 *     .buildAndOpen(player);
 * }</pre>
 */
public class MenuBuilder {

    private String title = "Menu";
    private int size = 54;
    private List<ItemStack> items = new ArrayList<>();
    private List<Consumer<InventoryClickEvent>> itemActions = new ArrayList<>();
    private int itemsPerPage = 28;
    private int[] contentSlots;
    private int[] navigationSlots = {45, 46, 47, 48, 49, 50, 51, 52, 53};
    private boolean autoRegister = true;
    private boolean showDefaultNavigation = true;
    private Consumer<Player> onOpen = null;
    private Consumer<Player> onClose = null;

    /**
     * Creates a new MenuBuilder with default settings.
     */
    public MenuBuilder() {
        this.contentSlots = calculateContentSlots(size);
    }

    /**
     * Sets the title of the menu inventory.
     *
     * @param title The title to display.
     * @return This builder instance for chaining.
     */
    public MenuBuilder title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the size of the menu inventory. Must be a multiple of 9.
     *
     * @param size The inventory size (e.g., 9, 18, 27, 36, 45, 54).
     * @return This builder instance for chaining.
     * @throws IllegalArgumentException if the size is not a multiple of 9.
     */
    public MenuBuilder size(int size) {
        if (size % 9 != 0) {
            throw new IllegalArgumentException("Menu size must be a multiple of 9");
        }
        this.size = size;
        this.contentSlots = calculateContentSlots(size);
        return this;
    }

    /**
     * Sets the list of items to be displayed in the menu.
     * This will replace any existing items.
     *
     * @param items A list of {@link ItemStack}s to display.
     * @return This builder instance for chaining.
     */
    public MenuBuilder items(List<ItemStack> items) {
        this.items = new ArrayList<>(items);
        return this;
    }

    /**
     * Sets the items to be displayed in the menu from a varargs array.
     * This will replace any existing items.
     *
     * @param items An array of {@link ItemStack}s to display.
     * @return This builder instance for chaining.
     */
    public MenuBuilder items(ItemStack... items) {
        this.items = Arrays.asList(items);
        return this;
    }

    /**
     * Adds a single item to the menu.
     *
     * @param item The {@link ItemStack} to add.
     * @return This builder instance for chaining.
     */
    public MenuBuilder addItem(ItemStack item) {
        this.items.add(item);
        return this;
    }

    /**
     * Adds a single item with an associated click action.
     *
     * @param item The {@link ItemStack} to add.
     * @param action The {@link Consumer} to execute when the item is clicked.
     * @return This builder instance for chaining.
     */
    public MenuBuilder addItem(ItemStack item, Consumer<InventoryClickEvent> action) {
        this.items.add(item);
        this.itemActions.add(action);
        return this;
    }

    /**
     * Adds multiple items to the menu from a varargs array.
     *
     * @param items The {@link ItemStack}s to add.
     * @return This builder instance for chaining.
     */
    public MenuBuilder addItems(ItemStack... items) {
        this.items.addAll(Arrays.asList(items));
        return this;
    }

    /**
     * Sets the number of items to display per page.
     * This is automatically calculated if custom content slots are set.
     *
     * @param itemsPerPage The number of items per page.
     * @return This builder instance for chaining.
     */
    public MenuBuilder itemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
        return this;
    }

    /**
     * Sets the specific inventory slots to be used for content.
     * This overrides the default content slot calculation.
     * The number of items per page will be set to the number of slots provided.
     *
     * @param slots An array of slot indices for content.
     * @return This builder instance for chaining.
     */
    public MenuBuilder contentSlots(int... slots) {
        this.contentSlots = slots;
        this.itemsPerPage = slots.length;
        return this;
    }

    /**
     * Sets the specific inventory slots to be used for navigation controls.
     *
     * @param slots An array of slot indices for navigation.
     * @return This builder instance for chaining.
     */
    public MenuBuilder navigationSlots(int... slots) {
        this.navigationSlots = slots;
        return this;
    }

    /**
     * Sets whether the menu should be automatically registered with the {@link MenuManager}.
     * Defaults to true.
     *
     * @param autoRegister Set to false to handle registration manually.
     * @return This builder instance for chaining.
     */
    public MenuBuilder autoRegister(boolean autoRegister) {
        this.autoRegister = autoRegister;
        return this;
    }

    /**
     * Sets whether to show the default navigation controls (previous/next page, close).
     * Defaults to true.
     *
     * @param showDefaultNavigation Set to false to hide default controls.
     * @return This builder instance for chaining.
     */
    public MenuBuilder showDefaultNavigation(boolean showDefaultNavigation) {
        this.showDefaultNavigation = showDefaultNavigation;
        return this;
    }

    /**
     * Sets a callback to be executed when the menu is opened for a player.
     *
     * @param onOpen A {@link Consumer} that accepts the {@link Player} opening the menu.
     * @return This builder instance for chaining.
     */
    public MenuBuilder onOpen(Consumer<Player> onOpen) {
        this.onOpen = onOpen;
        return this;
    }

    /**
     * Sets a callback to be executed when the menu is closed for a player.
     *
     * @param onClose A {@link Consumer} that accepts the {@link Player} closing the menu.
     * @return This builder instance for chaining.
     */
    public MenuBuilder onClose(Consumer<Player> onClose) {
        this.onClose = onClose;
        return this;
    }

    /**
     * Builds the {@link PaginatedMenu} with the configured settings.
     *
     * @return The configured {@link PaginatedMenu} instance.
     */
    public PaginatedMenu build() {
        PaginatedMenu menu = new PaginatedMenu(title, size) {
            @Override
            public void open(Player player) {
                super.open(player);
                if (onOpen != null) {
                    onOpen.accept(player);
                }
            }

            @Override
            public void close(Player player) {
                super.close(player);
                if (onClose != null) {
                    onClose.accept(player);
                }
            }
        };

        menu.setItems(items);
        menu.setItemsPerPage(itemsPerPage);
        menu.setContentSlots(contentSlots);
        menu.setNavigationSlots(navigationSlots);

        // Set item actions if provided
        if (!itemActions.isEmpty()) {
            for (int i = 0; i < Math.min(itemActions.size(), items.size()); i++) {
                int finalI = i;
                menu.setAction(i, itemActions.get(finalI));
            }
        }

        // Configure default navigation
        if (showDefaultNavigation) {
            configureDefaultNavigation(menu);
        }

        if (autoRegister) {
            menu.register();
        }

        return menu;
    }

    /**
     * Builds the menu and immediately opens it for the specified player.
     *
     * @param player The player to open the menu for.
     * @return The built and opened {@link PaginatedMenu} instance.
     */
    public PaginatedMenu buildAndOpen(Player player) {
        PaginatedMenu menu = build();
        menu.open(player);
        return menu;
    }

    /**
     * Builds the menu and immediately opens it for the specified player at a specific page.
     *
     * @param player The player to open the menu for.
     * @param page The page number to open (1-based).
     * @return The built and opened {@link PaginatedMenu} instance.
     */
    public PaginatedMenu buildAndOpen(Player player, int page) {
        PaginatedMenu menu = build();
        menu.open(player, page);
        return menu;
    }

    /**
     * Configures the default navigation items (previous, next, close) for the menu.
     *
     * @param menu The menu to configure.
     */
    private void configureDefaultNavigation(PaginatedMenu menu) {
        // Previous page button
        menu.setNavigationItem(navigationSlots[0], MenuItem.previousPage().build());
        menu.setAction(navigationSlots[0], event -> menu.previousPage());

        // Next page button
        menu.setNavigationItem(navigationSlots[navigationSlots.length - 1], MenuItem.nextPage().build());
        menu.setAction(navigationSlots[navigationSlots.length - 1], event -> menu.nextPage());

        // Close button (second to last slot)
        if (navigationSlots.length >= 2) {
            int closeSlot = navigationSlots[navigationSlots.length - 2];
            menu.setNavigationItem(closeSlot, MenuItem.closeButton().build());
            menu.setAction(closeSlot, event -> menu.close((Player) event.getWhoClicked()));
        }
    }

    /**
     * Calculates the default content slots based on the inventory size,
     * reserving the last row for navigation.
     *
     * @param size The total size of the inventory.
     * @return An array of slot indices for content.
     */
    private int[] calculateContentSlots(int size) {
        int rows = size / 9;
        int contentRows = rows - 1; // Reserve last row for navigation
        int[] slots = new int[contentRows * 9];

        for (int row = 0; row < contentRows; row++) {
            for (int col = 0; col < 9; col++) {
                slots[row * 9 + col] = row * 9 + col;
            }
        }
        return slots;
    }

    /**
     * Static factory method to create a new {@link MenuBuilder} instance.
     *
     * @return A new, empty MenuBuilder.
     */
    public static MenuBuilder create() {
        return new MenuBuilder();
    }

    /**
     * Static factory method to create a new {@link MenuBuilder} with a specified title.
     *
     * @param title The title of the menu.
     * @return A new MenuBuilder with the given title.
     */
    public static MenuBuilder create(String title) {
        return new MenuBuilder().title(title);
    }

    /**
     * Static factory method to create a new {@link MenuBuilder} with a title and size.
     *
     * @param title The title of the menu.
     * @param size The size of the inventory (must be a multiple of 9).
     * @return A new MenuBuilder with the given title and size.
     */
    public static MenuBuilder create(String title, int size) {
        return new MenuBuilder().title(title).size(size);
    }

    /**
     * Creates a pre-configured builder for a simple item list menu.
     *
     * @param title The title of the menu.
     * @param items A list of items to display.
     * @return A configured MenuBuilder for an item list.
     */
    public static MenuBuilder itemList(String title, List<ItemStack> items) {
        return create(title).items(items).showDefaultNavigation(true);
    }

    /**
     * Creates a pre-configured builder for a simple item list menu.
     *
     * @param title The title of the menu.
     * @param items An array of items to display.
     * @return A configured MenuBuilder for an item list.
     */
    public static MenuBuilder itemList(String title, ItemStack... items) {
        return create(title).items(items).showDefaultNavigation(true);
    }

    /**
     * Creates a pre-configured builder for a settings menu.
     *
     * @param title The title of the settings menu.
     * @return A configured MenuBuilder for a settings menu.
     */
    public static MenuBuilder settings(String title) {
        return create(title).size(27).itemsPerPage(9).showDefaultNavigation(false);
    }

    /**
     * Creates a pre-configured builder for a confirmation dialog menu.
     * Includes "Confirm" and "Cancel" buttons.
     *
     * @param title The title of the confirmation dialog.
     * @param confirmAction The action to run when the confirm button is clicked.
     * @param cancelAction The action to run when the cancel button is clicked.
     * @return A configured MenuBuilder for a confirmation dialog.
     */
    public static MenuBuilder confirmation(String title, Consumer<Player> confirmAction, Consumer<Player> cancelAction) {
        return create(title).size(27).itemsPerPage(9).showDefaultNavigation(false)
            .addItem(MenuItem.confirmButton().build(), event -> {
                if (confirmAction != null) {
                    confirmAction.accept((Player) event.getWhoClicked());
                }
                ((Player) event.getWhoClicked()).closeInventory();
            })
            .addItem(MenuItem.cancelButton().build(), event -> {
                if (cancelAction != null) {
                    cancelAction.accept((Player) event.getWhoClicked());
                }
                ((Player) event.getWhoClicked()).closeInventory();
            });
    }
}