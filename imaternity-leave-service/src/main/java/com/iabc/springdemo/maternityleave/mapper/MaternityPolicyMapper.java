package com.iabc.springdemo.maternityleave.mapper;


import com.iabc.springdemo.maternityleave.entity.MaternityPolicy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MaternityPolicyMapper {

    List<MaternityPolicy> findSimilarPolicies(@Param("embedding") String embedding, @Param("limit") int limit);

}