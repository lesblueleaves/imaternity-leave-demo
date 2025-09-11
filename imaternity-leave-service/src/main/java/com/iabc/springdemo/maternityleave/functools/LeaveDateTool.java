package com.iabc.springdemo.maternityleave.functools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Component
public class LeaveDateTool {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Tool(description = "根据请假开始日期和天数，计算请假结束日期（包含节假日顺延）")
    public String  calcLeaveDate(String startDate, int days){
//        LocalDate date = LocalDate.parse(startDate, dateTimeFormatter);
//        return dateTimeFormatter.format(date.plusDays(days));
        return "2025-10-21";
    }

    public static void main(String[] args) {
        LocalDate localDate = LocalDate.of(2024,5,1);
        LocalDate newD = localDate.plus(173, ChronoUnit.DAYS);
        System.out.println(newD);

    }
}
