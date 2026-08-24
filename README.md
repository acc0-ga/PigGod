# PigGod

指令：`/piggod`（别名：`/猪神`、`/pgod`）  
B 站 **Pig God（猪神）** 梗插件。

## 硬兼容目标：1.12 ~ 26.x

| 版本 | 预期 |
|------|------|
| **1.12 ~ 1.12.2** | 可加载（Java 8 字节码），召唤猪 + 文字消息；粒子/音效/发光尽量回退 |
| **1.13 ~ 1.16.4** | 基本完整 |
| **1.16.5 ~ 1.21.11 / 26.x** | 完整体验 |

### 本版为 1.12 做的硬措施

- **字节码目标 Java 8**（1.12 服务器常见 Java 8，用 17 编译会无法加载）
- **无现代 Java 语法**（不用 var / switch 表达式 / pattern matching）
- **消息全部 legacy `ChatColor`**（不强制 Adventure）
- **粒子 / 声音 / 属性 / 药水** 多重枚举名 + 反射回退
- **plugin.yml 不写 api-version**，方便 1.12 识别

> 说明：1.12 没有 `TOTEM` 粒子、没有完整 Adventure、部分方法名不同，插件会自动降级，而不是直接崩溃。

## 编译

需要能编译 Java 8 的 JDK（11/17/21 均可，会输出 1.8 字节码）：

```bash
./gradlew build
```

产物：`build/libs/PigGod-1.3.0.jar`

## 权限

- `piggod.use`（默认 true）

## 版本

### 1.3.0
- 硬兼容 1.12：Java 8 字节码 + 全反射回退
- 去掉对 Adventure 的硬依赖

### 1.2.0
- 扩大兼容说明与反射层

### 1.0.0 ~ 1.1.0
- 初版与多版本 Attribute 支持

## 作者

[acc0-ga](https://github.com/acc0-ga)

Technoblade never dies 🐷