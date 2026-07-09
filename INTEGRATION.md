# Spring Boot + Next.js 統合ガイド

## 概要

このプロジェクトは、Spring Boot REST APIとNext.js SPAフロントエンドを統合しています。
Spring Bootアプリケーションがリバースプロキシとして動作し、すべてのリクエストを単一のポート（8080）で処理します。

## アーキテクチャ

```
Browser
   ↓
http://localhost:8080
   ↓
Spring Boot (Port 8080)
   ├─ /api/**        → Spring Boot REST Controllers
   └─ その他          → Next.js (Port 3000) へプロキシ
                        ├─ /
                        ├─ /owners/**
                        ├─ /vets-list/**
                        ├─ /oups/**
                        ├─ /_next/**
                        └─ /images/**
```

## 起動方法

### 1. Next.jsを起動

```bash
cd petclinic-frontend
npm install
npm run dev
```

Next.jsは http://localhost:3000 で起動します。

### 2. Spring Bootを起動

```bash
cd spring-petclinic-to-quarkus
mvn spring-boot:run
```

Spring Bootは http://localhost:8080 で起動します。

### 3. アクセス

ブラウザで http://localhost:8080 にアクセスすると、Next.jsフロントエンドが表示されます。

## リクエストのルーティング

| パス | 処理 |
|------|------|
| `/api/**` | Spring Boot REST Controllers（直接処理） |
| `/` | Next.js へプロキシ（Welcome Page） |
| `/owners/**` | Next.js へプロキシ（Owners関連ページ） |
| `/vets-list/**` | Next.js へプロキシ（Vets List Page） |
| `/oups/**` | Next.js へプロキシ（Error Page） |
| `/_next/**` | Next.js へプロキシ（Next.js静的リソース） |
| `/images/**` | Next.js へプロキシ（画像ファイル） |
| `/vets` | Spring Boot（Vets JSON/XML API） |

## 設定

### application.properties

```properties
# Next.js Frontend Proxy
nextjs.url=http://localhost:3000
```

本番環境では、Next.jsを別のポートや別のサーバーで実行する場合、この値を変更します。

## 開発時の注意点

### Hot Reload

- **Next.js**: `npm run dev`で起動しているため、ファイルを変更すると自動的にリロードされます
- **Spring Boot**: Spring Boot DevToolsを使用している場合、Javaファイルの変更時に自動リロードされます

### ポート

- Next.js: 3000（開発サーバー）
- Spring Boot: 8080（統合エンドポイント）

ユーザーは **http://localhost:8080** のみにアクセスします。
http://localhost:3000 は内部的に使用されますが、直接アクセスする必要はありません。

### CORS

Next.jsとSpring Bootは同じオリジン（localhost:8080）からアクセスされるため、CORS設定は不要になりました。

## 削除されたコンポーネント

統合に伴い、以下のThymeleaf関連ファイルを削除しました：

### Thymeleafテンプレート
- `src/main/resources/templates/**` - すべてのThymeleafテンプレート

### Thymeleafコントローラー
- `org.springframework.samples.petclinic.owner.OwnerController`
- `org.springframework.samples.petclinic.owner.PetController`
- `org.springframework.samples.petclinic.owner.VisitController`
- `org.springframework.samples.petclinic.vet.VetController`
- `org.springframework.samples.petclinic.system.WelcomeController`
- `org.springframework.samples.petclinic.system.CrashController`

### その他
- `org.springframework.samples.petclinic.owner.PetValidator` - バリデーションはAPI層で実施
- `pom.xml`から`spring-boot-starter-thymeleaf`依存関係を削除

## Next.jsが起動していない場合

Spring Bootにアクセスした際にNext.jsが起動していない場合、エラーページが表示されます：

```
Next.js Frontend Not Available

The Next.js development server is not running.
Please start it with: cd petclinic-frontend && npm run dev
```

## 本番環境への移行

### オプション1: 両方を同じサーバーで実行

```bash
# Next.jsをビルド（本番モード）
cd petclinic-frontend
npm run build
npm start  # ポート3000で実行

# Spring Bootを実行（別ターミナル）
cd spring-petclinic-to-quarkus
mvn spring-boot:run
```

### オプション2: Next.jsを静的ファイルとしてビルド（推奨されない）

現在のNext.jsは動的ルート（`[id]`, `[petId]`）を使用しているため、完全な静的エクスポートはできません。
Next.js Serverが必要です。

### オプション3: 別々のサーバーでホスト

- Next.js: Vercel、AWS、Azureなどでホスト（例: https://petclinic-frontend.com）
- Spring Boot: 別のサーバーでホスト（例: https://petclinic-api.com）
- `nextjs.url`を本番URLに変更

または、Nginxなどのリバースプロキシを使用して統合。

## トラブルシューティング

### ポート3000が使用中

```bash
# Windowsの場合
netstat -ano | findstr :3000
taskkill /PID <PID> /F

# macOS/Linuxの場合
lsof -i :3000
kill -9 <PID>
```

### Next.jsが503エラー

Next.jsサーバーが起動していることを確認：

```bash
cd petclinic-frontend
npm run dev
```

### API呼び出しが失敗

`petclinic-frontend/src/lib/api/client.ts`の`baseURL`が正しいことを確認：

```typescript
baseURL: process.env.NEXT_PUBLIC_API_URL || '/api',
```

これにより、Next.jsは相対パス`/api`を使用し、Spring Bootプロキシ経由でAPIにアクセスします。

## まとめ

この統合により：

1. **単一のポート（8080）**でフロントエンドとバックエンドの両方にアクセス可能
2. **CORS問題の解消** - 同じオリジンからのアクセスのため
3. **シンプルなデプロイ** - 1つのSpring Bootアプリケーションとして管理
4. **開発効率の向上** - Next.jsのHot Reloadを活用

ThymeleafからNext.jsへの完全な移行が完了しました。
