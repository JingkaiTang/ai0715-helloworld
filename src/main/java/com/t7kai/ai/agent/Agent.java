package com.t7kai.ai.agent;

import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessage;
import com.openai.models.chat.completions.ChatCompletionMessageToolCall;
import com.openai.models.chat.completions.ChatCompletionToolMessageParam;
import com.t7kai.ai.agent.tools.Tool;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class Agent {

    private static final String SYSTEM_PROMPT = """
            你是一个叫不要葱姜蒜的人工智能助手。你的输出应该与用户的语言保持一致。
            当用户的问题需要调用工具时，你可以从提供的工具列表中调用适当的工具函数。
            """;

    private final OpenAIClient client;
    private final List<Class<? extends Tool>> tools;
    // 使用ChatCompletionCreateParams.Builder来构建对话上下文 也作为Agent的memory部分
    private final ChatCompletionCreateParams.Builder createParamsBuilder;
    // debug模式开关
    private final boolean debug;

    public Agent(OpenAIClient client, String model, List<Class<? extends Tool>> tools, boolean debug) {
        this.client = client;
        this.tools = tools;
        this.createParamsBuilder = ChatCompletionCreateParams.builder()
                .model(model)
                .addSystemMessage(SYSTEM_PROMPT)
                // 关闭思考模式
                .additionalBodyProperties(Map.of("enable_thinking", JsonValue.from(false)));
        this.tools.forEach(this.createParamsBuilder::addTool);
        this.debug = debug;
    }

    public String complete(String message) {
        // 将用户消息添加到对话上下文中
        createParamsBuilder.addUserMessage(message);
        // 调用OpenAI API获取助手回复
        ChatCompletionMessage completionMessage = client.chat()
                .completions()
                .create(createParamsBuilder.build())
                .choices()
                .getFirst()
                .message();
        // 如果助手回复中包含工具调用，则处理工具调用
        String response = completionMessage.content().orElse("No response");
        if (completionMessage.toolCalls().isPresent()) {
            createParamsBuilder.addAssistantMessage(response);
            completionMessage.toolCalls()
                    .ifPresent(toolCalls -> {
                        // 遍历所有工具调用
                        for (ChatCompletionMessageToolCall toolCall : toolCalls) {
                            Object result = callFunction(toolCall.function());
                            createParamsBuilder.addMessage(ChatCompletionToolMessageParam.builder()
                                    .toolCallId(toolCall.id())
                                    .contentAsJson(result)
                                    .build());
                        }
                    });
            // 再次调用API获取最终回复
            response = client.chat()
                    .completions()
                    .create(createParamsBuilder.build())
                    .choices()
                    .stream()
                    .flatMap(choice -> choice.message().content().stream())
                    .findFirst()
                    .orElse("No response");
        }
        // 将助手的回复添加到对话上下文中
        createParamsBuilder.addAssistantMessage(response);
        return response;
    }

    private Object callFunction(ChatCompletionMessageToolCall.Function function) {
        return tools.stream()
                .filter(tool -> tool.getSimpleName().equals(function.name()))
                .findFirst()
                .map(functionClass -> {
                    // 调用工具
                    Object result = function.arguments(functionClass).execute();
                    if (debug) {
                        log.info("Calling function: {}, result: {}", function.name(), result);
                    }
                    return result;
                })
                .orElseThrow(() -> new IllegalArgumentException("Unknown function: " + function.name()));
    }
}
