package utils.initializers;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.List;

import utils.InstanceReflectionUtil;

public class ArraInitializer extends ArrayLikeInitializerParent {   //TODO MM: rename

    @Override
    public boolean canProvideValueFor(Class<?> type, Type genericType) {
        return type.isArray();
    }

    @Override
    protected Object instantiateCollection(Class<?> classType, Type typeOfElements, List items) {
        Class<?> componentType = InstanceReflectionUtil.GenericType.getClassType(typeOfElements);

        Object newArray = Array.newInstance(componentType, items.size());
        for(int i = 0; i < items.size(); i++) {
            Array.set(newArray, i, items.get(i));
        }
        return newArray;
    }

    @Override
    protected Type getTypeOfElements(Type genericType) {
        return InstanceReflectionUtil.GenericType.getTypeOfArrayElements(genericType);
    }
}
