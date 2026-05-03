# spring-ai-rag-lab

Java / Spring Boot / Spring AI を使って、業務システムに生成AI機能を組み込む検証プロジェクト。

## 目的

Javaバックエンドエンジニアとして、既存業務システムにLLM/RAG機能を安全に統合するための実装パターンを検証する。

## 技術スタック

- Java 21
- Spring Boot 3.x
- Spring AI
- OpenAI API / Azure OpenAI
- Docker
- JUnit

---

## 起動方法

### 前提条件

- Java 21 以上
- Maven 3.9 以上
- OpenAI API キー

### 環境変数の設定

**Mac / Linux**
```bash
export OPENAI_API_KEY=sk-...
```

**Windows (PowerShell)**
```powershell
$env:OPENAI_API_KEY = "sk-..."
```

### ビルド・起動

```bash
./mvnw spring-boot:run
```

または JAR を作ってから起動：

```bash
./mvnw package -DskipTests
java -jar target/spring-ai-multi-llm-demo-0.0.1-SNAPSHOT.jar
```

---

## API

### `POST /api/chat`

ユーザーの質問を受け取り、LLM の回答を JSON で返す。

**リクエスト**

```json
{
  "question": "Spring AIとは何ですか？"
}
```

**レスポンス (200 OK)**

```json
{
  "answer": "Spring AIは、Springエコシステム向けのAIフレームワークです...",
  "model": "gpt-4o"
}
```

**バリデーションエラー (400 Bad Request)**

```json
{
  "message": "question: 質問は必須です",
  "status": 400
}
```

**curl での確認例**

```bash
curl -s -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "Spring AIとは何ですか？"}' | jq
```

---

## テスト

```bash
./mvnw test
```

| テストクラス | 種別 | 内容 |
|---|---|---|
| `ChatControllerTest` | `@WebMvcTest` | バリデーション・正常系・異常系のHTTPレイヤーテスト |
| `ChatServiceTest` | Mockito 単体 | ChatClient のフルエントAPIをモックしてサービスロジックを検証 |
| `SpringAiDemoApplicationTests` | `@SpringBootTest` | アプリケーションコンテキストの起動確認 |

---

## プロジェクト構成

```
src/main/java/com/example/demo/
├── SpringAiDemoApplication.java          # エントリポイント
├── config/
│   └── ChatConfig.java                   # ChatClient Bean 定義
├── controller/
│   └── ChatController.java               # POST /api/chat
├── service/
│   └── ChatService.java                  # Spring AI 呼び出しロジック
├── dto/
│   ├── ChatRequest.java                  # リクエスト DTO (record)
│   ├── ChatResponse.java                 # レスポンス DTO (record)
│   └── ErrorResponse.java               # エラーレスポンス DTO (record)
└── exception/
    └── GlobalExceptionHandler.java       # バリデーションエラーの統一ハンドリング
```
