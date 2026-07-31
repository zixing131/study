# 思答帝 · 1Panel 守护进程部署说明

端口：**8050**  
包内容：薄 Jar（业务代码 + 前端）+ `lib/` 依赖目录 + SQLite 数据目录

## 1. 上传解压

在服务器上准备目录，例如：

```bash
mkdir -p /opt/kids-learn
# 将本地 dist/kids-learn-8050 整个目录上传到服务器，例如：
# scp -r dist/kids-learn-8050 user@服务器:/opt/kids-learn/
cd /opt/kids-learn/kids-learn-8050
```

目录结构：

```
kids-learn-8050/
├── kids-learn.jar          # 核心（业务 + 前端静态资源，约 2MB）
├── lib/                    # 第三方依赖（约 37MB，很少变）
├── start.sh / stop.sh
├── 部署说明-1Panel.md
└── data/                   # 运行时数据（SQLite + 发音缓存）
    ├── kidslearn.db
    └── audio/
```

### 增量更新（推荐）

日常改功能/前端时，**只需覆盖** `kids-learn.jar`（约 2MB），不必重传整个 `lib/`：

```bash
scp dist/kids-learn-8050/kids-learn.jar user@服务器:/opt/kids-learn/kids-learn-8050/
# 然后在 1Panel 重启守护进程，或：
# ./stop.sh && ./start.sh
```

仅当升级 Spring Boot / 增删依赖时，才需要同步整个 `lib/` 目录。

## 2. 环境要求

- **JDK 21+**（推荐 Temurin / OpenJDK 21）
- 确认：

```bash
java -version
```

若没有 Java，在 1Panel「应用商店 / 运行环境」安装 JDK 21，或：

```bash
# 示例（Debian/Ubuntu）
apt update && apt install -y openjdk-21-jre-headless
```

## 3. 1Panel 守护进程配置

打开：1Panel → **主机** → **进程守护** → **创建守护进程**

| 配置项 | 建议值 |
|--------|--------|
| 名称 | `kids-learn` |
| 启动目录 | `/opt/kids-learn/kids-learn-8050` |
| 启动命令 | `java -Xms256m -Xmx512m -Dserver.port=8050 -jar kids-learn.jar` |
| 运行用户 | `root` 或有目录写权限的用户 |
| 自动重启 | 开启 |

说明：

- **启动目录必须是 jar 与 lib 所在目录**（Manifest 里 `Class-Path` 指向相对路径 `lib/*.jar`）
- 这样 `./data` 会写在这里（数据库、发音缓存）
- 不要把 jar 单独挪走而丢掉旁边的 `lib/`

保存后点 **启动**，看日志里出现：

```text
Tomcat started on port 8050
```

## 4. 放行端口 / 反代（二选一）

### 方式 A：直接访问端口

1Panel → 安全 / 防火墙 → 放行 **8050**  
访问：`http://服务器IP:8050`

### 方式 B：反代（推荐）

1Panel → 网站 → 创建网站（反向代理）

- 域名：你的域名
- 代理地址：`http://127.0.0.1:8050`

然后用域名访问即可。

## 5. 验证

```bash
curl -I http://127.0.0.1:8050/
curl http://127.0.0.1:8050/api/characters | head
```

浏览器打开首页，管理端：

- 地址：`http://IP:8050/admin/login`
- 账号：`admin`
- 密码：`zixing131`

> 若密码仍是旧的：检查启动目录下是否有外部 `application.yml` / `application.properties` 覆盖了 jar 内配置；或启动命令加 `-Dadmin.password=zixing131`。部署后请强刷浏览器（Ctrl/Cmd+Shift+R）。

## 6. 修改管理密码（可选）

在启动目录创建 `application.yml`（或启动命令加参数）：

```yaml
admin:
  username: admin
  password: 你的新密码
```

或守护进程启动命令改为：

```bash
java -Xms256m -Xmx512m -Dserver.port=8050 -Dadmin.password=你的新密码 -jar kids-learn.jar
```

当前默认密码为 `zixing131`。

## 7. 常用运维

```bash
# 看日志（守护进程面板里也能看）
tail -f /opt/kids-learn/kids-learn-8050/app.log

# 手动启停（若没用 1Panel 守护，可用脚本）
./start.sh
./stop.sh
```

数据备份：直接备份整个 `data/` 目录（含 `kidslearn.db` 与 `audio/`）。

## 8. 注意

- 首次启动会预缓存发音，可能持续几分钟，属正常
- 服务器需能访问外网（首次拉发音；之后走本地缓存）：优先 `speech.platform.bing.com`（Edge TTS），回退 `fanyi.baidu.com` / `translate.google.com` / `dict.youdao.com`
- Java 版本必须 ≥ 21
- `kids-learn.jar` 与 `lib/` 必须同目录；启动命令仍用 `-jar kids-learn.jar`（Class-Path 会自动加载依赖）
