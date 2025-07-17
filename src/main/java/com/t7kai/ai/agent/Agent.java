package com.t7kai.ai.agent;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessage;
import com.openai.models.chat.completions.ChatCompletionMessageToolCall;
import com.openai.models.chat.completions.ChatCompletionToolMessageParam;
import com.t7kai.ai.agent.tools.Tool;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class Agent {

    private static final String SYSTEM_PROMPT = """
            你是一个叫不要葱姜蒜的人工智能助手。你的输出应该与用户的语言保持一致。
            当用户的问题需要调用工具时，你可以从提供的工具列表中调用适当的工具函数。
            """;

    private final OpenAIClient client;
    private final List<Class<? extends Tool>> tools;
    private final ChatCompletionCreateParams.Builder createParamsBuilder;
    private final boolean debug;

    public Agent(OpenAIClient client, String model, List<Class<? extends Tool>> tools, boolean debug) {
        this.client = client;
        this.tools = tools;
        this.createParamsBuilder = ChatCompletionCreateParams.builder()
                .model(model)
                .addSystemMessage(SYSTEM_PROMPT);
        this.tools.forEach(this.createParamsBuilder::addTool);
        this.debug = debug;
    }

    public String complete(String message) {
        createParamsBuilder.addUserMessage(message);
        ChatCompletionMessage completionMessage = client.chat()
                .completions()
                .create(createParamsBuilder.build())
                .choices()
                .getFirst()
                .message();
        String response = completionMessage.content().orElse("No response");
        if (completionMessage.toolCalls().isPresent()) {
            createParamsBuilder.addAssistantMessage(response);
            completionMessage.toolCalls()
                    .ifPresent(toolCalls -> {
                        for (ChatCompletionMessageToolCall toolCall : toolCalls) {
                            Object result = callFunction(toolCall.function());
                            createParamsBuilder.addMessage(ChatCompletionToolMessageParam.builder()
                                    .toolCallId(toolCall.id())
                                    .contentAsJson(result)
                                    .build());
                        }
                    });
            response = client.chat()
                    .completions()
                    .create(createParamsBuilder.build())
                    .choices()
                    .stream()
                    .flatMap(choice -> choice.message().content().stream())
                    .findFirst()
                    .orElse("No response");
        }
        createParamsBuilder.addAssistantMessage(response);
        return response;
    }

    private Object callFunction(ChatCompletionMessageToolCall.Function function) {
        return tools.stream()
                .filter(tool -> tool.getSimpleName().equals(function.name()))
                .findFirst()
                .map(functionClass -> {
                    Object result = function.arguments(functionClass).execute();
                    if (debug) {
                        log.info("Calling function: {}, result: {}", function.name(), result);
                    }
                    return result;
                })
                .orElseThrow(() -> new IllegalArgumentException("Unknown function: " + function.name()));
    }
}
