# PigGod

指令：`/piggod`（别名：`/猪神`、`/pgod`）  
B 站 **Pig God（猪神）** 梗插件。

## 硬兼容目标：1.12 ~ 26.x

| 版本 | 预期 |
|------|------|
| **1.12 ~ 1.12.2** | 可加载（尽量回退），召唤猪 + 文字消息；粒子/音效/发光尽量回退 |
| **1.13 ~ 1.16.4** | 基本完整 |
| **1.16.5 ~ 1.21.11 / 26.x** | 完整体验 |

> 说明：1.12 没有 `TOTEM` 粒子、没有完整 Adventure、部分方法名不同，插件会自动降级，而不是直接崩溃。

## 编译

```bash
./gradlew build
```

产物：`build/libs/PigGod-1.3.0.jar`

## 自动构建 & 发布

仓库已配置 GitHub Actions：

| 触发条件 | 行为 |
|----------|------|
| `push` / `PR` 到 `main` | 自动编译，上传 JAR 作为 Artifact |
| 推送 `v*` 标签（如 `v1.3.0`） | 自动编译 + **创建 GitHub Release** 并附上 JAR |
| 手动 `workflow_dispatch` | 同上编译 |

### 发布新版本步骤

1. 修改 `build.gradle.kts` 中的 `version`
2. 提交并推送到 `main`
3. 打标签并推送：

```bash
git tag v1.3.0
git push origin v1.3.0
```

几分钟后即可在 [Releases](https://github.com/acc0-ga/PigGod/releases) 下载对应 JAR。

## 权限

- `piggod.use`（默认 true）

## 版本

### 1.3.0
- 硬兼容 1.12：尽量回退 + 反射
- 去掉对 Adventure 的硬依赖

### 1.2.0
- 扩大兼容说明与反射层

### 1.0.0 ~ 1.1.0
- 初版与多版本 Attribute 支持

## 作者

[acc0-ga](https://github.com/acc0-ga)

Technoblade never dies 🐷
