/**
 * Project Name: elasticsearch-similarity-plugin
 * File Name: SimilarityPlugin
 * Date: 2020/4/13 14:32
 */
package org.elasticsearch.plugin;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.ScriptPlugin;
import org.elasticsearch.script.SimilarityEngine;
import org.elasticsearch.script.ScriptContext;
import org.elasticsearch.script.ScriptEngine;

import java.util.Collection;

/**
 * ElasticSearch Similarity Calculation Plugin
 * @Author liutwv
 * @Date 2020/4/13 14:32
 */
public class SimilarityPlugin extends Plugin implements ScriptPlugin {

    @Override
    public ScriptEngine getScriptEngine(Settings settings, Collection<ScriptContext<?>> contexts) {
        return new SimilarityEngine();
    }

}