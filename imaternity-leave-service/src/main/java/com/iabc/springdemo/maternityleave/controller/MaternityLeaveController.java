package com.iabc.springdemo.maternityleave.controller;

import com.iabc.springdemo.maternityleave.service.PlanService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maternity-leave")
public class MaternityLeaveController {
//
//    @Autowired
//    private MaternityLeaveRagService maternityLeaveRagService;

    @Autowired
    private PlanService planService;



    @GetMapping
    public String queryPolicy(String q) {
//        return maternityLeaveRagService.queryMaternityPolicy( q);
        return planService.plan(q);
    }


    @Getter
    @Setter
    public static class CalculationRequest {
        private String city;
        private String leaveType;
        private String conditionKey;
        private String conditionValue;
        private int numberOfBabies;
        private boolean lateBirth;
    }
}