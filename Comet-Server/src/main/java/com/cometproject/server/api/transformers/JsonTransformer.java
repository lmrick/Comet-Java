package com.cometproject.server.api.transformers;

import com.cometproject.api.utilities.JsonUtil;
import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer {
    
    @Override
    public String render(Object o) {
        try {
            String gsonString = JsonUtil.getInstance().toJson(o);

            if (!gsonString.startsWith("{")) {
                return "{\"response\":" + gsonString + "}";
            } else {
                return gsonString;
            }
        } catch (Exception e) {
            return JsonUtil.getInstance().toJson(e);
        } finally {
            o = null;
        }
    }

}
