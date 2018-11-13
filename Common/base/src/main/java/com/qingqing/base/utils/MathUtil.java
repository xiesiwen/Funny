package com.qingqing.base.utils;

/**
 * Created by qingqing on 2017/4/20.
 *
 * Math 工具类
 */

public final class MathUtil {
    
    public static final double DOUBLE_PRECISION = 0.0000001D;
    
    public static boolean equals(double d1, double d2) {
        return Math.abs(d1 - d2) < DOUBLE_PRECISION;
    }
    
    public static boolean equals(float f1, float f2) {
        return Math.abs(f1 - f2) < DOUBLE_PRECISION;
    }
    
    public static boolean moreThan(float f1, float f2) {
        return (f1 - f2) > DOUBLE_PRECISION;
    }
    
    public static boolean moreThan(double f1, double f2) {
        return (f1 - f2) > DOUBLE_PRECISION;
    }
    
    public static boolean notLessThan(float f1, float f2) {
        return equals(f1, f2) || moreThan(f1, f2);
    }
    
    public static boolean notLessThan(double f1, double f2) {
        return equals(f1, f2) || moreThan(f1, f2);
    }
    
    public static boolean lessThan(float f1, float f2) {
        return (f2 - f1) > DOUBLE_PRECISION;
    }
    
    public static boolean lessThan(double f1, double f2) {
        return (f2 - f1) > DOUBLE_PRECISION;
    }
    
    public static boolean notMoreThan(float f1, float f2) {
        return equals(f1, f2) || lessThan(f1, f2);
    }
    
    public static boolean notMoreThan(double f1, double f2) {
        return equals(f1, f2) || lessThan(f1, f2);
    }
}
