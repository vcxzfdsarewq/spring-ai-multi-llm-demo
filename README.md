# spring-ai-multi-llm-demo

Java / Spring Boot / Spring AI を使って、業務システムに生成AI機能を組み込む検証プロジェクト。  
Ollama（ローカルLLM）と OpenAI を設定で切り替え可能。

## 目的

Javaバックエンドエンジニアとして、既存業務システムにLLM機能を安全に統合するための実装パターンを検証する。

## 技術スタック

- Java 25
- Spring Boot 3.5.x
- Spring AI 1.0.0-M6
- Ollama（デフォルト）/ OpenAI API
- JUnit / Mockito

---

## LLMプロバイダーの切り替え

`src/main/resources/application.yml` の `spring.profiles.active` で切り替える。

```yaml
spring:
  profiles:
    active: ollama   # "openai" に変更するとOpenAIを使用
```

| プロファイル | プロバイダー | 必要なもの |
|---|---|---|
| `ollama`（デフォルト） | ローカルLLM | Ollama インストール済み |
| `openai` | OpenAI API | `OPENAI_API_KEY` 環境変数 |

---

## 起動方法

### Ollama で起動する（デフォルト）

**1. Ollamaのインストール**

https://ollama.com/download からWindows版をインストール。  
インストール後、`http://localhost:11434` で自動起動する。

**2. モデルのダウンロード**

```powershell
ollama pull gemma4:e4b
```

**3. アプリ起動**

```powershell
./mvnw spring-boot:run
```

モデルやURLは環境変数で上書き可能：

```powershell
$env:OLLAMA_MODEL = "llama3.2"
$env:OLLAMA_BASE_URL = "http://192.168.1.10:11434"
./mvnw spring-boot:run
```

---

### OpenAI で起動する

**1. `application.yml` を編集**

```yaml
spring:
  profiles:
    active: openai
```

**2. APIキーを設定して起動**

```powershell
$env:OPENAI_API_KEY = "sk-..."
./mvnw spring-boot:run
```

モデルの変更：

```powershell
$env:OPENAI_MODEL = "gpt-4o-mini"
```

---

## API

### `POST /api/chat` — 通常レスポンス

全文生成完了後にJSONで返す。

**リクエスト**

```json
{ "question": "Spring AIとは何ですか？" }
```

**レスポンス (200 OK)**

```json
{
  "answer": "Spring AIは、Springエコシステム向けのAIフレームワークです...",
  "model": "gemma4:e4b"
}
```

**PowerShell での確認例**

```powershell
$body = [System.Text.Encoding]::UTF8.GetBytes('{"question": "Spring AIとは何ですか？"}')
$res = Invoke-RestMethod -Uri "http://localhost:8080/api/chat" -Method POST -ContentType "application/json; charset=UTF-8" -Body $body
$res.answer
```

---

### `POST /api/chat/stream` — ストリーミングレスポンス（SSE）

トークン生成のたびに即座にテキストが流れる。体感速度が大幅に向上する。

**PowerShell / curl での確認例**

```powershell
# UTF-8ファイル経由でcurl.exeに渡す
$body = '{"question": "Spring AIとは何ですか？"}'
[System.IO.File]::WriteAllText("$env:TEMP\req.json", $body, [System.Text.Encoding]::UTF8)

curl.exe -N -X POST http://localhost:8080/api/chat/stream `
  -H "Content-Type: application/json; charset=UTF-8" `
  --data-binary "@$env:TEMP\req.json"
```

```bash
# Git Bash
curl -N -s -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"question": "What is Spring AI?"}'
```

---

### エラーレスポンス

**バリデーションエラー (400)**

```json
{ "message": "question: 質問は必須です", "status": 400 }
```

**エンコードエラー (400)** — UTF-8以外のリクエスト送信時

```json
{ "message": "リクエストの解析に失敗しました。Content-Type: application/json; charset=UTF-8 でリクエストを送信してください。", "status": 400 }
```

**サーバーエラー (500)**

```json
{ "message": "サーバーエラーが発生しました: ...", "status": 500 }
```

---

## テスト

```powershell
./mvnw test
```

| テストクラス | 種別 | 内容 |
|---|---|---|
| `ChatControllerTest` | `@WebMvcTest` | バリデーション・正常系・異常系のHTTPレイヤーテスト |
| `ChatServiceTest` | Mockito 単体 | ChatClientのモックによるサービスロジック検証 |
| `SpringAiDemoApplicationTests` | `@SpringBootTest` | アプリケーションコンテキストの起動確認 |

---

## プロジェクト構成

```
src/main/
├── java/com/example/demo/
│   ├── SpringAiDemoApplication.java
│   ├── config/
│   │   └── ChatConfig.java                 # プロファイル別 ChatClient Bean定義
│   ├── controller/
│   │   └── ChatController.java             # POST /api/chat, POST /api/chat/stream
│   ├── service/
│   │   └── ChatService.java                # Spring AI 呼び出し（通常・ストリーミング）
│   ├── dto/
│   │   ├── ChatRequest.java
│   │   ├── ChatResponse.java
│   │   └── ErrorResponse.java
│   └── exception/
│       └── GlobalExceptionHandler.java     # エラーの統一ハンドリング
└── resources/
    ├── application.yml                     # 共通設定・プロファイル指定
    ├── application-ollama.yml              # Ollama設定（デフォルト）
    └── application-openai.yml             # OpenAI設定
```
