package org.evrete.guides.showcase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;

public final class ShowcaseUtils {

    public static String toJson(Object object) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return new Gson().fromJson(json, type);
    }

    public static String readResourceAsString(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            return StreamUtils.copyToString(
                    resource.getInputStream(), Charset.defaultCharset());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
