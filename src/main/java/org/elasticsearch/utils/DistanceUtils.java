/**
 * Project Name: elasticsearch-similarity-plugin
 * File Name: DistanceUtils
 * Date: 2020/4/17 13:19
 * Copyright © 2015-2020, 杭州核盛网络科技有限公司
 */
package org.elasticsearch.utils;

import java.math.BigDecimal;

/**
 * 距离计算工具类
 * @Author liutwv
 * @Date 2020/4/17 13:19
 */
public class DistanceUtils {

    // 小数点后保留几位小数
    private static final Integer SCALE = 6;

    /**
     * 计算汉明距离
     * @param str1
     * @param str2
     * @return
     */
    public static int hammingDistance(String str1, String str2) {
        int num = 0;
        char[] inputCharArray = str1.toCharArray();
        char[] targetCharArray = str2.toCharArray();
        for (int i = 0; i < inputCharArray.length; i ++) {
            if (inputCharArray[i] != targetCharArray[i]) {
                num ++;
            }
        }
        return num;
    }

    /**
     * 计算欧几里得距离
     * @param str1
     * @param str2
     * @return
     */
    public static double getEuclideanDistance(String str1, String str2) {
        return getEuclideanDistanceInBigDecimal(str1, str2).setScale(SCALE, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 计算欧几里得距离，返回BigDecimal结果
     * @param str1
     * @param str2
     * @return
     */
    protected static BigDecimal getEuclideanDistanceInBigDecimal(String str1, String str2) {
        char[] iArray = str1.toCharArray();
        char[] tArray = str2.toCharArray();
        BigDecimal result = BigDecimal.ZERO;
        for (int i = 0; i < iArray.length; i++) {
            BigDecimal inputValue = new BigDecimal(iArray[i]);
            BigDecimal targetValue = new BigDecimal(tArray[i]);
            result = result.add(inputValue.subtract(targetValue).pow(2));
        }
        return new BigDecimal(Math.sqrt(result.doubleValue()));
    }

    /**
     * 计算莱文斯坦距离
     * @param str1
     * @param str2
     * @return
     */
    public static int getLevenshteinDistance(String str1, String str2) {
        // 列长度
        int len1 = (str1 == null) ? 0 : str1.length();
        // 行长度
        int len2 = (str2 == null) ? 0 : str2.length();
        if (len1 == 0) {
            return len2;
        }
        if (len2 == 0) {
            return len1;
        }

        // 建立一个表格，长宽各+1
        int[][] dif = new int[len1 + 1][len2 + 1];
        // 给第一行赋值
        for (int a = 0; a <= len2; a++) {
            dif[0][a] = a;
        }
        // 给第一列赋值
        for (int a = 0; a <= len1; a++) {
            dif[a][0] = a;
        }

        // 计算两个字符是否一样，计算左上的值
        int temp;
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                // 计算中间某格子上、左比较结果，相同取1，不同取0
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    temp = 0;
                } else {
                    temp = 1;
                }
                // 比较该格子和其上、左、左上格子的值大小，取三个值中最小值（其中左上值+0/1，上、左格子值+1）
                dif[i][j] = min(dif[i - 1][j - 1] + temp, dif[i][j - 1] + 1, dif[i - 1][j] + 1);
            }
        }

        // 取数组右下角的值，同样不同位置代表不同字符串的比较
        return dif[len1][len2];
    }

    /**
     * 取一组数中的最小值
     * @param is
     * @return
     */
    private static int min(int... is) {
        int min = Integer.MAX_VALUE;
        for (int i : is) {
            if (min > i) {
                min = i;
            }
        }
        return min;
    }

}