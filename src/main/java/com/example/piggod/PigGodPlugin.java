package com.example.piggod;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * PigGod - 尽力多版本支持
 *
 * 实际可靠范围：Paper / Spigot 约 1.16.5 ~ 1.21.11 / 26.x
 * 1.12 ~ 1.15：可能能加载，但名字、粒子、属性等会大量降级或失败
 * 完整 1.12 → 26.2 单 JAR 完美支持不现实（API 断层太大）
 */
public class PigGodPlugin extends JavaPlugin implements CommandExecutor {

    private final Map<UUID, Pig> activePigGods = new HashMap<>();
    private String serverVersion = "unknown";
    private boolean adventureAvailable = false;

    @Override
    public void onEnable() {
        detectServerVersion();
        adventureAvailable = checkAdventure();

        if (getCommand("piggod") == null) {
            getLogger().severe("无法注册指令 /piggod，请检查 plugin.yml！");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getCommand("piggod").setExecutor(this);
        getLogger().info("PigGod 已启用！猪神永不落幕！");
        getLogger().info("服务器: " + serverVersion + " | Adventure: " + adventureAvailable);
        getLogger().info("推荐支持: Paper 1.16.5 ~ 26.2 | 1.12~1.15 仅尽力兼容");
    }

    @Override
    public void onDisable() {
        for (Pig pig : activePigGods.values()) {
            safeRemove(pig);
        }
        activePigGods.clear();
        getLogger().info("PigGod 已关闭。");
    }

    private void detectServerVersion() {
        try {
            Method m = Bukkit.class.getMethod("getMinecraftVersion");
            serverVersion = String.valueOf(m.invoke(null));
        } catch (Throwable t) {
            try {
                serverVersion = Bukkit.getVersion();
            } catch (Throwable t2) {
                serverVersion = "unknown";
            }
        }
    }

    private boolean checkAdventure() {
        try {
            Class.forName("net.kyori.adventure.text.Component");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家才能使用此指令！");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("piggod.use")) {
            sendMsg(player, ChatColor.RED + "你没有权限召唤猪神！");
            return true;
        }

        Pig oldPig = activePigGods.get(player.getUniqueId());
        if (oldPig != null && isValidEntity(oldPig)) {
            safeRemove(oldPig);
            sendMsg(player, ChatColor.GRAY + "旧的猪神已被请走……");
        }

        try {
            Location loc = player.getLocation().clone();
            try {
                loc.add(player.getLocation().getDirection().multiply(2)).add(0, 0.5, 0);
            } catch (Throwable ignored) {
                loc.add(1.5, 0.5, 0);
            }

            Pig pig = (Pig) player.getWorld().spawnEntity(loc, EntityType.PIG);

            setPigName(pig, "猪神 (Pig God)");
            try { pig.setCustomNameVisible(true); } catch (Throwable ignored) {}
            try { pig.setGlowing(true); } catch (Throwable ignored) {}
            try { pig.setInvulnerable(true); } catch (Throwable ignored) {}
            try { pig.setSilent(true); } catch (Throwable ignored) {}

            setMaxHealth(pig, 100.0);
            addSafeEffect(pig, "REGENERATION", Integer.MAX_VALUE, 1);
            addSafeEffect(pig, "FIRE_RESISTANCE", Integer.MAX_VALUE, 0);

            activePigGods.put(player.getUniqueId(), pig);

            playSafeSound(player, loc);
            spawnSafeParticles(player, loc);
            sendMemeMessages(player, pig);

        } catch (Throwable e) {
            getLogger().log(Level.WARNING, "召唤猪神失败: " + e.getMessage(), e);
            sendMsg(player, ChatColor.RED + "召唤猪神失败，请查看控制台。");
        }

        return true;
    }

    private void setPigName(Pig pig, String name) {
        // 优先 Adventure（Paper 新版本）
        if (adventureAvailable) {
            try {
                Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component");
                Class<?> namedTextColor = Class.forName("net.kyori.adventure.text.format.NamedTextColor");
                Class<?> textDecoration = Class.forName("net.kyori.adventure.text.format.TextDecoration");

                Object gold = namedTextColor.getField("GOLD").get(null);
                Object yellow = namedTextColor.getField("YELLOW").get(null);
                Object bold = textDecoration.getField("BOLD").get(null);

                Method textMethod = componentClass.getMethod("text", String.class, Class.forName("net.kyori.adventure.text.format.TextColor"));
                // 简化：直接用 legacy 回退更稳
            } catch (Throwable ignored) {
            }
        }

        // 全版本通用：legacy 名字
        try {
            pig.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + "猪神" + ChatColor.YELLOW + " (Pig God)");
        } catch (Throwable t) {
            try {
                pig.setCustomName("猪神 (Pig God)");
            } catch (Throwable ignored) {
            }
        }

        // Paper Adventure customName(Component) 额外尝试
        if (adventureAvailable) {
            try {
                Method customNameMethod = pig.getClass().getMethod("customName", Class.forName("net.kyori.adventure.text.Component"));
                Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component");
                Method text = componentClass.getMethod("text", String.class);
                Object comp = text.invoke(null, "猪神 (Pig God)");
                customNameMethod.invoke(pig, comp);
            } catch (Throwable ignored) {
            }
        }
    }

