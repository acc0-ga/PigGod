package com.example.piggod;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * PigGod - 多版本支持插件
 * 支持 Paper 1.20.4 ~ 1.21.11
 * 仅使用稳定的 Bukkit / Paper API，无 NMS
 */
public class PigGodPlugin extends JavaPlugin implements CommandExecutor {

    // 记录每个玩家当前的猪神
    private final Map<UUID, Pig> activePigGods = new HashMap<>();

    // 当前服务器版本信息（仅用于日志）
    private String serverVersion = "unknown";

    @Override
    public void onEnable() {
        // 检测服务器版本
        detectServerVersion();

        if (getCommand("piggod") == null) {
            getLogger().severe("无法注册指令 /piggod，请检查 plugin.yml！");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getCommand("piggod").setExecutor(this);
        getLogger().info("PigGod 插件已启用！猪神永不落幕！");
        getLogger().info("当前服务器版本: " + serverVersion + " | 支持范围: Paper 1.20.4 ~ 1.21.11");
    }

    @Override
    public void onDisable() {
        // 清理所有猪神
        for (Pig pig : activePigGods.values()) {
            if (pig != null && pig.isValid() && !pig.isDead()) {
                pig.remove();
            }
        }
        activePigGods.clear();
        getLogger().info("PigGod 插件已关闭。");
    }

    /**
     * 简单检测服务器版本（仅用于日志提示）
     */
    private void detectServerVersion() {
        try {
            serverVersion = Bukkit.getMinecraftVersion();
        } catch (NoSuchMethodError | Exception e) {
            // 旧版本可能没有 getMinecraftVersion()
            try {
                serverVersion = Bukkit.getVersion();
            } catch (Exception ex) {
                serverVersion = "unknown";
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("只有玩家才能使用此指令！");
            return true;
        }

        if (!player.hasPermission("piggod.use")) {
            player.sendMessage(Component.text("你没有权限召唤猪神！", NamedTextColor.RED));
            return true;
        }

        // 如果已经有猪神，先移除旧的
        Pig oldPig = activePigGods.get(player.getUniqueId());
        if (oldPig != null && oldPig.isValid() && !oldPig.isDead()) {
            oldPig.remove();
            player.sendMessage(Component.text("旧的猪神已被请走……", NamedTextColor.GRAY));
        }

        try {
            // 召唤新猪神
            Location loc = player.getLocation()
                    .add(player.getLocation().getDirection().multiply(2))
                    .add(0, 0.5, 0);

            Pig pig = (Pig) player.getWorld().spawnEntity(loc, EntityType.PIG);

            // 设置名字（使用 Adventure API，Paper 全版本支持）
            pig.customName(Component.text("猪神", NamedTextColor.GOLD, TextDecoration.BOLD)
                    .append(Component.text(" (Pig God)", NamedTextColor.YELLOW)));
            pig.setCustomNameVisible(true);
            pig.setGlowing(true);
            pig.setInvulnerable(true);
            pig.setSilent(true);

            // 安全设置最大生命值（兼容不同版本 Attribute 命名）
            setMaxHealth(pig, 100.0);

            // 添加效果（PotionEffectType 在 1.20.5+ 有变化，但这些基础效果稳定）
            addSafeEffect(pig, "REGENERATION", Integer.MAX_VALUE, 1);
            addSafeEffect(pig, "FIRE_RESISTANCE", Integer.MAX_VALUE, 0);

            activePigGods.put(player.getUniqueId(), pig);

            // 播放音效和粒子（使用稳定的枚举）
            playSafeSound(player, loc);
            spawnSafeParticles(player, loc);

            // 发送梗消息
            sendMemeMessages(player, pig);

        } catch (Exception e) {
            getLogger().log(Level.WARNING, "召唤猪神时发生错误: " + e.getMessage(), e);
            player.sendMessage(Component.text("召唤猪神失败，请查看服务器控制台。", NamedTextColor.RED));
        }

        return true;
    }

    /**
     * 兼容不同版本的 Attribute 名称设置最大生命值
     */
    private void setMaxHealth(Pig pig, double health) {
        AttributeInstance attr = null;

        // 优先尝试现代名称（部分新版本）
        try {
            attr = pig.getAttribute(Attribute.valueOf("MAX_HEALTH"));
        } catch (IllegalArgumentException ignored) {
        }

        // 回退到经典名称
        if (attr == null) {
            try {
                attr = pig.getAttribute(Attribute.valueOf("GENERIC_MAX_HEALTH"));
            } catch (IllegalArgumentException ignored) {
            }
        }

        // 最后尝试直接使用枚举（编译期可见的）
        if (attr == null) {
            try {
                attr = pig.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            } catch (Exception ignored) {
            }
        }

        if (attr != null) {
            attr.setBaseValue(health);
            pig.setHealth(Math.min(health, attr.getValue()));
        } else {
            // 最保底方案
            pig.setHealth(Math.min(health, pig.getMaxHealth()));
        }
    }

    /**
     * 安全添加药水效果（防止某些版本枚举缺失）
     */
    private void addSafeEffect(Pig pig, String effectName, int duration, int amplifier) {
        try {
            PotionEffectType type = PotionEffectType.getByName(effectName);
            if (type != null) {
                pig.addPotionEffect(new PotionEffect(type, duration, amplifier, false, false));
            }
        } catch (Exception e) {
            getLogger().fine("无法添加效果 " + effectName + ": " + e.getMessage());
        }
    }

    /**
     * 安全播放声音
     */
    private void playSafeSound(Player player, Location loc) {
        try {
            player.getWorld().playSound(loc, Sound.ENTITY_PIG_AMBIENT, 1.5f, 0.6f);
        } catch (Exception e) {
            // 忽略音效失败
        }
    }

    /**
     * 安全生成粒子
     */
    private void spawnSafeParticles(Player player, Location loc) {
        try {
            // TOTEM_OF_UNDYING 在 1.20+ 稳定存在
            player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 50, 0.5, 0.8, 0.5, 0.1);
        } catch (Exception ignored) {
        }

        try {
            player.getWorld().spawnParticle(Particle.END_ROD, loc, 30, 0.3, 0.6, 0.3, 0.05);
        } catch (Exception ignored) {
        }
    }

    private void sendMemeMessages(Player player, Pig pig) {
        player.sendMessage(Component.text("══════════════════════════════════", NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.text("  猪神来电……", NamedTextColor.GOLD, TextDecoration.BOLD));
        player.sendMessage(Component.text("══════════════════════════════════", NamedTextColor.DARK_GRAY));

        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                if (!pig.isValid() || pig.isDead()) {
                    cancel();
                    return;
                }

                switch (step) {
                    case 0 -> player.sendMessage(Component.text("猪神：", NamedTextColor.GOLD)
                            .append(Component.text(" 你好，人类。", NamedTextColor.WHITE)));
                    case 1 -> player.sendMessage(Component.text("猪神：", NamedTextColor.GOLD)
                            .append(Component.text(" 还我十万美刀。", NamedTextColor.RED, TextDecoration.BOLD)));
                    case 2 -> player.sendMessage(Component.text("猪神：", NamedTextColor.GOLD)
                            .append(Component.text(" Dream die die die……", NamedTextColor.DARK_RED)));
                    case 3 -> player.sendMessage(Component.text("猪神：", NamedTextColor.GOLD)
                            .append(Component.text(" No way.", NamedTextColor.GRAY)));
                    case 4 -> {
                        player.sendMessage(Component.text("══════════════════════════════════", NamedTextColor.DARK_GRAY));
                        player.sendMessage(Component.text("  猪神已降临你身边。", NamedTextColor.YELLOW));
                        player.sendMessage(Component.text("  （Technoblade never dies）", NamedTextColor.AQUA));
                        player.sendMessage(Component.text("══════════════════════════════════", NamedTextColor.DARK_GRAY));

                        // 尝试添加漂浮效果
                        addSafeEffect(pig, "LEVITATION", 40, 0);
                    }
                    default -> cancel();
                }
                step++;
            }
        }.runTaskTimer(this, 20L, 35L);
    }
}