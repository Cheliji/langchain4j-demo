# LangChain4j 学习 demo

**目录**

- [聊天与对应模型 ](#section-1)
- [聊天记忆](#section-2)
- [AI Services](#section-3)
- [Agent](#section-4)
- [工具调用（Tool Calling）](#section-5)
- [RAG(检索增强生成)](#section-6)

<a id="section-1"></a>
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

<a id="section-2"></a>
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


<a id="section-3"></a>
## AI Services

AI Services 是 LangChain4j 提供的高层抽象，旨在**简化与大模型的交互**。它的设计理念类似于 Spring Data JPA —— 你只需要声明一个接口，LangChain4j 会自动生成实现该接口的代理对象，屏蔽底层复杂性

>个人感觉 AI Services 更多是将调用大模型以及解析响应这一过程进行了封装

### 基本用法

#### 最简单的 AI 服务

```java
// 1. 定义接口
interface Assistant {
    String chat(String userMessage);
}

// 2. 创建模型
ChatLanguageModel model = OpenAiChatModel.builder()
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .modelName(GPT_4_O_MINI)
    .build();

// 3. 创建 AI 服务实例
Assistant assistant = AiServices.create(Assistant.class, model);

// 4. 使用
String answer = assistant.chat("Hello");
System.out.println(answer);// Hello, how can I help you?

```

**工作原理**：`AiServices` 通过反射创建代理对象，自动将 String 转为 `UserMessage`，调用 LLM 后将`AIMessage`转换回 String 返回

### 核心特性详解

**系统消息（@SystemMessage）**

通过注解为对话设置系统角色和指令

```java
interface Friend {
    @SystemMessage("You are a good friend of mine. Answer using slang.")
    String chat(String userMessage);
}

Friend friend = AiServices.create(Friend.class, model);
String answer = friend.chat("Hello"); // Hey! What's up?
```

**从资源文件加载（适合长提示词）**

```java
@SystemMessage(fromResource = "my-prompt-template.txt")
String chat(String userMessage);
```

### 结构化输出

AI Services 支持将 LLM 输出自动解析为 Java 对象

**返回 POJO（复杂对象）**

```java
class Person {
    @Description("first name of a person") // 可选描述帮助 LLM 理解
    String firstName;
    String lastName;
    LocalDate birthDate;
    Address address;
}

@Description("an address")
class Address {
    String street;
    Integer streetNumber;
    String city;
}

interface PersonExtractor {
    @UserMessage("Extract information about a person from {{it}}")
    Person extractPersonFrom(String text);
}

PersonExtractor extractor = AiServices.create(PersonExtractor.class, model);
Person person = extractor.extractPersonFrom("John Doe was born on 1968-07-04...");
```

**返回基本类型或枚举**

```java
// 布尔值
interface SentimentAnalyzer {
    @UserMessage("Does {{it}} has a positive sentiment?")
    boolean isPositive(String text);
}

// 枚举
enum Priority { CRITICAL, HIGH, LOW }

interface PriorityAnalyzer {
    @UserMessage("Analyze the priority of the following issue: {{it}}")
    Priority analyzePriority(String issueDescription);
}
```

<a id="section-4"></a>
## Agent

**引用文章**

[Building Effective AI Agents \ Anthropic](https://www.anthropic.com/engineering/building-effective-agents)

[构建高效智能体（翻译版）](https://blog.frognew.com/2025/01/building-effective-agents.html)

**LangChain4j 中构建 Agent 的方式**

- **推荐方式**：使用高层 API（AI Service + Tool API）就能构建大多数 Agent 功能
- **灵活方式**：如果需要更精细控制，可以使用底层 API（ChatLanguageModel + ToolSpceification + ChatMemeory）

### 解读 Anthropic 文章的核心观点

#### 两个概念

| 类型               | 定义                              | 使用场景                           |
| ------------------ | --------------------------------- | ---------------------------------- |
| Workflow（工作流） | 通过预定义代码路径编排 LLM 和工具 | 流程固定、可预测的任务             |
| Agent（智能体）    | LLM 动态指导自己的流程和工具使用  | 开放式、步骤无法预先确定的复杂任务 |

**核心建议**：不要为了用 Agent 而用 Agent。如果工作流能解决，就别上 Agent

#### 基础构建块：增强型 LLM

无论 Workflow 还是 Agent，基础都是**增强型 LLM**，具备三个能力：

- **检索**：主动生成搜索查询信息
- **工具使用**：理解何时调用什么工具（Tool Calling）
- **记忆**：决定保存哪些信息、何时使用

#### 五种常见工作流模式

如果你需要比单次 LLM 调用更复杂的逻辑，可以考虑这些模式

1. **Prompt Chaining（提示链）**：把任务拆成几步，上一步输出作为下一步输入
2. **Routing（路由）**：先让 LLM 判断任务类型，在路由到不同的处理流程
3. **Parallelization（并行化）**：把任务拆成多个子任务并行执行，在整合结果
4. **Orchestrator-Workers（编排器-工人）**：一个中央协调者动态拆解任务，分配给多个子代理并行处理，再汇总结果
5. **Evaluator-Optimizer（评估-优化）**：一个 LLM 生成结果，另一个 LLM 评估，循环迭代直到质量达标

**真正的 Agent** 是让 LLM 自己循环决定：下一步做什么、调用什么工具、什么时候结束

#### 构建 Agent 的三个黄金法则

1. **保持简单**：不要为了复杂而复杂，简单的 Prompt 能搞定的就别用 Agent
2. **保持透明**：让用户清楚看到 Agent 的规划步骤和决策过程
3. **精心设计工具接口（ACI）**：给 LLM 用的工具必须有清楚文档、参数描述准确，要像给初级工程师写文档一样认真

#### Agent 最适合什么场景

- 需要**对话+行动**结合的任务
- 有明确的**成功标准**（能判断是否做对）
- 能实现**反馈循环**（错了能改）
- 能融入有效的人工监督

<a id="section-5"></a>

## 工具调用（Tool Calling）

**工具可以是任何东西**：网络搜索、调用外部 API、执行特定代码片段、数据库查询等

**工作流程**：用户提问 -> LLM 判断是否需要工具 -> 如需工具则生成调用请求 -> 开发这执行工具 -> 将结果返回给 LLM -> LLM 生成最终回答

LangChain4j 提供了两种使用工具的方式

| 级别   | 使用方式                                           | 适用场景                   |
| ------ | -------------------------------------------------- | -------------------------- |
| 低级别 | 使用 `ChatLanguageModel` 和`ToolSpecification` API | 需要精细控制工具调用流程   |
| 高级别 | 使用 AI services 和`@Tool` 注解的 Java 方法        | 快速开发、自动处理工具调用 |



### 注解详解

#### `@Tool`注解

用于标记可由 LLM 调用的方法

```java
class Calculator {
    @Tool  // name 默认为方法名 "add"
    double add(int a, int b) {
        return a + b;
    }

    @Tool("返回给定数字的平方根")  // 自定义描述
    double squareRoot(double x) {
        return Math.sqrt(x);
    }
}
```

**`@Tool`注解属性**

- `name`可选：工具名称，默认为方法名
- `value`可选：工具描述，帮助 LLM 理解何时使用该工具

#### `@P`注解

用于描述方法参数

```java
@Tool("获取指定城市的天气预报")
String getWeather(
        @P("需要查询天气的城市名称") String city,  // 描述参数含义
        @P("预报天数，范围 1-7 天") int days
) {
    return city + "未来" + days + "天天气：晴转多云";
}
```

**`@P`注解属性**

- `value`必选：参数描述
- `required`可选：参数是否必需，默认为 true

#### `Description`注解

用于描述类和字段：

```java
@Description("要执行的查询")
class Query {
    @Description("要选择的字段")
    private List<String> select;
    
    @Description("过滤条件")
    private List<Condition> where;
}

@Tool
Result executeQuery(Query query) {
    // 实现
}
```



**注意**：在 SpringBoot 中，必须将工具类加入 Spring 容器中管理

```java
@Component //关键：必须被 Spring 管理
public class CityInformation {

    @Tool("返回给定城市的天气预报")
    public String getWeather(@P("指定城市")String city) {
        return city + "最近几天下雨" ;
    }


    @Tool("返回给定城市现在的道路情况")
    public String getRoadInformation(@P("指定城市")String city) {
        return city + "路况不错,不塞车" ;
    }

}
```

<a id="section-6"></a>
## RAG

### 文档加载与解析（Document Loaders & Parsers）

#### 核心抽象

LangChain4j 中，文档解析遵循统一的接口设计

```java
// 核心接口：将输入流转换为 Document
public interface DocumentParser {
    Document parse(InputStream inputStream);
}

// Document 对象结构
public class Document {
    private final String text;           // 文档文本内容
    private final Metadata metadata;     // 元数据（文件名、URL、页码等）
}
```

#### 内置解析器

**PDF 解析（ApachePdfBoxDocumentParser）**

```java
DocumentParser parser = new ApachePdfBoxDocumentParser();
        Document parse = parser.parse(new FileInputStream(file));

String text = parse.text() ;

System.out.println("解析文件的文本内容：" + text());
```

**Office 文档解析（MsOfficeDocumentParser)**

```java
// 支持 Word、Excel、PowerPoint
DocumentParser parser = new MsOfficeDocumentParser(DOCUMENT);  // Word
DocumentParser parser = new MsOfficeDocumentParser(SPREADSHEET);  // Excel  
DocumentParser parser = new MsOfficeDocumentParser(PRESENTATION);  // PPT

// 实际使用：自动检测类型
DocumentParser autoParser = new MsOfficeDocumentParser();

// 解析 Word 文档
Document doc = autoParser.parse(new FileInputStream("contract.docx"));
```

**文本解析（TextDocumentParser）**

```java
// 纯文本文件
DocumentParser parser = new TextDocumentParser();

// 支持指定编码
DocumentParser utf8Parser = new TextDocumentParser(StandardCharsets.UTF_8);
DocumentParser gbkParser = new TextDocumentParser(Charset.forName("GBK"));

// Markdown 也属于文本，但保留标记
Document mdDoc = parser.parse(new FileInputStream("readme.md"));
// 内容包含 # ## 等 Markdown 标记，分割时可利用这些结构
```

#### 元数据管理

> LangChain4j RAG 大部分解析器都是默认不会自动提取文件元数据

```java
// 2. 手动构建元数据
Metadata metadata = new Metadata();
        
// 文件基本信息
metadata.put("file_name", filePath.getFileName().toString());
metadata.put("file_path", filePath.toAbsolutePath().toString());
metadata.put("file_extension", getFileExtension(filePath));
metadata.put("file_size_bytes", Files.size(filePath));
        
// 时间信息
BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
metadata.put("creation_time", attrs.creationTime().toString());
metadata.put("last_modified_time", attrs.lastModifiedTime().toString());
metadata.put("last_access_time", attrs.lastAccessTime().toString());
        
// 自定义业务标签
metadata.put("source_type", "product_manual");
metadata.put("doc_type", "markdown");
```
