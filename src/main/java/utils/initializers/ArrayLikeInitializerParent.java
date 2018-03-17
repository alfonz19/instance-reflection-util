package utils.initializers;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import utils.GenericTypeUtil;
import utils.traverser.ClassTreeTraverser;

public abstract class ArrayLikeInitializerParent extends RandomInitializer {

    protected static final int MAX_ITEMS_TO_CREATE_IN_COLLECTIONS = 5;

    private List createItemsForCollection(Type typeOfListElements, ClassTreeTraverser traverser) {
        //TODO MM: allow specification number of items. Globally 0/1..N, locally. Allow null for whole container? Allow null internal values?
        int itemCount = 1 + random.nextInt(MAX_ITEMS_TO_CREATE_IN_COLLECTIONS);

        List result = new ArrayList(itemCount);
        for (int i = 0; i < itemCount; i++) {
            try {
                Initializer initializer = this.getInitializers().getSoleInitializer(GenericTypeUtil.getClassType(typeOfListElements), typeOfListElements);

                Object newInstance = initializer.getValue(GenericTypeUtil.getClassType(typeOfListElements), typeOfListElements, traverser);

                //noinspection unchecked
                result.add(newInstance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return result;
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, ClassTreeTraverser traverser) {
        //TODO MM: allow to specify subclasses to be instantiated as well.
        Type typeOfElements = getTypeOfElements(genericType);

        List listItems = createItemsForCollection(typeOfElements, traverser);
        return instantiateCollection(type, typeOfElements, listItems);
    }

    protected abstract Type getTypeOfElements(Type genericType);

    protected abstract Object instantiateCollection(Class<?> type,
                                                    Type typeOfElements,
                                                    List items);
}
