package utils.initializers;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import utils.GenericTypeUtil;

public class CollectionOrIterableInitializer extends ArrayLikeInitializerParent {

    @Override
    public boolean canProvideValueFor(Class<?> type, Type genericType) {
        return Collection.class.isAssignableFrom(type) || Iterable.class.isAssignableFrom(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object instantiateCollection(Class<?> type, Type typeOfElements, List items) {

        //collection will be either list or set.
        boolean useList = random.nextBoolean();
        if (useList) {
            return new ArrayList(items);
        } else {
            return new HashSet<>(items);
        }
    }

    @Override
    protected Type getTypeOfElements(Type genericType) {
        return GenericTypeUtil.typeOfListSetElements(genericType);
    }
}
