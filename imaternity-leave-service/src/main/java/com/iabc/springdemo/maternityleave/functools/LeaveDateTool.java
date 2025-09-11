package com.iabc.springdemo.maternityleave.functools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Locale;


@Slf4j
@Component
public class LeaveDateTool {
    private static final DateTimeFormatter MULTI_FORMATTER = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-M-d"))
            .toFormatter(Locale.CHINA);


    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @Tool(description = "根据请假开始日期和天数，计算请假结束日期（包含节假日顺延）")
    public String  calcLeaveDate(String startDate, int days){
        LocalDate date = parseDate(startDate);
        date = date.plusDays(days);
        return dateTimeFormatter.format(date);
    }

    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, MULTI_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("无法解析日期: " + dateStr, e);
        }
    }

    public static void main(String[] args) {
        LeaveDateTool leaveDateTool = new LeaveDateTool();
        System.out.println(leaveDateTool.parseDate("2025-5-1"));
        System.out.println(leaveDateTool.parseDate("2025-11-1"));

    }
}
