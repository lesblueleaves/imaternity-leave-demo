package com.iabc.springdemo.maternityleave.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaternityPolicy {
    private Long id;
    private String city;
    private String policyText;
    private double[] embedding;
}