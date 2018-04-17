package utils.initializers;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utils.GenericTypeUtil;
import utils.InstanceReflectionUtilException;
import utils.traverser.PathNode;

public class SetInitializer extends ArrayLikeInitializerParent {

    @Override
    public boolean canProvideValueFor(Class<?> type, Type genericType, PathNode pathNode) {
        return Set.class.isAssignableFrom(type);
    }

    @Override
    protected Object instantiateCollection(Class<?> type, Type typeOfElements, List items) {
        int modifiers = type.getModifiers();
        boolean interfaceOrAbstractClass = type.isInterface() || Modifier.isAbstract(modifiers);
        if (interfaceOrAbstractClass) {
            //TODO MM: allow to specify which set are created.
            //noinspection unchecked
            return new HashSet(items);
        } else {
            try {
                Set result = (Set) type.newInstance();
                //noinspection unchecked
                result.addAll(items);
                return result;
            } catch (Exception e) {
                throw new InstanceReflectionUtilException(e);
            }
        }
    }

    @Override
    protected Type getTypeOfElements(Type genericType) {
        return GenericTypeUtil.typeOfListSetElements(genericType);
    }
}
