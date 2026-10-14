# GitHub DNS 污染与推送解决方案

> 记录在中国大陆网络环境下，解决 GitHub 访问失败、推送代码的完整步骤。
> 适用于 SSH/HTTPS 均提示 `Connection closed` 或 `Could not read from remote repository` 的场景。

---

## 问题现象

```bash
$ git push -u origin main
Connection closed by 198.18.0.67 port 22
fatal: Could not read from remote repository.

Please make sure you have the correct access rights
and the repository exists.
```

或 HTTPS 方式：

```bash
$ git push -u origin main
fatal: unable to access 'https://github.com/xxx/xxx.git/': 
LibreSSL SSL_connect: SSL_ERROR_SYSCALL in connection to github.com:443
```

**根本原因：** DNS 污染导致 `github.com` 解析到虚假 IP（如 `198.18.x.x`），连接被重置。

---

## 解决方案：手动绑定真实 IP

### 1. 获取 GitHub 真实 IP

使用公共 DNS（如阿里 `223.5.5.5`）查询真实解析结果：

```bash
# GitHub HTTPS 域名
dig @223.5.5.5 github.com +short
# 输出示例：20.205.243.166

# GitHub SSH 域名（443 端口）
dig @223.5.5.5 ssh.github.com +short
# 输出示例：20.205.243.160
```

> 注意：GitHub IP 可能随时间变化，以实际查询结果为准。

### 2. 绑定到本地 hosts

```bash
# 绑定 GitHub HTTPS
sudo sh -c 'echo "20.205.243.166 github.com" >> /etc/hosts'

# 绑定 GitHub SSH
sudo sh -c 'echo "20.205.243.160 ssh.github.com" >> /etc/hosts'

# 刷新 DNS 缓存
sudo dscacheutil -flushcache
```

### 3. 验证连通性

```bash
# 测试 HTTPS
curl -s -o /dev/null -w "%{http_code}" https://github.com
# 应输出：200

# 测试 SSH（会提示是否信任 host key）
ssh -T -p 443 git@ssh.github.com
# 应输出：Hi <username>! You've successfully authenticated...
```

---

## 配置 SSH 走 443 端口

由于 SSH 标准端口 22 通常被封锁，GitHub 支持通过 `ssh.github.com:443` 连接：

### 方法 A：SSH Config 配置（推荐）

创建或编辑 `~/.ssh/config`：

```ssh-config
Host github.com
    HostName ssh.github.com
    Port 443
    User git
    IdentityFile ~/.ssh/id_ed25519
```

然后直接使用标准 SSH URL：

```bash
git remote set-url origin git@github.com:username/repo.git
git push -u origin main
```

### 方法 B：Remote URL 直连

```bash
git remote set-url origin ssh://git@ssh.github.com:443/username/repo.git
git push -u origin main
```

---

## 完整推送流程（首次）

### 1. 生成本地 SSH Key（如已有可跳过）

```bash
ssh-keygen -t ed25519 -C "your_email@example.com"
# 一路回车，默认保存到 ~/.ssh/id_ed25519
```

### 2. 复制公钥并添加到 GitHub

```bash
cat ~/.ssh/id_ed25519.pub
# 复制输出内容
```

打开 https://github.com/settings/keys → **New SSH key** → 粘贴 → **Add SSH key**

### 3. 初始化本地仓库并推送

```bash
cd /path/to/your/project

# 初始化
git init
git add .
git commit -m "Initial commit"

# 设置远程仓库（使用 SSH）
git remote add origin git@github.com:username/repo.git

# 如远程已有内容，先拉取合并
git pull origin main --allow-unrelated-histories --no-rebase

# 解决冲突后提交合并
git commit -m "Merge remote"

# 推送
git push -u origin main
```

---

## 备用方案：HTTPS + Personal Access Token

如果 SSH 仍有问题，可使用 HTTPS + PAT（Personal Access Token）：

### 1. 生成 PAT

GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic) → **Generate new token**

勾选 `repo` 权限，生成后复制 token（只显示一次）。

### 2. 配置 Git 使用 PAT

```bash
# 修改 remote 为 HTTPS
git remote set-url origin https://github.com/username/repo.git

# 推送时输入用户名和 PAT（作为密码）
git push -u origin main
```

### 3. 缓存凭证（避免每次输入）

```bash
git config --global credential.helper osxkeychain
# 下次推送输入一次后自动缓存
```

---

## 常见问题排查

| 问题 | 排查命令 | 解决 |
|---|---|---|
| `Connection closed` | `curl -v https://github.com` | DNS 污染 → 绑定真实 IP |
| `Host key verification failed` | `ssh-keyscan github.com >> ~/.ssh/known_hosts` | 添加 host key |
| `Permission denied (publickey)` | `ssh -T git@github.com` | 检查 SSH key 是否添加到 GitHub |
| `fetch first` / `rejected` | `git pull origin main --allow-unrelated-histories` | 先合并远程内容 |
| SSL 证书错误 | `git config --global http.sslVerify false` | 临时关闭（不推荐长期使用） |

---

## 参考命令速查

```bash
# 查看当前 remote
git remote -v

# 切换 SSH / HTTPS
git remote set-url origin git@github.com:user/repo.git
git remote set-url origin https://github.com/user/repo.git

# 查看提交历史
git log --oneline --graph

# 强制推送（谨慎使用）
git push origin main --force-with-lease

# 查看当前分支跟踪状态
git status
```

---

> **提示：** 绑定 hosts 后如仍无法连接，可能是本地代理/VPN 与 hosts 冲突，尝试关闭代理后重试。
