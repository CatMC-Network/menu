package moe.vwinter.utils.menu;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * A builder for creating and customizing {@link ItemStack}s for use in menus.
 * This class provides a fluent interface for setting item properties like name,
 * lore, enchantments, and click actions.
 *
 * <p>Example usage:
 * <pre>{@code
 * ItemStack customItem = new MenuItem(Material.DIAMOND_SWORD)
 *     .name("§cLegendary Sword")
 *     .lore("A powerful weapon.", "Forged in dragon's fire.")
 *     .enchant(Enchantment.DAMAGE_ALL, 5)
 *     .glow()
 *     .action(event -> {
 *         Player player = (Player) event.getWhoClicked();
 *         player.sendMessage("You clicked the legendary sword!");
 *     })
 *     .build();
 * }</pre>
 */
public class MenuItem {

    private ItemStack item;
    private Consumer<InventoryClickEvent> clickAction;

    /**
     * Creates a new MenuItem with the specified material.
     *
     * @param material The material of the item.
     */
    public MenuItem(Material material) {
        this.item = new ItemStack(material);
    }

    /**
     * Creates a new MenuItem with the specified material and amount.
     *
     * @param material The material of the item.
     * @param amount The amount of the item.
     */
    public MenuItem(Material material, int amount) {
        this.item = new ItemStack(material, amount);
    }

    /**
     * Creates a new MenuItem with the specified material, amount, and durability.
     *
     * @param material The material of the item.
     * @param amount The amount of the item.
     * @param durability The durability (data value) of the item.
     */
    public MenuItem(Material material, int amount, short durability) {
        this.item = new ItemStack(material, amount, durability);
    }

    /**
     * Creates a new MenuItem from an existing ItemStack, creating a clone.
     *
     * @param item The ItemStack to base the MenuItem on.
     */
    public MenuItem(ItemStack item) {
        this.item = item.clone();
    }

    /**
     * Sets the display name of the item. Color codes are supported.
     *
     * @param name The display name to set.
     * @return This MenuItem instance for chaining.
     */
    public MenuItem name(String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Sets the lore of the item from a varargs array of strings.
     *
     * @param lore The lines of the lore.
     * @return This MenuItem instance for chaining.
     */
    public MenuItem lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    /**
     * Sets the lore of the item from a list of strings.
     *
     * @param lore The list of lore lines.
     * @return This MenuItem instance for chaining.
     */
    public MenuItem lore(List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Adds an enchantment to the item.
     *
     * @param enchantment The enchantment to add.
     * @param level The level of the enchantment.
     * @param ignoreLevelRestriction True to ignore vanilla level restrictions.
     * @return This MenuItem instance for chaining.
     */
    public MenuItem enchant(
        Enchantment enchantment,
        int level,
        boolean ignoreLevelRestriction
    ) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(enchantment, level, ignoreLevelRestriction);
            item.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Adds an enchantment to the item, respecting vanilla level restrictions.
     *
     * @param enchantment The enchantment to add.
     * @param level The level of the enchantment.
     * @return This MenuItem instance for chaining.
     */
    public MenuItem enchant(Enchantment enchantment, int level) {
        return enchant(enchantment, level, false);
    }

    /**
     * Removes an enchantment from the item.
     *
     * @param enchantment The enchantment to remove.
     * @return This MenuItem instance for chaining.
     */
    public MenuItem removeEnchant(Enchantment enchantment) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.removeEnchant(enchantment);
            item.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Sets the amount (stack size) of the item.
     *
     * @param amount The new amount.
     * @return This MenuItem instance for chaining.
     */
    public MenuItem amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    /**
     * Sets the durability (data value) of the item.
     *
     * @param durability The new durability.
     * @return This MenuItem instance for chaining.
     */
    public MenuItem durability(short durability) {
        item.setDurability(durability);
        return this;
    }

    /**
     * Makes the item glow by applying a hidden enchantment.
     *
     * @return This MenuItem instance for chaining.
     */
    public MenuItem glow() {
        return enchant(Enchantment.LUCK, 1, true);
    }

    /**
     * Removes the glow effect from the item.
     *
     * @return This MenuItem instance for chaining.
     */
    public MenuItem unglow() {
        return removeEnchant(Enchantment.LUCK);
    }

    /**
     * Sets the owner of a skull item. Only works if the item is a {@link Material#SKULL_ITEM}.
     *
     * @param owner The name of the player whose skin to display.
     * @return This MenuItem instance for chaining.
     */
    public MenuItem skullOwner(String owner) {
        if (item.getType() == Material.SKULL_ITEM) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta != null) {
                meta.setOwner(owner);
                item.setItemMeta(meta);
            }
        }
        return this;
    }

