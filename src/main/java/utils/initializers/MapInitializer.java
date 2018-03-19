package utils.initializers;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import utils.GenericTypeUtil;
import utils.traverser.ClassTreeTraverserContext;

public class MapInitializer extends RandomInitializer {
    @Override
    public boolean canProvideValueFor(Type genericType) {
        return Map.class.isAssignableFrom(GenericTypeUtil.getClassType(genericType));
    }

    @Override
    public Object getValue(Type genericType, ClassTreeTraverserContext context) {
        Map resultMap = instantiateMap(genericType);

        Type keyType = getKeyValueType(genericType, 0);
        Type valueType = getKeyValueType(genericType, 1);

        for(int i = 0; i < ArrayLikeInitializerParent.MAX_ITEMS_TO_CREATE_IN_COLLECTIONS; i++) {
            Object key = getInitializers().generateValue(keyType, context);
            Object value = getInitializers().generateValue(valueType, context);
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

    private Map instantiateMap(Type genericType) {
        Class<?> type = GenericTypeUtil.getClassType(genericType);
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
