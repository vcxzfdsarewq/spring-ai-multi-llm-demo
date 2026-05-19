# Spring AI とは

Spring AI は、Spring エコシステム向けの AI エンジニアリングフレームワークです。
OpenAI、Ollama、Anthropic などの主要な LLM プロバイダーに対して統一された抽象レイヤーを提供し、
Java アプリケーションへの AI 機能の組み込みを容易にします。

## 主な機能

### マルチプロバイダー対応
Spring AI は単一の API で複数の LLM プロバイダーを切り替えられます。
設定ファイルを変えるだけで OpenAI から Ollama（ローカル LLM）へ移行できます。

### ChatClient
ChatClient は Spring AI の中心的なインターフェースで、LLM との対話を抽象化します。
プロンプトを送り、レスポンスを受け取る操作が数行で実装できます。

```java
String answer = chatClient.prompt()
    .user("Spring AI とは？")
    .call()
    .content();
```

### ストリーミング対応
Server-Sent Events（SSE）を使ったトークンごとのストリーミングレスポンスに対応しています。
体感速度が大幅に向上し、ユーザー体験が改善します。

### RAG（Retrieval-Augmented Generation）
VectorStore と組み合わせることで、社内文書や独自データを LLM に参照させる
RAG パターンを実装できます。pgvector、Redis、Pinecone などのベクトルデータベースに対応しています。

### Embedding モデル
テキストを数値ベクトルに変換する Embedding モデルも統一 API で扱えます。
Ollama の nomic-embed-text や OpenAI の text-embedding-3-small が利用可能です。

## プロファイルによる切り替え

このプロジェクトでは Spring の Profile 機能を使い、LLM プロバイダーを動的に切り替えています。

| プロファイル | LLM | Embedding |
|---|---|---|
| ollama | gemma4:e4b | nomic-embed-text |
| openai | gpt-4o | text-embedding-3-small |

## バージョン情報

- Spring AI 1.1.5（GA 安定版）
- Spring Boot 3.5.x
- Java 21 (LTS)
