# PigGod

Minecraft 插件 · 指令 `/piggod`（别名 `/猪神`、`/pgod`）  
参考 B 站 **Pig God（猪神）** 梗。

## 版本支持说明（重要）

| 范围 | 支持程度 |
|------|----------|
| **Paper 1.16.5 ~ 1.21.11 / 26.x** | **推荐，功能完整** |
| Paper / Spigot 1.13 ~ 1.16.4 | 基本可用，部分效果可能缺失 |
| **1.12 及更早** | **不保证**（API 断层太大，可能无法正常加载或运行） |

> **为什么不能完美支持 1.12 → 26.2？**  
> - 1.12 → 1.13：材料/方块系统重做  
> - 聊天系统：旧版 `ChatColor` vs 新版 Adventure  
> - Attribute / Particle / Sound / PotionEffect 多次改名  
> - Paper 26.x 需要 **Java 25**，而 1.12 时代是 Java 8  
> 单 JAR 完整覆盖需要多模块 + 大量反射/兼容库，维护成本极高。

本插件采用「尽力兼容」策略：优先保证现代 Paper 可用，旧版本尽量不崩溃。

## 功能

- 召唤发光、尽量无敌的「猪神」
- 梗风格来电消息
- 粒子 + 音效（有则播，无则跳过）

## 编译（Gradle）

需要 **JDK 17+**（跑 26.x 服务器请用 Java 25）。

```bash
./gradlew build
```

产物：`build/libs/PigGod-1.2.0.jar`

## 权限

- `piggod.use`（默认 true）

## 更新日志

### 1.2.0
- 扩大兼容层（反射 + legacy 消息）
- 明确支持范围：推荐 1.16.5 ~ 26.2，1.12 不保证
- 编译目标 Java 17

### 1.1.0
- 多版本 Attribute / 粒子兼容

### 1.0.0
- 初版

## 作者

[acc0-ga](https://github.com/acc0-ga)

Technoblade never dies 🐷