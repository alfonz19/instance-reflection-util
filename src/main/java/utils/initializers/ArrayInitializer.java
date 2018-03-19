package utils.initializers;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.List;

import utils.GenericTypeUtil;

public class ArrayInitializer extends ArrayLikeInitializerParent {

    @Override
    public boolean canProvideValueFor(Type genericType) {
        return GenericTypeUtil.getClassType(genericType).isArray();
    }

    @Override
    protected Object instantiateCollection(Class<?> classType, Type typeOfElements, List items) {
        Class<?> componentType = GenericTypeUtil.getClassType(typeOfElements);

        Object newArray = Array.newInstance(componentType, items.size());
        for(int i = 0; i < items.size(); i++) {
            Array.set(newArray, i, items.get(i));
        }
        return newArray;
    }

    @Override
    protected Type getTypeOfElements(Type genericType) {
        return GenericTypeUtil.getTypeOfArrayElements(genericType);
    }
}
