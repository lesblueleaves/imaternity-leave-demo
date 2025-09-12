package com.iabc.springdemo.maternityleave.service;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.ChatCompletionCreateParams;
import ai.z.openapi.service.model.ChatCompletionResponse;
import ai.z.openapi.service.model.ChatFunction;
import ai.z.openapi.service.model.ChatFunctionParameterProperty;
import ai.z.openapi.service.model.ChatFunctionParameters;
import ai.z.openapi.service.model.ChatMessage;
import ai.z.openapi.service.model.ChatMessageRole;
import ai.z.openapi.service.model.ChatThinking;
import ai.z.openapi.service.model.ChatTool;
import ai.z.openapi.service.model.ChatToolType;
import ai.z.openapi.service.model.ToolCalls;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.iabc.springdemo.maternityleave.functools.AllowanceTool;
import com.iabc.springdemo.maternityleave.functools.LeaveDateTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class BigModelService {

    private final ZhipuAiClient zhipuAiClient;

    @Autowired
    private LeaveDateTool leaveDateTool;

    @Autowired
    private AllowanceTool allowanceTool;

    /**
     * 调用智普大模型
     *
     * @param prompt 提示词
     * @param query  用户问题
     * @return 答案
     */
    public String callBigModel(String prompt, String query) {
        List<ChatMessage> messages = Lists.newArrayList(
                ChatMessage.builder()
                        .role(ChatMessageRole.SYSTEM.value())
                        .content(prompt)
                        .build(),
                ChatMessage.builder()
                        .role(ChatMessageRole.USER.value())
                        .content(query)
                        .build()
        );
        messages.add(ChatMessage.builder()
                .role(ChatMessageRole.USER.value())
                .content("政府发放金额是10000元，生育津贴是20000元和产前12个月的月平均工资是3000元")
                .build());

        // 创建聊天完成请求
        ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                .model("glm-4.5")
                .messages(messages)
                .tools(buildChatTools())
                .toolChoice("auto")
                .thinking(ChatThinking.builder().type("disabled").build())
                .maxTokens(9102)
                .temperature(0.1f)
                .build();

        // 发送请求
        ChatCompletionResponse response = zhipuAiClient.chat().createChatCompletion(request);

        // 获取回复
        String resp ="";

        if (response.isSuccess()) {
            // 处理函数调用
            ChatMessage assistantMessage = response.getData().getChoices().get(0).getMessage();
            if (assistantMessage.getToolCalls() != null && !assistantMessage.getToolCalls().isEmpty()) {
                for (ToolCalls toolCall : assistantMessage.getToolCalls()) {
                    String functionName = toolCall.getFunction().getName();
                    String text = toolCall.getFunction().getArguments().asText();
                    JSONObject jsonObject = JSONObject.parseObject(text);
                    if ("calcLeaveDate".equals(functionName)) {
                        String startDate = jsonObject.getString("startDate");
                        int days = jsonObject.getInteger("leaveDays");
                        resp = leaveDateTool.calcLeaveDate(startDate, days);
                        log.info("请假结束时间: " + resp);
                    }
                    if ("calcAllowance".equals(functionName)) {
                        resp = allowanceTool.calcCompensate("00", "20000", "30000");
                        log.info("需补差: " + resp);
                    }
                    // 二次调用模型，把工具结果传回去
                    messages.add(assistantMessage); // 添加模型的回复（包含函数调用请求）
                    messages.add(ChatMessage.builder()
                            .role("tool")
                            .content(resp)
                            .toolCallId(toolCall.getId()) // 关联对应的函数调用
                            .build());
                }
                // 创建聊天完成请求
                ChatCompletionCreateParams seqRequest = ChatCompletionCreateParams.builder()
                        .model("glm-4.5")
                        .messages(messages)
                        .thinking(ChatThinking.builder().type("disabled").build())
                        .maxTokens(9102)
                        .temperature(0.1f)
                        .build();

                // 发送请求
                ChatCompletionResponse secresponse = zhipuAiClient.chat().createChatCompletion(seqRequest);
                String finalAnswer = secresponse.getData().getChoices().get(0).getMessage().getContent().toString();
                log.info("最终回答: " + finalAnswer);
                return finalAnswer;
            } else {
                System.out.println(assistantMessage.getContent());
            }
        } else {
            System.err.println("错误: " + response.getMsg());
        }

        return resp;
    }


    /**
     * 构建func list
     * @return func列表
     */
    private List<ChatTool> buildChatTools(){
        // 产假结束时间Func tool
        Map<String, ChatFunctionParameterProperty> properties = new HashMap<>();
        ChatFunctionParameterProperty startDateProperty = ChatFunctionParameterProperty
                .builder().type("string").description("请假开始日期，格式yyyy-MM-dd").build();
        properties.put("startDate", startDateProperty);
        ChatFunctionParameterProperty leaveDaysProperty = ChatFunctionParameterProperty
                .builder().type("integer").description("请假天数").build();
        properties.put("leaveDays", leaveDaysProperty);
        ChatTool chatTool = ChatTool.builder()
                .type(ChatToolType.FUNCTION.value())
                .function(ChatFunction.builder()
                        .name("calcLeaveDate")
                        .description("根据请假开始日期和请假天数计算结束日期")
                        .parameters(ChatFunctionParameters.builder()
                                .type("object")
                                .properties(properties)
                                .required(Collections.singletonList("startDate"))
                                .build())
                        .build())
                .build();

        // 津贴计算Func tool
        Map<String, ChatFunctionParameterProperty> allowanceProperties = new HashMap<>();
        ChatFunctionParameterProperty govAllowanceProperty = ChatFunctionParameterProperty
                .builder().type("string").description("政府发放金额").build();
        allowanceProperties.put("govAllowance", govAllowanceProperty);
        ChatFunctionParameterProperty birthAllowanceProperty = ChatFunctionParameterProperty
                .builder().type("string").description("生育津贴").build();
        allowanceProperties.put("birthAllowance", birthAllowanceProperty);
        ChatFunctionParameterProperty avgSalaryProperty = ChatFunctionParameterProperty
                .builder().type("string").description("产前12个月的月平均工资").build();
        allowanceProperties.put("avgSalary", avgSalaryProperty);
        ChatTool allowanceChatTool = ChatTool.builder()
                .type(ChatToolType.FUNCTION.value())
                .function(ChatFunction.builder()
                        .name("calcAllowance")
                        .description("根据政府发放金额，生育津贴，产前12个月的月平均工资计算补差")
                        .parameters(ChatFunctionParameters.builder()
                                .type("object")
                                .properties(allowanceProperties)
                                .required(Lists.newArrayList("govAllowance", "birthAllowance", "avgSalary"))
                                .build())
                        .build())
                .build();
        return Lists.newArrayList(chatTool, allowanceChatTool);
    }

}
