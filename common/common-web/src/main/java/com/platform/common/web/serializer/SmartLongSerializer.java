package com.platform.common.web.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Long 智能序列化器
 * 超过 JS 安全整数范围的 Long 输出为字符串，否则输出为数字
 */
public class SmartLongSerializer extends JsonSerializer<Long> {

    /**
     * JavaScript Number.MAX_SAFE_INTEGER = 2^53 - 1
     */
    private static final long JS_MAX_SAFE_INTEGER = 9007199254740991L;

    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else if (value > JS_MAX_SAFE_INTEGER || value < -JS_MAX_SAFE_INTEGER) {
            gen.writeString(value.toString());
        } else {
            gen.writeNumber(value);
        }
    }
}
