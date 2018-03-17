package utils.initializers;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import utils.InstanceReflectionUtil;
import utils.initializers.RandomInitializer;

public class MapInitializer extends RandomInitializer {
    @Override
    public boolean canProvideValueFor(Class<?> type, Type genericType) {
        return Map.class.isAssignableFrom(type);
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, InstanceReflectionUtil.Traverser traverser) {
        Map resultMap = instantiateMap(type);

        Type keyType = getKeyValueType(genericType, 0);
        Type valueType = getKeyValueType(genericType, 1);

        for(int i = 0; i < InstanceReflectionUtil.MAX_ITEMS_TO_CREATE_IN_COLLECTIONS; i++) {
            Object key = getInitializers().generateValue(keyType, traverser);
            Object value = getInitializers().generateValue(valueType, traverser);
            resultMap.put(key, value);
        }

        return resultMap;
    }

    private Type getKeyValueType(Type genericType, int index) {
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;

            return parameterizedType.getActualTypeArguments()[index];
        } else {
            throw new RuntimeException("Unknown type of instances to be created.");
        }
    }

    private Map instantiateMap(Class<?> type) {
        int modifiers = type.getModifiers();
        boolean interfaceOrAbstractClass = type.isInterface() || Modifier.isAbstract(modifiers);
        if (interfaceOrAbstractClass) {
            //TODO MM: allow to specify which maps are created.
            //noinspection unchecked
            HashMap hashMap = new HashMap();
            return hashMap;
        } else {
            try {
                Map result = (Map) type.newInstance();
                //noinspection uncheckbed
                return result;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
