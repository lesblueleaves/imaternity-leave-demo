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
        String prompt2 = """
                已知政策规则：
                %s
                
                用户问题：
                %s
                
                请从政策规则中提取员工应享受的产假天数，
                然后调用工具函数计算请假结束日期，最终输出：
                【产假开始日期】【总天数】和【产假结束日期】即可。
                """.formatted(policy, userQuery);
        String resp = "";
        String days = bigModelService.callZhiPuBigModel(prompt2, userQuery);
        resp = days;
        return resp;
    }


}
