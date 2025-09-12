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

    /**
     * 对于用户问题做规划
     * @param question 用户问题
     * @return 问题的回答
     */
    public String plan(String question) {

        // step1 get rag
        List<MaternityPolicy> list = maternityLeaveRagService.queryMaternityPolicy(question);

        // step2 func call
        String resp = doCalculate(list.get(0).getPolicyText(), question);
        return resp;
    }

    public String doCalculate(String policy, String userQuery) {
        String prompt2 = """
                已知政策规则：
                %s

                用户问题：
                %s

                请从政策规则中提取员工应享受的产假天数，然后
                1. 调用工具函数计算请假结束日期，
                2. 调用工具根据政府发放金额，生育津贴，产前12个月的月平均工资，计算补差

                最终输出：
                【产假开始日期】【总天数】和【产假结束日期】【补差金额】即可。
                """.formatted(policy, userQuery);
        String resp = "";
        String days = bigModelService.callBigModel(prompt2, userQuery);
        resp = days;
        return resp;
    }


}
