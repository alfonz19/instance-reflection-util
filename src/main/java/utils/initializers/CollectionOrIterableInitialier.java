package utils.initializers;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import utils.InstanceReflectionUtil;
import utils.initializers.RandomInitializer;

public class CollectionOrIterableInitialier extends RandomInitializer {//TODO MM: rename
    @Override
    public boolean canProvideValueFor(Class<?> type, Type genericType) {
        return Collection.class.isAssignableFrom(type) || Iterable.class.isAssignableFrom(type);
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, InstanceReflectionUtil.Traverser traverser) {
        List<Class<? extends Collection>> delegateTo = Arrays.asList(List.class, Set.class);
        Class<? extends Collection> delegateClass = delegateTo.get(random.nextInt(delegateTo.size()));
        return getInitializers().getSoleInitializer(delegateClass, genericType)
                .getValue(delegateClass, genericType, traverser);
    }
}
