package com.iabc.springdemo.maternityleave.service;

import com.iabc.springdemo.maternityleave.entity.MaternityPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanService {

    @Autowired
    private MaternityLeaveRagService maternityLeaveRagService;

    @Autowired
    private BigModelService bigModelService;

    public String plan(String question) {
        List<MaternityPolicy> list = maternityLeaveRagService.queryMaternityPolicy(question);
        String resp = calculateLeaveDays(list.get(0).getPolicyText(), question);
        return resp;
    }

    public String calculateLeaveDays(String policy, String userQuery) {
        // 2. 构造 Prompt
        String prompt = """
                已知以下上海产假政策规则：
                %s
                
                用户问题：%s
                
                请严格根据规则，计算员工应享受的总产假天数，并给出推理过程和最终答案。只回答最后数字即可。
                """.formatted(policy, userQuery);

        String prompt1 = """
                已知以下上海产假政策规则：
                %s
         
                请严格根据规则，计算员工应享受的总产假天数，若是无法计算则答案是0天，只回答最后数字即可。
                """.formatted(policy);

        String prompt2 = """
                已知政策规则：
                %s
                
                用户问题：
                %s
                
                请从政策规则中提取员工应享受的产假天数，
                然后调用工具函数计算请假结束日期，最终输出：
                【产假开始日期】【总天数】和【产假结束日期】。
                """.formatted(policy, userQuery);

//        OpenAiChatOptions options = OpenAiChatOptions.builder()
//                .model("glm-4.5")        // 指定智谱模型名（或其它兼容名称）
//                .temperature(0.0d)    // 可选：设置温度等
//                .build();
//        ChatClient.CallResponseSpec response = chatClient.prompt(prompt2)
//                .call();
//        // 3. 调用 DeepSeek
//        ChatClient.CallResponseSpec response = chatClient.prompt(prompt).
//                .options(opt -> opt.("glm-4"))
//                .call();
        String resp = "";
        String days = bigModelService.callZhiPuBigModel(prompt2, userQuery);
        resp = days;
        return resp;
    }


}
