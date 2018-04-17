package utils.initializers;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import utils.GenericTypeUtil;
import utils.InstanceReflectionUtilException;
import utils.traverser.ClassTreeTraverserContext;
import utils.traverser.PathNode;

public abstract class ArrayLikeInitializerParent extends RandomInitializer {

    protected static final int MAX_ITEMS_TO_CREATE_IN_COLLECTIONS = 5;

    private List createItemsForCollection(Type typeOfListElements,
                                          PathNode pathNode,
                                          ClassTreeTraverserContext context) {
        //TODO MM: allow specification number of items. Globally 0/1..N, locally. Allow null for whole container? Allow null internal values?
        int itemCount = 1 + random.nextInt(MAX_ITEMS_TO_CREATE_IN_COLLECTIONS);

        List result = new ArrayList(itemCount);
        for (int i = 0; i < itemCount; i++) {
            try {
                Class<?> type = GenericTypeUtil.getClassType(typeOfListElements);
                Object newInstance = this.getInitializers().generateValue(type, typeOfListElements, pathNode, context);//TODO MM: fix here index of item in array/collection.

                //noinspection unchecked
                result.add(newInstance);
            } catch (Exception e) {
                throw new InstanceReflectionUtilException(e);
            }
        }

        return result;
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, PathNode pathNode, ClassTreeTraverserContext context) {
        //TODO MM: allow to specify subclasses to be instantiated as well.
        Type typeOfElements = getTypeOfElements(genericType);

        List listItems = createItemsForCollection(typeOfElements, pathNode, context);
        return instantiateCollection(type, typeOfElements, listItems);
    }

    protected abstract Type getTypeOfElements(Type genericType);

    protected abstract Object instantiateCollection(Class<?> type, Type typeOfElements, List items);
}
