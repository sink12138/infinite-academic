package com.buaa.academic.tool.translator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;

public class Translator {

    private static final String appid = "20210916000945263";
    private static final String securityKey = "nff5EjfZ5h2pJKdiHcnH";

    private Translator() { }

    /**
     * @apiNote Language: zh, en, ...
     * @param text The text to be translated
     * @param from Source language. Use 'auto' if not specified
     * @param to Target language
     * @return Translated text
     */
    public static String translate(String text, String from, String to) {
        TransApi api = new TransApi(appid, securityKey);
        String transResultResponse = api.getTransResult(text, from, to);
        JSONObject transResultJSON = JSON.parseObject(transResultResponse);
        JSONArray transResultArray = (JSONArray) transResultJSON.get("trans_result");
        @SuppressWarnings("unchecked")
        Map<String, Object> transResultMap = (Map<String, Object>) transResultArray.get(0);
        return transResultMap.get("dst").toString();
    }
}
