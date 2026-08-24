package com.example.piggod;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
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

public class PigGodPlugin extends JavaPlugin implements CommandExecutor {

    // 记录每个玩家当前的猪神
    private final Map<UUID, Pig> activePigGods = new HashMap<>();

    @Override
    public void onEnable() {
        getCommand("piggod").setExecutor(this);
        getLogger().info("PigGod 插件已启用！猪神永不落幕！");
    }

    @Override
    public void onDisable() {
        // 清理所有猪神
        for (Pig pig : activePigGods.values()) {
            if (pig != null && !pig.isDead()) {
                pig.remove();
            }
        }
        activePigGods.clear();
        getLogger().info("PigGod 插件已关闭。");
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
        if (oldPig != null && !oldPig.isDead()) {
            oldPig.remove();
            player.sendMessage(Component.text("旧的猪神已被请走……", NamedTextColor.GRAY));
        }

        // 召唤新猪神
        Location loc = player.getLocation().add(player.getLocation().getDirection().multiply(2)).add(0, 0.5, 0);
        Pig pig = (Pig) player.getWorld().spawnEntity(loc, EntityType.PIG);

        // 设置名字 & 属性
        pig.customName(Component.text("猪神", NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.text(" (Pig God)", NamedTextColor.YELLOW)));
        pig.setCustomNameVisible(true);
        pig.setGlowing(true);
        pig.setInvulnerable(true);
        pig.setSilent(true); // 静音，配合后续消息更有梗
        pig.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);
        pig.setHealth(100.0);
        pig.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1, false, false));
        pig.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));

        activePigGods.put(player.getUniqueId(), pig);

        // 播放音效和粒子
        player.getWorld().playSound(loc, Sound.ENTITY_PIG_AMBIENT, 1.5f, 0.6f); // 低沉水牛音感
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 50, 0.5, 0.8, 0.5, 0.1);
        player.getWorld().spawnParticle(Particle.END_ROD, loc, 30, 0.3, 0.6, 0.3, 0.05);

        // 发送梗消息（分步延迟，更像“来电”）
        sendMemeMessages(player, pig);

        return true;
    }

    private void sendMemeMessages(Player player, Pig pig) {
        // 立即消息
        player.sendMessage(Component.text("══════════════════════════════════", NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.text("  猪神来电……", NamedTextColor.GOLD, TextDecoration.BOLD));
        player.sendMessage(Component.text("══════════════════════════════════", NamedTextColor.DARK_GRAY));

        // 延迟消息序列（模拟抽象对话）
        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                if (pig.isDead() || !pig.isValid()) {
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
                        // 给猪神一个缓慢漂浮效果更有“神”感
                        pig.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 40, 0, false, false));
                    }
                    default -> cancel();
                }
                step++;
            }
        }.runTaskTimer(this, 20L, 35L); // 1秒后开始，每1.75秒一条
    }
}