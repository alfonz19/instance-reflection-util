package utils.initializers;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import utils.GenericTypeUtil;
import utils.traverser.PathNode;

public class ListInitializer extends ArrayLikeInitializerParent {
    @Override
    public boolean canProvideValueFor(Class<?> type, Type genericType, PathNode pathNode) {
        return List.class.isAssignableFrom(type);
    }

    @Override
    protected Object instantiateCollection(Class<?> type, Type typeOfElements, List items) {
        int modifiers = type.getModifiers();
        boolean interfaceOrAbstractClass = type.isInterface() || Modifier.isAbstract(modifiers);
        if (interfaceOrAbstractClass) {
            //TODO MM: allow to specify which lists are created.
            //noinspection unchecked
            return new ArrayList(items);
        } else {
            try {
                List result = (List) type.newInstance();
                //noinspection unchecked
                result.addAll(items);
                return result;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected Type getTypeOfElements(Type genericType) {
        return GenericTypeUtil.typeOfListSetElements(genericType);
    }
}
