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

