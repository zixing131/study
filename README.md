# 思答帝 · 学龄前趣味学习网站

面向学龄前儿童的趣味学习站：语文（识字 / 古诗）、数学（数数 / 加减）、英语（字母 / 单词），另附管理端题库配置。英语发音使用有道词典接口。

## 技术栈

- 后端：Spring Boot 4 + MyBatis + SQLite（MVC：controller / service / dao / xml / config）
- 数据库文件：`backend/data/kidslearn.db`
- 前端：Vue 3 + Vite + Vue Router + Pinia
- 朗读：有道真人发音，**服务端落盘缓存**（`backend/data/audio/`）；数字 1–100 启动预热，其它发音首次拉取后复用；失败回退浏览器 TTS
- 音效 / 动画：WebAudio + CSS 鲜花 / 哭脸反馈

## 快速启动

### 1. 启动后端

```bash
cd backend
./gradlew bootRun
```

后端默认：`http://localhost:8050`

管理端账号（可在 `backend/src/main/resources/application.yml` 修改）：

- 账号：`admin`
- 密码：`zixing131`

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认：`http://localhost:5173`（已代理 `/api` 到后端）

手机访问时，用电脑局域网 IP + 5173 端口即可（用户端按手机分辨率设计）。

## 功能说明

### 用户端（手机宽度）

| 模块 | 功能 |
|------|------|
| 识字乐园 | 整屏汉字 + 喇叭朗读 + 笔顺 / 组词 / 句子 + 下一题 |
| 古诗童谣 | 点句跟读；测试模式随机缺句，家长判断对错（鲜花 / 哭脸） |
| 快乐数数 | 1→100 领读；测试模式缺数字，家长判断 |
| 加减运算 | 10 以内加减，家长判断对错 |
| 字母宝宝 | A–Z 跟读与测试 |
| 单词卡片 | 生活小单词卡片 + 测试 |

### 管理端

路径：`/admin/login`

- 识字题库增删改
- 古诗题库增删改
- 英语字母 / 单词题库增删改

## 目录结构

```
study/
├── backend/                 # Spring Boot
│   └── src/main/java/com/study/kids/
│       ├── controller/
│       ├── service/
│       ├── dao/
│       ├── config/
│       └── entity/
│   └── src/main/resources/
│       ├── application.yml
│       ├── schema.sql
│       ├── data.sql
│       └── xml/             # MyBatis XML
└── frontend/                # Vue 用户端 + 管理端
```

## 生产打包

```bash
./scripts/build-release.sh
```

输出目录 `dist/kids-learn-8050/`：

| 产物 | 说明 |
|------|------|
| `kids-learn.jar` | 核心（业务代码 + 前端），约 2MB，日常更新只传这个 |
| `lib/` | 第三方依赖，约 37MB，依赖不变可跳过 |
| `start.sh` / `stop.sh` | 启停脚本 |
| `data/` | SQLite 与发音缓存 |

启动方式不变：`java -jar kids-learn.jar`（Manifest 的 Class-Path 会加载 `lib/`）。详见 `部署说明-1Panel.md`。