    /**
     * Sets the owner of a skull item using a player's UUID.
     *
     * @param uuid The UUID of the player whose skin to display.
     * @return This MenuItem instance for chaining.
     */
    public MenuItem skullOwner(UUID uuid) {
        if (item.getType() == Material.SKULL_ITEM) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta != null) {
                meta.setOwner(Bukkit.getOfflinePlayer(uuid).getName());
                item.setItemMeta(meta);
            }
        }
        return this;
    }

    /**
     * Sets the action to be executed when this item is clicked in a menu.
     *
     * @param action The {@link Consumer} to handle the {@link InventoryClickEvent}.
     * @return This MenuItem instance for chaining.
     */
    public MenuItem action(Consumer<InventoryClickEvent> action) {
        this.clickAction = action;
        return this;
    }

    /**
     * Gets the click action associated with this menu item.
     *
     * @return The click action handler, or null if none is set.
     */
    public Consumer<InventoryClickEvent> getClickAction() {
        return clickAction;
    }

    /**
     * Builds the final, customized {@link ItemStack}.
     *
     * @return A clone of the configured ItemStack.
     */
    public ItemStack build() {
        return item.clone();
    }

    /**
     * Creates a standard placeholder item, typically a gray stained glass pane with no name.
     * Useful for filling empty slots in a menu.
     *
     * @return A placeholder MenuItem.
     */
    public static MenuItem placeholder() {
        return new MenuItem(Material.STAINED_GLASS_PANE, 1, (short) 7)
            .name(" ")
            .lore(" ");
    }

    /**
     * A factory method for creating a generic navigation item.
     *
     * @param material The material of the navigation item.
     * @param name The name of the navigation item.
     * @param lore The lore of the navigation item.
     * @return A navigation MenuItem.
     */
    public static MenuItem navigation(
        Material material,
        String name,
        String... lore
    ) {
        return new MenuItem(material).name(name).lore(lore);
    }

    /**
     * Creates a standardized "Previous Page" button.
     *
     * @return A "Previous Page" MenuItem.
     */
    public static MenuItem previousPage() {
        return navigation(
            Material.ARROW,
            "§aPrevious Page",
            "§7Click to go to the previous page"
        );
    }

    /**
     * Creates a standardized "Next Page" button.
     *
     * @return A "Next Page" MenuItem.
     */
    public static MenuItem nextPage() {
        return navigation(
            Material.ARROW,
            "§aNext Page",
            "§7Click to go to the next page"
        );
    }

    /**
     * Creates an item to display page information (e.g., "Page 1/5").
     *
     * @param currentPage The current page number.
     * @param totalPages The total number of pages.
     * @return A page info MenuItem.
     */
    public static MenuItem pageInfo(int currentPage, int totalPages) {
        return navigation(
            Material.PAPER,
            "§ePage " + currentPage + "/" + totalPages,
            "§7Navigate through pages"
        );
    }

    /**
     * Creates a standardized "Close" button.
     *
     * @return A "Close" MenuItem.
     */
    public static MenuItem closeButton() {
        return navigation(
            Material.BARRIER,
            "§cClose",
            "§7Click to close the menu"
        );
    }

    /**
     * Creates a standardized "Back" button.
     *
     * @return A "Back" MenuItem.
     */
    public static MenuItem backButton() {
        return navigation(Material.ARROW, "§cBack", "§7Click to go back");
    }

    /**
     * Creates a standardized "Confirm" button.
     *
     * @return A "Confirm" MenuItem.
     */
    public static MenuItem confirmButton() {
        return navigation(Material.EMERALD, "§aConfirm", "§7Click to confirm");
    }

    /**
     * Creates a standardized "Cancel" button.
     *
     * @return A "Cancel" MenuItem.
     */
    public static MenuItem cancelButton() {
        return navigation(Material.REDSTONE, "§cCancel", "§7Click to cancel");
    }

    /**
     * Creates a default text input item (paper) for anvil menus.
     *
     * @param placeholder The placeholder text to display as the item's name.
     * @return A text input MenuItem.
     */
    public static MenuItem textInput(String placeholder) {
        return new MenuItem(Material.PAPER)
            .name(placeholder)
            .lore("§7Click to enter text");
    }

    /**
     * Creates a text input item with a custom material for anvil menus.
     *
     * @param material The material of the input item.
     * @param placeholder The placeholder text to display as the item's name.
     * @return A text input MenuItem.
     */
    public static MenuItem textInput(Material material, String placeholder) {
        return new MenuItem(material)
            .name(placeholder)
            .lore("§7Click to enter text");
    }

    /**
     * Creates a name tag input item for anvil menus.
     *
     * @return A name tag input MenuItem.
     */
    public static MenuItem nameTagInput() {
        return new MenuItem(Material.NAME_TAG)
            .name("Enter name...")
            .lore("§7Click to enter a name");
    }

    /**
     * Creates a book input item for anvil menus.
     *
     * @return A book input MenuItem.
     */
    public static MenuItem bookInput() {
        return new MenuItem(Material.BOOK)
            .name("Enter text...")
            .lore("§7Click to enter text");
    }

    /**
     * Creates a sign input item for anvil menus.
     *
     * @return A sign input MenuItem.
     */
    public static MenuItem signInput() {
        return new MenuItem(Material.SIGN)
            .name("Enter text...")
            .lore("§7Click to enter text");
    }
}