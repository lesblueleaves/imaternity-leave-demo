package com.iabc.springdemo.maternityleave.functools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class AllowanceTool {

    @Tool(description = "根据政府发放金额，生育津贴，产前12个月的月平均工资计算补差")
    public String  calcCompensate(String govAllowance, String birthAllowance, String avgSalary){
        log.info("{}|{}|{}", govAllowance, birthAllowance, avgSalary);
        return "XXX";
    }
}
