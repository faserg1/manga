package com.danilov.manga.core.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Semyon Danilov on 17.05.2014.
 */
public class ServiceContainer {

    private static Map<Class, Object> services = new HashMap<Class, Object>();

    public static <T> T getService(final Class<T> clazz) {
        return (T) services.get(clazz);
    }

    public static void addService(final Object object) {
        services.put(object.getClass(), object);
    }


}
