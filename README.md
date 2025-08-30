# Menu Library

A simple menu utility library for Spigot.

## Installation

This project is hosted on GitHub Packages. To use it, you need to add the repository to your `pom.xml` or `build.gradle` file.

### Maven

Add the following repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/CatMC-Network/menu</url>
    </repository>
</repositories>
```

Then, add the dependency:

```xml
<dependencies>
    <dependency>
        <groupId>moe.vwinter.utils</groupId>
        <artifactId>menu</artifactId>
        <version>0.2</version>
    </dependency>
</dependencies>
```

### Gradle

Add the following to your `build.gradle` file:

**Groovy DSL:**
```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/CatMC-Network/menu")
    }
}

dependencies {
    implementation 'moe.vwinter.utils:menu:0.2'
}
```

**Kotlin DSL:**
```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/CatMC-Network/menu")
    }
}

dependencies {
    implementation("moe.vwinter.utils:menu:0.2")
}
```

## Requirements

This library is built against Spigot 1.8.8. You must have this version or a compatible version in your project.

## Usage

Here are a few examples of how to use the menu library.

### Simple Paginated Menu

This example creates a simple menu with a list of items that spans multiple pages.

```java
import moe.vwinter.utils.menu.MenuBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MenuExamples {

    public void openSimpleMenu(Player player) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            items.add(new ItemStack(Material.DIAMOND, i));
        }

        MenuBuilder.create("My Awesome Menu", 54)
            .items(items)
            .onOpen(p -> p.sendMessage("Welcome to the menu!"))
            .onClose(p -> p.sendMessage("You closed the menu."))
            .buildAndOpen(player);
    }
}
```

### Confirmation Menu

This example shows how to create a confirmation dialog with confirm and cancel actions.

```java
import moe.vwinter.utils.menu.MenuBuilder;
import org.bukkit.entity.Player;

public class MenuExamples {

    public void openConfirmationMenu(Player player) {
        MenuBuilder.confirmation(
            "Are you sure?",
            p -> {
                p.sendMessage("You confirmed!");
                // Add your confirmation logic here
            },
            p -> {
                p.sendMessage("You cancelled.");
                // Add your cancellation logic here
            }
        ).buildAndOpen(player);
    }
}
```

### Sign Input Menu

This example demonstrates how to get text input from a player using a sign.

```java
import moe.vwinter.utils.menu.SignInputMenu;
import org.bukkit.entity.Player;

public class MenuExamples {

    public void openSignInputMenu(Player player) {
        SignInputMenu.create(
            player,
            new String[]{"", "^^^^^^^^^^^^^^^", "Enter your", "text here"},
            lines -> {
                String combined = String.join(" ", lines);
                player.sendMessage("You entered: " + combined);
            }
        ).open(player);
    }
}
```
### Advanced Paginated Menu with Actions

This example demonstrates a paginated menu where each item has a specific click action.

```java
import moe.vwinter.utils.menu.MenuBuilder;
import moe.vwinter.utils.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MenuExamples {

    public void openAdvancedMenu(Player player) {
        MenuBuilder builder = MenuBuilder.create("Advanced Menu", 36);

        for (int i = 1; i <= 20; i++) {
            int finalI = i;
            builder.addItem(
                new MenuItem(Material.EMERALD, finalI)
                    .name("§aItem #" + finalI)
                    .lore("§7Click me!")
                    .build(),
                event -> {
                    Player p = (Player) event.getWhoClicked();
                    p.sendMessage("You clicked item #" + finalI);
                    p.closeInventory();
                }
            );
        }

        builder.buildAndOpen(player);
    }
}
```

### Anvil Input Menu

This example shows how to get text input from a player using an anvil interface.

```java
import moe.vwinter.utils.menu.AnvilInputMenu;
import org.bukkit.entity.Player;

public class MenuExamples {

    public void openAnvilInputMenu(Player player) {
        AnvilInputMenu.create(
            player,
            "Enter your name",
            name -> {
                player.sendMessage("Your name is: " + name);
            },
            p -> {
                p.sendMessage("You cancelled the input.");
            }
        ).open(player);
    }
}
```
## License

This project is licensed under the ISC License. See the [LICENSE](LICENSE) file for details.