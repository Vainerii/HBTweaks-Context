package vai.hbtweaks.context.client.contextmenu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.yaml.snakeyaml.Yaml;
import vai.hbtweaks.context.HBTweaksContext;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class CustomContextMenuLoader {
    private CustomContextMenuLoader() {}

    public static ContextMenu load(Path path, Player player) {
        ContextMenu cm = new ContextMenu(0, 0, player);
        return load(cm, path);
    }

    public static ContextMenu load(Path path, int rootX, int rootY, Player player) {
        ContextMenu cm = new ContextMenu(rootX, rootY, player);
        return load(cm, path);
    }

    public static ContextMenu load(ContextMenu cm, Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(is);

            List<?> menuList = (List<?>) root.get("menu");
            CustomContextMenuLoader.parseItems(cm, (List<Map<String, Object>>) menuList);
            return cm;
        } catch (Exception e) {
            HBTweaksContext.LOGGER.warn("Context menu is missing or invalid");
            return null;
        }
    }

    public static Map<String, Object> readYaml(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            Yaml yaml = new Yaml();
            return yaml.load(is);
        } catch (Exception e) {
            return null;
        }
    }

    public static ContextMenu load(Map<String, Object> root, Player player) {
        ContextMenu cm = new ContextMenu(0, 0, player);
        CustomContextMenuLoader.parseItems(cm, (List<Map<String, Object>>) root.get("menu"));
        return cm;
    }

    private static void parseItems(ContextMenu menu, List<Map<String, Object>> items) {
        for (Map<String, Object> rawMap : items) {
            CustomContextMenuLoader.parseEntry(menu, rawMap);
        }
    }

    private static void parseEntry(ContextMenu menu, Map<String, Object> entry) {

        if (entry.containsKey("info")) {
            String text = (String) entry.get("info");
            menu.addInfoItem(Component.literal(text));
            return;
        }

        Component label = Component.literal((String) entry.get("label"));

        if (entry.containsKey("submenu")) {
            Object sub = entry.get("submenu");
            ContextMenu submenu = new ContextMenu(0, 0, menu.getPlayer());
            CustomContextMenuLoader.parseItems(submenu, (List<Map<String, Object>>) sub);
            menu.addSubmenuItem(label, submenu);
        } else if (entry.containsKey("command")) {
            String command = (String) entry.get("command");
            menu.addCommandItem(label, command);
        }
    }

}
