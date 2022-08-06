package xyz.idaoteng.auth.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class JacksonUtil {
    private static volatile ObjectMapper OBJECT_MAPPER;

    private JacksonUtil() {}

    public static ObjectMapper objectMapper() {
        if (OBJECT_MAPPER == null) {
            synchronized (JacksonUtil.class) {
                if (OBJECT_MAPPER == null) {
                    OBJECT_MAPPER = new ObjectMapper();
                    //配置Jackson映射器和映射规则
                    OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).
                            setSerializationInclusion(JsonInclude.Include.NON_NULL).
                            configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false).
                            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).
                            configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true).
                            setTimeZone(TimeZone.getTimeZone("GMT+8")).
                            setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
                }
            }
        }
        return OBJECT_MAPPER;
    }
}
