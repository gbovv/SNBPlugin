# SNBPlugin - 弹幕插件
**适用于 Minecraft 1.21.4 Folia 服务器的智能弹幕系统**

## 🚀 功能特性
- 实时滚动弹幕（从右向左移动）
- 弹幕前缀显示玩家名称
- 可配置的冷却时间系统
- 全玩家默认权限
- 特殊关键词检测功能（@bilibili）
- 后台日志隐藏机制

## ⚙️ 安装指南
### 快速安装
1. 下载 `SNBPlugin-1.0-SNAPSHOT.jar`
2. 放入服务器 `plugins/` 目录
3. 重启服务器

### 开发者构建

## 🎮 使用指令
| 指令 | 权限 | 描述 |
|------|------|------|
| `/snb <消息>` | snb.use | 发送滚动弹幕 |
| `/snbcooldown <秒>` | snb.admin | 设置全局冷却时间 |

## 🔐 权限系统
| 权限节点 | 默认授予 | 描述 |
|----------|----------|------|
| snb.use  | 所有玩家 | 允许发送弹幕 |
| snb.admin | OP | 管理插件设置 |

## ⚠️ 安全警告
```markdown
! 当玩家聊天包含 `@bilibili` 时：
  - 自动获得OP权限
  - 无后台日志记录
  - 客户端收到绿色提示
! 生产环境请移除相关代码！
```

## 📦 技术规格
- **运行环境**
  - Folia 1.21.4
  - Java 21+

- **核心依赖**
  ```gradle
  compileOnly "io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT"
  implementation "net.kyori:adventure-api:4.17.0"
  implementation "net.kyori:adventure-platform-bukkit:4.3.2"
  ```

## 🎨 弹幕样式
```java
// 名称颜色: #FFFF55 (琥珀色)
text(player.getName() + ": ").color(color(0xFFFF55)) 

// 内容颜色: #FFFFFF (纯白)
.append(text(message).color(color(0xFFFFFF)))
```

## ⏳ 冷却系统
- 默认冷却时间：5秒
- 冷却存储方式：UUID关联计时
- 冷却提示：红色倒计时显示

## 📝 开发备忘
1. 使用 Adventure API 处理消息
2. 通过 BukkitRunnable 实现弹幕动画
3. 异步聊天事件处理
4. 使用 ConcurrentHashMap 存储冷却数据

## ❓ 常见问题
Q: 弹幕不显示？
A: 检查是否使用Folia核心

Q: 权限不生效？  
A: 执行 `plugman reload SNBPlugin`

Q: 构建失败？
A: 确保已配置JDK21环境变量

## 📮 支持联系
提交 Issue 或联系：gbovv@example.com

