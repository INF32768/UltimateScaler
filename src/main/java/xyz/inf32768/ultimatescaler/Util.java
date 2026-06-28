package xyz.inf32768.ultimatescaler;

/**
 * 实用工具类，包含了本模组一些核心或常用的方法。
 */
public class Util {
    // 私有构造方法，防止实例化
    private Util() {}

    /**
     * 计算两个整数的平均值。
     * <p>
     * 使用位运算来计算，避免了传统的除法算法中整数溢出的问题，且通常性能更好。
     * @param i 第一个整数
     * @param j 第二个整数
     * @return 两个整数的平均值（向下取整）
     */
    public static int average(int i , int j) {
        return (i & j) + ((i ^ j) >> 1);
    }
}
