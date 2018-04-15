package utils.traverser;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class ReflectUtil {

    public static List<Type> getActualArgumentsList(Type genericSuperclass) {
        return Arrays.asList(asParameterizedType(genericSuperclass).getActualTypeArguments());
    }

    public static ParameterizedType asParameterizedType(Type genericSuperclass) {
        return (ParameterizedType) genericSuperclass;
    }

    public static boolean isParameterizedType(Type genericSuperclass) {
        return genericSuperclass instanceof ParameterizedType;
    }
}
