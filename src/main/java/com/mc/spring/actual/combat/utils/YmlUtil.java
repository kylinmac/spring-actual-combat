package com.mc.spring.actual.combat.utils;

import com.google.common.collect.Maps;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * @author macheng
 * @date 2022/3/7 18:28
 */
public class YmlUtil {
    private static Map<String, Object> ymlMap = null;

    static {
        ymlMap = Maps.newConcurrentMap();
        getApplicationYml();
    }

    /**
     *
     */
    public YmlUtil() {
        super();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getApplicationYml() {
        try {
            Yaml yaml = new Yaml();
            InputStream resourceAsStream = YmlUtil.class.getResourceAsStream("/application.yml");
            if (resourceAsStream != null) {
                Map<String, Object> map = yaml.loadAs(resourceAsStream, Map.class);
                switchToMap(null, map);
            }
//                        URL url = ClassLoader.getSystemResource("application.yml");
//            if (url != null) {
//                Map<String, Object> map = yaml.loadAs(url, Map.class);
//                switchToMap(null, map);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ymlMap;
    }

    @SuppressWarnings("unchecked")
    private static void switchToMap(String myKey, Map<String, Object> map) {
        Iterator<String> it = map.keySet().iterator();
        myKey = myKey == null ? "" : myKey;
        String tmpkey = myKey;
        while (it.hasNext()) {
            String key = it.next();
            myKey = tmpkey + key;
            Object value = map.get(key);
            if (value instanceof Map) {
                switchToMap(myKey.concat("."), (Map<String, Object>) value);
            } else {
                if (null != value) {
                    ymlMap.put(myKey, value);
                }
//                System.out.println(myKey+"->"+map.get(key));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) ymlMap.get(key);
    }

    public static String getStr(String key) {
        return String.valueOf(ymlMap.get(key));
    }

}
