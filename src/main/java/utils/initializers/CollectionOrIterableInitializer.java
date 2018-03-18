package utils.initializers;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import utils.traverser.ClassTreeTraverserContext;

public class CollectionOrIterableInitializer extends RandomInitializer {
    @Override
    public boolean canProvideValueFor(Class<?> type, Type genericType) {
        return Collection.class.isAssignableFrom(type) || Iterable.class.isAssignableFrom(type);
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, ClassTreeTraverserContext context) {
        List<Class<? extends Collection>> delegateTo = Arrays.asList(List.class, Set.class);
        Class<? extends Collection> delegateClass = delegateTo.get(random.nextInt(delegateTo.size()));
        return getInitializers().getSoleInitializer(delegateClass, genericType)
                .getValue(delegateClass, genericType, context);//TODO MM: error, you should fix genericType as well.
    }
}
