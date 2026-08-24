# PigGod

Minecraft **Paper** 多版本插件  
指令：`/piggod`（别名：`/猪神`、`/pgod`）

参考 B 站 **Pig God（猪神）** 梗制作。

## 支持版本

| 服务器 | 支持范围 |
|--------|----------|
| **Paper** | **1.20.4 ~ 1.21.11** |
| Spigot / Purpur | 理论上 1.20.4+（未完整测试） |

> 使用纯 Bukkit / Paper API，无 NMS，兼容性较好。

## 功能

- 在玩家身边召唤一只发光、无敌的「猪神」
- 播放梗风格来电消息（还我十万美刀 / Dream die die die 等）
- 粒子 + 音效效果
- 多版本自动兼容 Attribute / 粒子 / 声音

## 使用方法（Gradle）

1. 确保已安装 **JDK 21**
2. 编译插件：
   ```bash
   ./gradlew build
   ```
   Windows：
   ```bash
   gradlew.bat build
   ```
3. 编译后的 jar 文件位于：
   ```
   build/libs/PigGod-1.1.0.jar
   ```
4. 把 jar 放入服务器 `plugins` 文件夹
5. 重启服务器
6. 游戏内输入 `/piggod`

## 权限

- `piggod.use`（默认所有人可用）

## 更新日志

### 1.1.0
- 添加多版本支持（Paper 1.20.4 ~ 1.21.11）
- 兼容不同版本的 Attribute 命名
- 增强异常处理与日志

### 1.0.0
- 初始版本

## 作者

[acc0-ga](https://github.com/acc0-ga)

Technoblade never dies 🐷