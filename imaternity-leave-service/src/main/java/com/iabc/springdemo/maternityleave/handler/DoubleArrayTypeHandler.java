package com.iabc.springdemo.maternityleave.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

@MappedTypes(double[].class)
public class DoubleArrayTypeHandler extends BaseTypeHandler<double[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, double[] parameter, JdbcType jdbcType) throws SQLException {
        // 将 double 数组转换为 PostgreSQL 数组格式的字符串
        String arrayString = "[" + Arrays.toString(parameter).replace("[", "").replace("]", "") + "]";
        ps.setString(i, arrayString);
    }

    @Override
    public double[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String arrayString = rs.getString(columnName);
        return parseDoubleArray(arrayString);
    }

    @Override
    public double[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String arrayString = rs.getString(columnIndex);
        return parseDoubleArray(arrayString);
    }

    @Override
    public double[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String arrayString = cs.getString(columnIndex);
        return parseDoubleArray(arrayString);
    }

    private double[] parseDoubleArray(String arrayString) {
        if (arrayString == null || arrayString.isEmpty() || arrayString.equals("NULL")) {
            return null;
        }

        // 移除方括号和空格
        String cleaned = arrayString.replace("[", "").replace("]", "").replace(" ", "");

        if (cleaned.isEmpty()) {
            return new double[0];
        }

        // 分割字符串并转换为 double 数组
        String[] parts = cleaned.split(",");
        double[] result = new double[parts.length];

        for (int i = 0; i < parts.length; i++) {
            try {
                result[i] = Double.parseDouble(parts[i]);
            } catch (NumberFormatException e) {
                result[i] = 0.0;
            }
        }

        return result;
    }
}