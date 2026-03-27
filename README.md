# LangChain4j 学习 demo

## 聊天与对应模型

| 接口                           | 能力        | 适用场景       |
| ---------------------------- | --------- | ---------- |
| `ChatLanguageModel`          | 基础对话（阻塞式） | 简单问答、文本生成  |
| `StreamingChatLanguageModel` | 流式对话      | 实时响应、打字机效果 |
| `LanguageModel`              | 文本补全（旧版）  | 遗留系统兼容     |
| `EmbeddingModel`             | 文本向量化     | RAG、语义搜索   |

**接口继承关系**
```text
LanguageModel (基础接口)
    ├── ChatLanguageModel (聊天模型)
    │       └── StreamingChatLanguageModel (流式聊天)
    └── EmbeddingModel (嵌入模型)
```

### ChatLanguageModel 使用
LangChain4j 启动时会自动将配置文件（application.yml）
的配置装填至 ChatLanguageModel 中，所以在发送消息是，
一般直接发送即可

```java
@GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return chatLanguageModel.chat(message);
    }
```

### StreamingChatLanguageModel 
**流式响应**，一种通讯协议，虽然底层也是借助 Http 请求，
但是属于**保持连接状态，服务端给客户端持续发送消息**的情况，
和正常 Http 请求还是有一些不同。可以参考[SEE协议与流式响应](https://wx.zsxq.com/group/51121244585524/topic/14588152514152842)
。代码编写时需要使用`SseEmitter`

```java
@GetMapping(value = "/chat" , produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestParam String message) {

        SseEmitter sseEmitter = new SseEmitter();

        streamingChatLanguageModel.chat(message, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String message) {
                try {
                    System.out.println("message: " + message);
                    sseEmitter.send(SseEmitter.event()
                            .data(message, MediaType.APPLICATION_JSON)
                            .name("message"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onCompleteResponse(ChatResponse chatResponse) {
                TokenUsage tokenUsage = chatResponse.tokenUsage();

                Integer inputTokenCount = tokenUsage.inputTokenCount();
                Integer outputTokenCount = tokenUsage.outputTokenCount();
                Integer totalTokenCount = tokenUsage.totalTokenCount();
                System.out.println("inputTokenCount: " + inputTokenCount);
                System.out.println("outputTokenCount: " + outputTokenCount);
                System.out.println("totalTokenCount: " + totalTokenCount);

                sseEmitter.complete();

            }

            @Override
            public void onError(Throwable throwable) {
                sseEmitter.completeWithError(throwable);
            }
        });

        return sseEmitter;

    }
```

## 聊天记忆

### 记忆与历史

- 历史保持用户和 AI 之间的**所有消息完整无缺**。历史使用户在 UI 中看到的内容。它代表实际对话内容
- 记忆保存**一些信息**，这些信息呈现给 LLM，使其表现的好像“记住”了对话。记忆与历史有很大不同。根据使用记忆算法，它可以以各种方式修改历史：淘汰一些消息，总结多于消息，总结单独的消息，从消息中删除不重要的细节，向消息中注入额外信息（例如，用于 RAG）或指令（例如，用户结构化输出）等等

### ChatMemory 接口体系

```java
public interface ChatMemory {
    // 获取当前存储的所有消息
    List<ChatMessage> messages();
    
    // 添加消息到记忆
    void add(ChatMessage message);
    
    // 清除所有记忆
    void clear();
}
```

**消息类型支持**

| 消息类型                     | 说明         | 存储方式           |
| ---------------------------- | ------------ | ------------------ |
| `UserMessage`                | 用户输入     | 直接存储           |
| `AiMessage`                  | AI 回复      | 直接存储           |
| `SystemMessage`              | 系统指令     | 通常单独处理       |
| `ToolExecutionResultMessage` | 工具执行结果 | 与工具请求配对存储 |



### 两种核心实现方式

**MessageWindowChatMessage（消息窗口）**
**原理**：按消息数量滑动窗口，保留最近 N 条消息

**TokenWindowChatMemory**
**原理**：按 Token 数量滑动窗口，保留最近 N 个 Token



### 持久化存储

默认情况下，`ChatMemory`实现在内存中存储`ChatMessage`

如果需要持久化，可以实现自定义的`ChatMemoryStore`，将`ChatMessage`存储

> 这里的持久化指的是记忆的持久化，而不是历史的持久化

```java
class PersistentChatMemoryStore implements ChatMemoryStore {

        @Override
        public List<ChatMessage> getMessages(Object memoryId) {
          // TODO: 实现通过内存ID从持久化存储中获取所有消息。
          // 可以使用ChatMessageDeserializer.messageFromJson(String)和
          // ChatMessageDeserializer.messagesFromJson(String)辅助方法
          // 轻松地从JSON反序列化聊天消息。
        }

        @Override
        public void updateMessages(Object memoryId, List<ChatMessage> messages) {
            // TODO: 实现通过内存ID更新持久化存储中的所有消息。
            // 可以使用ChatMessageSerializer.messageToJson(ChatMessage)和
            // ChatMessageSerializer.messagesToJson(List<ChatMessage>)辅助方法
            // 轻松地将聊天消息序列化为JSON。
        }

        @Override
        public void deleteMessages(Object memoryId) {
          // TODO: 实现通过内存ID删除持久化存储中的所有消息。
        }
    }

ChatMemory chatMemory = MessageWindowChatMemory.builder()
        .id("12345")
        .maxMessages(10)
        .chatMemoryStore(new PersistentChatMemoryStore())
        .build();
```

每当向`ChatMemory`添加新的`ChatMessage`时，都会调用`updateMessages()`方法。 这通常在与LLM的每次交互中发生两次： 一次是添加新的`UserMessage`时，另一次是添加新的`AiMessage`时。 `updateMessages()`方法预期会更新与给定内存ID关联的所有消息。 `ChatMessage`可以单独存储（例如，每条消息一条记录/行/对象） 或一起存储（例如，整个`ChatMemory`一条记录/行/对象）
