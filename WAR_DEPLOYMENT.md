# WAR/JAR デプロイメントガイド

## 概要

このアプリケーションは、Spring BootとNext.jsを単一のWAR/JARファイルにパッケージ化してデプロイできます。
Next.jsはStandaloneモードでビルドされ、Spring Boot起動時に自動的にNode.jsプロセスとして起動されます。

## アーキテクチャ

```
Spring Boot Application (JAR/WAR)
├─ Spring Boot (Port 8080)
│  ├─ REST API (/api/**)
│  └─ Reverse Proxy (その他のリクエスト)
│
└─ Next.js Standalone (Port 3000)
   ├─ 組み込みNext.jsサーバー
   ├─ Node.js プロセスとして起動
   └─ classpath:/nextjs/** から抽出
```

## 前提条件

### ビルド時
- Java 17以上
- Maven 3.x
- Node.js 20.x（frontend-maven-pluginが自動ダウンロード）

### 実行時
- Java 17以上
- **Node.js 18.x以上**（サーバーにインストール必要）

## ビルド方法

### 完全ビルド（推奨）

```bash
cd spring-petclinic-to-quarkus
mvn clean package
```

このコマンドは以下を実行します：
1. frontend-maven-pluginがNode.jsとnpmをダウンロード
2. `petclinic-frontend`でnpm installを実行
3. `petclinic-frontend`でnpm run build（standalone）を実行
4. Next.js standalone buildをresourcesにコピー
5. Spring Bootアプリケーションをビルド
6. すべてを含むJARファイルを生成

### 手動ビルド（開発時）

Next.jsを手動でビルド済みの場合：

```bash
# 1. Next.jsをビルド
cd petclinic-frontend
npm run build

# 2. Spring Bootをビルド
cd ../spring-petclinic-to-quarkus
mvn clean package -Dfrontend-maven-plugin.skip=true
```

## 起動方法

### 開発環境

```bash
java -jar target/spring-petclinic-4.0.0-SNAPSHOT.jar
```

または

```bash
mvn spring-boot:run
```

### 本番環境

```bash
java -jar spring-petclinic-4.0.0-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --nextjs.enabled=true
```

### Node.jsがない環境

サーバーにNode.jsがインストールされていない場合、Next.jsは自動起動されません。
この場合、以下の2つの選択肢があります：

**オプション1: Node.jsをインストール**
```bash
# Ubuntu/Debian
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt-get install -y nodejs

# CentOS/RHEL
curl -fsSL https://rpm.nodesource.com/setup_20.x | sudo bash -
sudo yum install -y nodejs
```

**オプション2: Next.jsを無効化**
```bash
java -jar spring-petclinic-4.0.0-SNAPSHOT.jar \
  --nextjs.enabled=false
```

この場合、フロントエンドは使用できなくなりますが、REST APIは正常に動作します。

## 設定

### application.properties

```properties
# Next.js組み込みサーバー設定
nextjs.enabled=true          # Next.jsサーバーを自動起動するか
nextjs.port=3000             # Next.jsサーバーのポート
nextjs.url=http://localhost:3000  # Next.jsサーバーのURL
```

### 環境変数で上書き

```bash
export NEXTJS_ENABLED=true
export NEXTJS_PORT=3001
export NEXTJS_URL=http://localhost:3001

java -jar spring-petclinic-4.0.0-SNAPSHOT.jar
```

## ビルド成果物

### ディレクトリ構造

```
target/
├─ spring-petclinic-4.0.0-SNAPSHOT.jar
│  └─ BOOT-INF/
│     └─ classes/
│        └─ nextjs/               ← Next.js standalone
│           ├─ .next/
│           │  ├─ server/
│           │  └─ static/
│           ├─ node_modules/      ← 必要な依存関係のみ
│           ├─ public/
│           ├─ server.js          ← Next.js起動スクリプト
│           └─ package.json
```

### ファイルサイズ

- Next.js standalone: 約50-100MB
- Spring Boot JAR: 約60MB
- **合計: 約110-160MB**

## 起動シーケンス

1. Spring Bootアプリケーション起動
2. `NextJsServerStarter`が実行される
   - classpath:/nextjs/** を一時ディレクトリに抽出
   - Node.jsの存在確認
   - `node server.js`でNext.jsサーバーを起動
3. `NextJsProxyController`がリクエストをプロキシ
   - `/api/**` → Spring Boot REST Controllers
   - その他 → Next.js (Port 3000)
4. ブラウザで http://localhost:8080 にアクセス

## トラブルシューティング

### Node.jsが見つからない

**エラー:**
```
Node.js is not available. Next.js server will not start.
```

**解決策:**
```bash
# Node.jsをインストール
node --version  # v18以上であることを確認
```

### Next.jsプロセスが起動しない

**ログを確認:**
```bash
java -jar spring-petclinic-4.0.0-SNAPSHOT.jar --debug
```

**原因:**
- server.jsが見つからない → ビルドを確認
- ポート3000が使用中 → `nextjs.port`を変更
- Node.jsバージョンが古い → v18以上に更新

### ポート競合

**エラー:**
```
Error: listen EADDRINUSE: address already in use :::3000
```

**解決策:**
```bash
# 別のポートを使用
java -jar spring-petclinic-4.0.0-SNAPSHOT.jar --nextjs.port=3001 --nextjs.url=http://localhost:3001
```

### メモリ不足

Next.jsプロセスはデフォルトで約512MBのメモリを使用します。

**解決策:**
```bash
# JVMヒープサイズを増やす
java -Xmx2g -jar spring-petclinic-4.0.0-SNAPSHOT.jar
```

## パフォーマンス最適化

### 1. Next.js Image Optimization

既に`unoptimized: true`を設定しているため、Next.jsの画像最適化は無効化されています。

### 2. Next.js Cache

Next.jsのビルド時キャッシュは`.next/cache`に保存されます。
standaloneビルドには含まれません。

### 3. Spring Boot Compression

```properties
# application.properties
server.compression.enabled=true
server.compression.mime-types=text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json
server.compression.min-response-size=1024
```

## Docker対応

### Dockerfile例

```dockerfile
FROM eclipse-temurin:17-jre

# Node.jsをインストール
RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    rm -rf /var/lib/apt/lists/*

# アプリケーションをコピー
COPY target/spring-petclinic-4.0.0-SNAPSHOT.jar app.jar

# ポート公開
EXPOSE 8080

# 起動
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### ビルドとデプロイ

```bash
# ビルド
mvn clean package
docker build -t petclinic:latest .

# 実行
docker run -p 8080:8080 petclinic:latest
```

## 本番環境チェックリスト

- [ ] Node.js v18以上がインストールされている
- [ ] ポート8080, 3000が利用可能
- [ ] 十分なメモリ（最低2GB推奨）
- [ ] 環境変数が適切に設定されている
- [ ] データベース接続設定が正しい
- [ ] ログ出力先が設定されている
- [ ] ヘルスチェックエンドポイントが動作する

```bash
# ヘルスチェック
curl http://localhost:8080/actuator/health
```

## まとめ

この構成により：

✅ **単一のJARファイル**でフロントエンドとバックエンドをデプロイ可能
✅ **Node.js組み込み**で自動起動
✅ **開発と本番で同じ構成**
✅ **シンプルなデプロイ**（`java -jar`のみ）

ただし、本番環境にはNode.jsのインストールが必要です。
