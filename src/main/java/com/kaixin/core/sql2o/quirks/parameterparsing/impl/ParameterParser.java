package com.kaixin.core.sql2o.quirks.parameterparsing.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lars on 22.09.2014.
 */
public class ParameterParser implements CharParser{

    private final Map<String, List<Integer>> parameterMap;
    int paramIdx = 1;

    public ParameterParser(Map<String, List<Integer>> parameterMap) {
        this.parameterMap = parameterMap;
    }

    @Override
    public boolean canParse(char c, String sql, int idx) {
        return (sql.length() > idx + 1 && c == ':' && Character.isJavaIdentifierStart( sql.charAt(idx + 1) ) && sql.charAt(idx-1) != ':')
                || c == '?';     //zhongshu:增加positioned parameter支持
    }

    @Override
    public int parse(char c, int idx, StringBuilder parsedSql, String sql, int length) {
        int startIdx = idx;
        if (c == '?') {
            //zhongshu:增加positioned parameter支持
            int total = parameterMap.size();
            List<Integer> indices = new ArrayList<>();
            parameterMap.put("p" + total, indices);
            indices.add(paramIdx++);
            parsedSql.append("?");
        }
        else {
            idx += 1;

            while (idx + 1 < length && Character.isJavaIdentifierPart(sql.charAt(idx + 1))) {
                idx += 1;
            }

            String name = sql.substring(startIdx + 1, idx + 1);
            List<Integer> indices = parameterMap.get(name);
            if (indices == null) {
                indices = new ArrayList<>();
                parameterMap.put(name, indices);
            }
            indices.add(paramIdx++);
            parsedSql.append("?");
        }
        return idx;
    }
}