    private void setMaxHealth(Pig pig, double health) {
        AttributeInstance attr = null;
        String[] names = {"MAX_HEALTH", "GENERIC_MAX_HEALTH"};

        for (String n : names) {
            try {
                Attribute a = Attribute.valueOf(n);
                attr = pig.getAttribute(a);
                if (attr != null) break;
            } catch (Throwable ignored) {
            }
        }

        if (attr == null) {
            try {
                attr = pig.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            } catch (Throwable ignored) {
            }
        }

        try {
            if (attr != null) {
                attr.setBaseValue(health);
                pig.setHealth(Math.min(health, attr.getValue()));
            } else {
                pig.setMaxHealth(health);
                pig.setHealth(health);
            }
        } catch (Throwable t) {
            try {
                pig.setHealth(Math.min(20.0, pig.getMaxHealth()));
            } catch (Throwable ignored) {
            }
        }
    }

    private void addSafeEffect(Pig pig, String effectName, int duration, int amplifier) {
        try {
            PotionEffectType type = PotionEffectType.getByName(effectName);
            if (type == null) {
                // 1.20.5+ 部分用 registry，旧方法可能 null
                try {
                    Method getKey = PotionEffectType.class.getMethod("getByKey", org.bukkit.NamespacedKey.class);
                    // 忽略复杂路径
                } catch (Throwable ignored) {
                }
            }
            if (type != null) {
                pig.addPotionEffect(new PotionEffect(type, duration, amplifier, false, false));
            }
        } catch (Throwable e) {
            getLogger().fine("效果 " + effectName + " 不可用: " + e.getMessage());
        }
    }

    private void playSafeSound(Player player, Location loc) {
        String[] sounds = {"ENTITY_PIG_AMBIENT", "PIG_IDLE", "ENTITY_PIG_IDLE"};
        for (String s : sounds) {
            try {
                Sound sound = Sound.valueOf(s);
                player.getWorld().playSound(loc, sound, 1.5f, 0.6f);
                return;
            } catch (Throwable ignored) {
            }
        }
    }

    private void spawnSafeParticles(Player player, Location loc) {
        String[] particles = {
                "TOTEM_OF_UNDYING", "TOTEM", "END_ROD", "FIREWORKS_SPARK", "VILLAGER_HAPPY", "HEART"
        };
        for (String p : particles) {
            try {
                Particle particle = Particle.valueOf(p);
                player.getWorld().spawnParticle(particle, loc, 30, 0.5, 0.8, 0.5, 0.05);
            } catch (Throwable ignored) {
            }
        }
    }

    private void sendMemeMessages(final Player player, final Pig pig) {
        sendMsg(player, ChatColor.DARK_GRAY + "══════════════════════════════════");
        sendMsg(player, ChatColor.GOLD + "" + ChatColor.BOLD + "  猪神来电……");
        sendMsg(player, ChatColor.DARK_GRAY + "══════════════════════════════════");

        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                if (!isValidEntity(pig)) {
                    cancel();
                    return;
                }

                switch (step) {
                    case 0:
                        sendMsg(player, ChatColor.GOLD + "猪神：" + ChatColor.WHITE + " 你好，人类。");
                        break;
                    case 1:
                        sendMsg(player, ChatColor.GOLD + "猪神：" + ChatColor.RED + ChatColor.BOLD + " 还我十万美刀。");
                        break;
                    case 2:
                        sendMsg(player, ChatColor.GOLD + "猪神：" + ChatColor.DARK_RED + " Dream die die die……");
                        break;
                    case 3:
                        sendMsg(player, ChatColor.GOLD + "猪神：" + ChatColor.GRAY + " No way.");
                        break;
                    case 4:
                        sendMsg(player, ChatColor.DARK_GRAY + "══════════════════════════════════");
                        sendMsg(player, ChatColor.YELLOW + "  猪神已降临你身边。");
                        sendMsg(player, ChatColor.AQUA + "  （Technoblade never dies）");
                        sendMsg(player, ChatColor.DARK_GRAY + "══════════════════════════════════");
                        addSafeEffect(pig, "LEVITATION", 40, 0);
                        break;
                    default:
                        cancel();
                        break;
                }
                step++;
            }
        }.runTaskTimer(this, 20L, 35L);
    }

    private void sendMsg(Player player, String msg) {
        try {
            player.sendMessage(msg);
        } catch (Throwable ignored) {
        }
    }

    private boolean isValidEntity(Pig pig) {
        try {
            return pig != null && pig.isValid() && !pig.isDead();
        } catch (Throwable t) {
            try {
                return pig != null && !pig.isDead();
            } catch (Throwable t2) {
                return false;
            }
        }
    }

    private void safeRemove(Pig pig) {
        try {
            if (pig != null) pig.remove();
        } catch (Throwable ignored) {
        }
    }
}