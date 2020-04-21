/**
 * Project Name: elasticsearch-similarity-plugin
 * File Name: SimilarityUtils
 * Date: 2020/4/13 22:05
 */
package org.elasticsearch.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;

/**
 * 相似性计算工具类
 * @Author liutwv
 * @Date 2020/4/13 22:05
 */
public class SimilarityUtils {

    // 小数点后保留几位小数
    private static final Integer SCALE = 6;

    /**
     * 计算汉明相似度
     * @param str1
     * @param str2
     * @return
     */
    public static double getHammingSimilarity(String str1, String str2) {
        int size = str1.length();
        int distance = DistanceUtils.hammingDistance(str1, str2);
        return new BigDecimal(1).subtract(
                new BigDecimal(distance).divide(new BigDecimal(size), SCALE, BigDecimal.ROUND_HALF_UP)).doubleValue();
    }

    /**
     * 计算余弦相似性
     * @param str1
     * @param str2
     * @return
     */
    public static double getCosSimilarityFromZeroToOne(String str1, String str2) {
        BigDecimal similarity = getCosSimilarityInBigDecimal(str1, str2);
        // 归一化到[0, 1]区间内, 0.5 * cos + 0.5
        return new BigDecimal(0.5).multiply(similarity).add(new BigDecimal(0.5))
                .setScale(SCALE, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 计算余弦值，返回BigDecimal结果
     * @param str1
     * @param str2
     * @return
     */
    public static BigDecimal getCosSimilarityInBigDecimal(String str1, String str2) {
        char[] iArray = str1.toCharArray();
        char[] tArray = str2.toCharArray();
        // 分子
        BigDecimal numerator = BigDecimal.ZERO;
        BigDecimal inputDenominator = BigDecimal.ZERO;
        BigDecimal targetDenominator = BigDecimal.ZERO;
        for (int i = 0; i < iArray.length; i++) {
            BigDecimal inputValue = new BigDecimal(iArray[i]);
            BigDecimal targetValue = new BigDecimal(tArray[i]);
            numerator = numerator.add(inputValue.multiply(targetValue));
            inputDenominator = inputDenominator.add(inputValue.pow(2));
            targetDenominator = targetDenominator.add(targetValue.pow(2));
        }
        inputDenominator = new BigDecimal(Math.sqrt(inputDenominator.doubleValue()));
        targetDenominator = new BigDecimal(Math.sqrt(targetDenominator.doubleValue()));
        if (BigDecimal.ZERO.equals(inputDenominator)
                || BigDecimal.ZERO.equals(targetDenominator)) {
            throw new IllegalArgumentException("Calculate cosine distance error. input or target value contains zero.");
        }
        return numerator.divide(inputDenominator.multiply(targetDenominator), SCALE, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 计算两个直方图JSON数值直接的余弦值
     * @param json1
     * @param json2
     * @return
     */
    public static double getCdHistCosSimilarity(String json1, String json2) {
        if (json1 == null || json1.trim().length() == 0 || json2 == null || json2.trim().length() == 0) {
            return 0d;
        }
        json1 = json1.replaceAll("\"\"", "").replaceAll("\"", "'");
        json2 = json2.replaceAll("\"\"", "").replaceAll("\"", "'");

        int sum1 = 0;
        int sum2 = 0;
        int sumMixd = 0;
        try {
            JSONObject jsonObj1 = JSON.parseObject(json1);
            JSONObject jsonObj2 = JSON.parseObject(json2);

            for (int i = 0; i < 512; i++) {
                int v1 = jsonObj1.getInteger(i + "") == null ? 0 : jsonObj1.getInteger(i + "");
                int v2 = jsonObj2.getInteger(i + "") == null ? 0 : jsonObj2.getInteger(i + "");

                sum1 += Math.pow(v1, 2);
                sum2 += Math.pow(v2, 2);
                sumMixd += v1 * v2;
            }

            return new BigDecimal(sumMixd).divide(new BigDecimal(Math.sqrt(sum1)).multiply(new BigDecimal(Math.sqrt(sum2))), SCALE, BigDecimal.ROUND_HALF_UP).doubleValue();
        } catch (Exception e) {
            System.out.println(e);
            return 0d;
        }
    }

    /**
     * 计算欧几里得相似性
     * @param str1
     * @param str2
     * @return
     */
    public static double getEuclideanSimilarity(String str1, String str2) {
        BigDecimal distance = DistanceUtils.getEuclideanDistanceInBigDecimal(str1, str2);
        // 1 / (1 + distance)
        return new BigDecimal(1).divide(new BigDecimal(1).multiply(distance))
                .setScale(SCALE, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 计算莱文斯坦比相似度
     * @param str1
     * @param str2
     * @return
     */
    public static double getLevenshteinSimilarity(String str1, String str2) {
        int distance = DistanceUtils.getLevenshteinDistance(str1, str2);
        // distance / 高宽的最长长度
        return new BigDecimal(distance).divide(new BigDecimal(Math.max(str1.length(), str2.length())))
                .setScale(SCALE, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

}