/**
 * Project Name: elasticsearch-similarity-plugin
 * File Name: SimilarityEngine
 * Date: 2020/4/13 14:54
 */
package org.elasticsearch.script;

import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.utils.SimilarityUtils;

import java.util.*;

/**
 * A script language implementation.
 *
 * @Author liutwv
 * @Date 2020/4/13 14:54
 */
public class SimilarityEngine implements ScriptEngine {

    private static final String SCRIPT_NAME = "wv_similarity";
    // 因考虑到图片可能旋转，尽量多考虑几种情况进行比较。但切图这里就没办法了
    // 按顺序依次为：向左转90度、180度、270度、水平翻转、上下翻转
    private static final String[] ANGLE_SUFFIX_ARRAY = {"x", "y", "z", "h", "v"};

    @Override
    public String getType() {
        return SCRIPT_NAME;
    }

    @Override
    public <FactoryType> FactoryType compile(String name, String code, ScriptContext<FactoryType> context, Map<String, String> params) {
        if (context.equals(ScoreScript.CONTEXT) == false) {
            throw new IllegalArgumentException(getType() + " scripts cannot be used for context [" + context.name + "]");
        }

        ScoreScript.Factory factory = (p, lookup) -> new ScoreScript.LeafFactory() {
            // 是否比较多列的值，取其中最大值
            final boolean multi = p.containsKey("multi") ? p.get("multi").toString().equals("true") : false;
            // 列名
            final String field = p.containsKey("field") ? (String) p.get("field") : null;
            // 比较的值
            final String term = p.containsKey("term") ? p.get("term").toString() : null;

            @Override
            public ScoreScript newInstance(LeafReaderContext context) {
                return new ScoreScript(p, lookup, context) {
                    @Override
                    public double execute(ExplanationHolder explanation) {
                        Object fieldValue = lookup.source().get(field);
                        double similarity = calculateSimilarity(fieldValue);

                        // 如果取多列的值，则取其中最大值
                        if (multi) {
                            for (String angleSuffix : ANGLE_SUFFIX_ARRAY) {
                                Object fieldValueAngle = lookup.source().get(field + angleSuffix);
                                double angleV = calculateSimilarity(fieldValueAngle);
                                // 比较各个相似度值，取最大一个
                                similarity = angleV > similarity ? angleV : similarity;
                            }
                        }

                        return similarity;
                    }

                    private double calculateSimilarity(Object fieldValue) {
                        double similarity = 0d;
                        switch (code) {
                            case "hamming":
                                // 汉明距离
                                similarity = SimilarityUtils.getHammingSimilarity(fieldValue.toString(), term);
                                break;
                            case "cos":
                                // 余弦距离
                                similarity = SimilarityUtils.getCosSimilarityFromZeroToOne(fieldValue.toString(), term);
                                break;
                            case "hist_cos":
                                // 直方图
                                similarity = SimilarityUtils.getCdHistCosSimilarity(fieldValue.toString(), term);
                                break;
                            case "euclidean":
                                // 欧几里得距离
                                similarity = SimilarityUtils.getEuclideanSimilarity(fieldValue.toString(), term);
                                break;
                            case "levenshtein":
                                // 莱文斯坦比
                                if (fieldValue instanceof ArrayList) {
                                    List<String> list = (ArrayList<String>) fieldValue;
                                    if (list.size() == 0) {
                                        break;
                                    }
                                    double total = 0D;
                                    for (String s : list) {
                                        total = Math.max(total, SimilarityUtils.getLevenshteinSimilarity(term, s));
                                    }
                                    similarity = total;
                                } else if (fieldValue instanceof String) {
                                    similarity = SimilarityUtils.getLevenshteinSimilarity(term, fieldValue.toString());
                                }
                                break;
                            default:
                                break;
                        }
                        return similarity;
                    }
                };
            }

            @Override
            public boolean needs_score() {
                return false;
            }
        };
        return context.factoryClazz.cast(factory);

    }

    @Override
    public Set<ScriptContext<?>> getSupportedContexts() {
        return Collections.singleton(SimilarityScript.CONTEXT);
    }

}