# PigGod

Minecraft **Paper 1.21.11** 插件  
指令：`/piggod`（别名：`/猪神`、`/pgod`）

参考 B 站 **Pig God（猪神）** 梗制作。

## 功能

- 在玩家身边召唤一只发光、无敌的「猪神」
- 播放梗风格来电消息（还我十万美刀 / Dream die die die 等）
- 粒子 + 音效效果

## 使用方法（Gradle）

1. 确保已安装 **JDK 21** 和 Gradle（或直接用项目自带的 Gradle Wrapper）
2. 编译插件：
   ```bash
   ./gradlew build
   ```
   Windows 下：
   ```bash
   gradlew.bat build
   ```
3. 编译后的 jar 文件位于：
   ```
   build/libs/PigGod-1.0.0.jar
   ```
4. 把 jar 放入服务器 `plugins` 文件夹
5. 重启服务器
6. 游戏内输入 `/piggod`

## 权限

- `piggod.use`（默认所有人可用）

## 作者

[acc0-ga](https://github.com/acc0-ga)

Technoblade never dies 🐷