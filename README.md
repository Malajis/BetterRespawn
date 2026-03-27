# BetterRespawn

一个 Minecraft Spigot 插件，提供死亡后原地复活、倒计时飞行、经验扣除等功能。

## 功能特性

- **原地复活**：死亡后在原地自动复活
- **倒计时系统**：倒计时期间显示 Title 提示
- **飞行能力**：倒计时期间可以自由飞行，移动到安全位置
- **无敌状态**：倒计时期间无敌
- **交互限制**：倒计时期间无法交互、破坏/放置方块、使用命令等
- **经验扣除**：每次复活扣除一定比例经验
- **自动重生**：死亡后自动重生，无需手动点击
- **虚空保护**：脚下是虚空时自动传送到重生点
- **管理员特权**：拥有 `betterrespawn.admin` 权限的玩家不受任何限制

## 安装方法

1. 将插件 `.jar` 文件放入服务器的 `plugins` 文件夹
2. 重启服务器或执行 `reload` 命令
3. 配置文件会自动生成在 `plugins/BetterRespawn/config.yml`

## 配置文件

```yaml
# 是否启用插件功能（原地重生+飞行+倒计时）
enable-feature: true

# 是否自动重生（死亡后自动复活）
auto-respawn: true

# 倒计时时间（秒）
respawn-time: 5

# 复活时扣除的经验百分比
experience-cost: 25
```

## 命令

| 命令 | 说明 | 权限 |
|------|------|------|
| `/betterrespawn reload` | 重载配置文件 | betterrespawn.reload |
| `/br reload` | 简写命令 | betterrespawn.reload |

## 权限节点

| 权限 | 说明 | 默认 |
|------|------|------|
| `betterrespawn.reload` | 允许重载配置 | OP |
| `betterrespawn.admin` | 管理员特权（不扣经验、无倒计时） | OP |

## 倒计时说明

- 死亡后自动重生在死亡位置
- 显示倒计时，期间可以飞行
- 倒计时结束后传送到当前位置下方的固体方块
- 如果脚下是虚空，则传送到世界重生点并发送警告

## 兼容性

- Spigot 1.13+
- Paper 1.13+

## 作者

Malajis