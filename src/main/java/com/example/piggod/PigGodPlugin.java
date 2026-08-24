package com.example.piggod;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
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
 * PigGod - 硬兼容 1.12 ~ 现代版本
 *
 * 约束：
 * - 源码与字节码保持 Java 8（1.12 服务器常用 Java 8）
 * - 不使用 switch 表达式、var、pattern matching、text block 等
 * - 名称/粒子/声音/属性全部反射或多重回退
 * - 消息只用 legacy ChatColor，避免强依赖 Adventure
 */
public class PigGodPlugin extends JavaPlugin implements CommandExecutor {

    private final Map<UUID, Entity> activePigGods = new HashMap<UUID, Entity>();
    private String serverVersion = "unknown";

    @Override
    public void onEnable() {
        detectServerVersion();

        if (getCommand("piggod") == null) {
            getLogger().severe("Cannot register /piggod - check plugin.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getCommand("piggod").setExecutor(this);
        getLogger().info("PigGod enabled! Technoblade never dies!");
        getLogger().info("Server: " + serverVersion + " | Hard-compat mode (1.12 ~ 26.x best-effort)");
    }

    @Override
    public void onDisable() {
        for (Entity e : activePigGods.values()) {
            safeRemove(e);
        }
        activePigGods.clear();
        getLogger().info("PigGod disabled.");
    }

    private void detectServerVersion() {
        try {
            Method m = Bukkit.class.getMethod("getMinecraftVersion");
            Object v = m.invoke(null);
            serverVersion = String.valueOf(v);
        } catch (Throwable t) {
            try {
                serverVersion = Bukkit.getBukkitVersion();
            } catch (Throwable t2) {
                try {
                    serverVersion = Bukkit.getVersion();
                } catch (Throwable t3) {
                    serverVersion = "unknown";
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("piggod.use")) {
            player.sendMessage(ChatColor.RED + "你没有权限召唤猪神！");
            return true;
        }

        Entity old = activePigGods.get(player.getUniqueId());
        if (old != null && isValid(old)) {
            safeRemove(old);
            player.sendMessage(ChatColor.GRAY + "旧的猪神已被请走……");
        }

        try {
            Location base = player.getLocation();
            Location loc = base.clone();
            try {
                loc.add(base.getDirection().multiply(2.0)).add(0.0, 0.5, 0.0);
            } catch (Throwable t) {
                loc.add(1.5, 0.5, 0.0);
            }

            Entity spawned = player.getWorld().spawnEntity(loc, EntityType.PIG);
            if (!(spawned instanceof Pig)) {
                player.sendMessage(ChatColor.RED + "召唤失败：无法生成猪。");
                safeRemove(spawned);
                return true;
            }

            Pig pig = (Pig) spawned;

            setCustomNameLegacy(pig, ChatColor.GOLD + "" + ChatColor.BOLD + "猪神" + ChatColor.YELLOW + " (Pig God)");
            trySetBoolean(pig, "setCustomNameVisible", true);
            trySetBoolean(pig, "setGlowing", true);
            trySetBoolean(pig, "setInvulnerable", true);
            trySetBoolean(pig, "setSilent", true);

            setMaxHealthCompat(pig, 100.0);
            addEffectCompat(pig, "REGENERATION", Integer.MAX_VALUE, 1);
            addEffectCompat(pig, "FIRE_RESISTANCE", Integer.MAX_VALUE, 0);

            activePigGods.put(player.getUniqueId(), pig);

            playSoundCompat(player, loc);
            spawnParticlesCompat(player, loc);
            sendMemeMessages(player, pig);

        } catch (Throwable e) {
            getLogger().log(Level.WARNING, "Summon PigGod failed: " + e.getMessage(), e);
            player.sendMessage(ChatColor.RED + "召唤猪神失败，请查看控制台。");
        }

        return true;
    }

    private void setCustomNameLegacy(Entity entity, String name) {
        try {
            entity.setCustomName(name);
        } catch (Throwable ignored) {
        }
        // Paper Adventure customName(Component) 可选
        try {
            Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component");
            Method text = componentClass.getMethod("text", String.class);
            Object component = text.invoke(null, ChatColor.stripColor(name));
            Method customName = entity.getClass().getMethod("customName", componentClass);
            customName.invoke(entity, component);
        } catch (Throwable ignored) {
        }
    }

    private void trySetBoolean(Object target, String methodName, boolean value) {
        try {
            Method m = target.getClass().getMethod(methodName, boolean.class);
            m.invoke(target, Boolean.valueOf(value));
        } catch (Throwable ignored) {
            try {
                Method m = target.getClass().getMethod(methodName, Boolean.TYPE);
                m.invoke(target, Boolean.valueOf(value));
            } catch (Throwable ignored2) {
            }
        }
    }

    private void setMaxHealthCompat(LivingEntity entity, double health) {
        // 1. 现代 Attribute API
        try {
            Class<?> attrClass = Class.forName("org.bukkit.attribute.Attribute");
            Class<?> attrInstClass = Class.forName("org.bukkit.attribute.AttributeInstance");
            Method getAttribute = LivingEntity.class.getMethod("getAttribute", attrClass);

            Object attrEnum = null;
            String[] names = new String[] { "MAX_HEALTH", "GENERIC_MAX_HEALTH" };
            for (int i = 0; i < names.length; i++) {
                try {
                    attrEnum = Enum.valueOf((Class<Enum>) attrClass.asSubclass(Enum.class), names[i]);
                    break;
                } catch (Throwable ignored) {
                }
            }

            if (attrEnum != null) {
                Object inst = getAttribute.invoke(entity, attrEnum);
                if (inst != null) {
                    Method setBase = attrInstClass.getMethod("setBaseValue", double.class);
                    setBase.invoke(inst, Double.valueOf(health));
                    Method getValue = attrInstClass.getMethod("getValue");
                    double v = ((Number) getValue.invoke(inst)).doubleValue();
                    entity.setHealth(Math.min(health, v));
                    return;
                }
            }
        } catch (Throwable ignored) {
        }

        // 2. 旧版 setMaxHealth（1.12 仍可用）
        try {
            Method setMax = LivingEntity.class.getMethod("setMaxHealth", double.class);
            setMax.invoke(entity, Double.valueOf(health));
            entity.setHealth(health);
            return;
        } catch (Throwable ignored) {
        }

        try {
            entity.setHealth(Math.min(20.0, entity.getMaxHealth()));
        } catch (Throwable ignored) {
        }
    }

    private void addEffectCompat(LivingEntity entity, String effectName, int duration, int amplifier) {
        try {
            PotionEffectType type = PotionEffectType.getByName(effectName);
            if (type == null) {
                // 部分旧映射
                if ("REGENERATION".equals(effectName)) {
                    type = PotionEffectType.getByName("REGEN");
                }
            }
            if (type != null) {
                // 1.12 构造器: (type, duration, amplifier) 或带 ambient/particles
                try {
                    entity.addPotionEffect(new PotionEffect(type, duration, amplifier, false, false));
                } catch (Throwable t) {
                    try {
                        entity.addPotionEffect(new PotionEffect(type, duration, amplifier));
                    } catch (Throwable t2) {
                    }
                }
            }
        } catch (Throwable e) {
            getLogger().fine("Effect " + effectName + " unavailable: " + e.getMessage());
        }
    }

    private void playSoundCompat(Player player, Location loc) {
        String[] soundNames = new String[] {
                "ENTITY_PIG_AMBIENT",
                "ENTITY_PIG_IDLE",
                "PIG_IDLE",
                "PIG_SAY"
        };
        for (int i = 0; i < soundNames.length; i++) {
            try {
                Class<?> soundClass = Class.forName("org.bukkit.Sound");
                Object sound = Enum.valueOf((Class<Enum>) soundClass.asSubclass(Enum.class), soundNames[i]);
                Method play = org.bukkit.World.class.getMethod(
                        "playSound", Location.class, soundClass, float.class, float.class);
                play.invoke(player.getWorld(), loc, sound, Float.valueOf(1.5f), Float.valueOf(0.6f));
                return;
            } catch (Throwable ignored) {
            }
        }
        // 字符串音效（极旧或不支持枚举时）
        try {
            Method play = org.bukkit.World.class.getMethod(
                    "playSound", Location.class, String.class, float.class, float.class);
            play.invoke(player.getWorld(), loc, "entity.pig.ambient", Float.valueOf(1.5f), Float.valueOf(0.6f));
        } catch (Throwable ignored) {
            try {
                Method play = org.bukkit.World.class.getMethod(
                        "playSound", Location.class, String.class, float.class, float.class);
                play.invoke(player.getWorld(), loc, "mob.pig.say", Float.valueOf(1.5f), Float.valueOf(0.6f));
            } catch (Throwable ignored2) {
            }
        }
    }

    private void spawnParticlesCompat(Player player, Location loc) {
        // 从新到旧尝试
        String[] particleNames = new String[] {
                "TOTEM_OF_UNDYING",
                "TOTEM",
                "END_ROD",
                "FIREWORKS_SPARK",
                "VILLAGER_HAPPY",
                "HEART",
                "CLOUD",
                "SPELL_WITCH",
                "CRIT"
        };

        boolean any = false;
        for (int i = 0; i < particleNames.length; i++) {
            try {
                Class<?> particleClass = Class.forName("org.bukkit.Particle");
                Object particle = Enum.valueOf((Class<Enum>) particleClass.asSubclass(Enum.class), particleNames[i]);
                // spawnParticle(Particle, Location, int, double, double, double, double)
                Method spawn = org.bukkit.World.class.getMethod(
                        "spawnParticle",
                        particleClass,
                        Location.class,
                        int.class,
                        double.class,
                        double.class,
                        double.class,
                        double.class);
                spawn.invoke(player.getWorld(), particle, loc,
                        Integer.valueOf(30),
                        Double.valueOf(0.5),
                        Double.valueOf(0.8),
                        Double.valueOf(0.5),
                        Double.valueOf(0.05));
                any = true;
            } catch (Throwable ignored) {
            }
        }

        if (!any) {
            // 1.8~1.12 早期 Effect 枚举
            try {
                Class<?> effectClass = Class.forName("org.bukkit.Effect");
                Object effect = Enum.valueOf((Class<Enum>) effectClass.asSubclass(Enum.class), "HEART");
                Method playEffect = org.bukkit.World.class.getMethod(
                        "playEffect", Location.class, effectClass, int.class);
                playEffect.invoke(player.getWorld(), loc, effect, Integer.valueOf(0));
            } catch (Throwable ignored) {
            }
        }
    }

    private void sendMemeMessages(final Player player, final Entity pig) {
        player.sendMessage(ChatColor.DARK_GRAY + "==================================");
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "  猪神来电......");
        player.sendMessage(ChatColor.DARK_GRAY + "==================================");

        new BukkitRunnable() {
            private int step = 0;

            @Override
            public void run() {
                if (!isValid(pig)) {
                    cancel();
                    return;
                }

                if (step == 0) {
                    player.sendMessage(ChatColor.GOLD + "猪神：" + ChatColor.WHITE + " 你好，人类。");
                } else if (step == 1) {
                    player.sendMessage(ChatColor.GOLD + "猪神：" + ChatColor.RED + "" + ChatColor.BOLD + " 还我十万美刀。");
                } else if (step == 2) {
                    player.sendMessage(ChatColor.GOLD + "猪神：" + ChatColor.DARK_RED + " Dream die die die......");
                } else if (step == 3) {
                    player.sendMessage(ChatColor.GOLD + "猪神：" + ChatColor.GRAY + " No way.");
                } else if (step == 4) {
                    player.sendMessage(ChatColor.DARK_GRAY + "==================================");
                    player.sendMessage(ChatColor.YELLOW + "  猪神已降临你身边。");
                    player.sendMessage(ChatColor.AQUA + "  (Technoblade never dies)");
                    player.sendMessage(ChatColor.DARK_GRAY + "==================================");
                    if (pig instanceof LivingEntity) {
                        addEffectCompat((LivingEntity) pig, "LEVITATION", 40, 0);
                    }
                } else {
                    cancel();
                    return;
                }
                step++;
            }
        }.runTaskTimer(this, 20L, 35L);
    }

    private boolean isValid(Entity entity) {
        if (entity == null) {
            return false;
        }
        try {
            if (entity.isDead()) {
                return false;
            }
        } catch (Throwable ignored) {
        }
        try {
            Method isValid = Entity.class.getMethod("isValid");
            Object r = isValid.invoke(entity);
            if (r instanceof Boolean && !((Boolean) r).booleanValue()) {
                return false;
            }
        } catch (Throwable ignored) {
        }
        return true;
    }

    private void safeRemove(Entity entity) {
        if (entity == null) {
            return;
        }
        try {
            entity.remove();
        } catch (Throwable ignored) {
        }
    }
}