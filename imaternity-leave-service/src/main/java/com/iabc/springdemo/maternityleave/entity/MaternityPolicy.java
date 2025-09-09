package com.iabc.springdemo.maternityleave.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public class MaternityPolicy {
    private Long id;
    private String city;
    private String leaveType;
    private String conditionKey;
    private String conditionValue;
    private Integer days;
    private String calendarType;
    private String policyText;
    private Boolean extendHoliday;
    private double[] embedding;


    @Override
    public String toString() {
        return "MaternityPolicy{" +
                "id=" + id +
                ", city='" + city + '\'' +
                ", leaveType='" + leaveType + '\'' +
                ", conditionKey='" + conditionKey + '\'' +
                ", conditionValue='" + conditionValue + '\'' +
                ", days=" + days +
                ", calendarType='" + calendarType + '\'' +
                ", policyText='" + policyText + '\'' +
                ", embedding=" + Arrays.toString(embedding) +
                '}';
    }
}