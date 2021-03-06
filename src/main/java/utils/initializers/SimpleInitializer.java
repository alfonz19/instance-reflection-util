package utils.initializers;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public abstract class SimpleInitializer extends RandomInitializer {

    private final List<Class<?>> classes;

    protected SimpleInitializer(Class<?> ... classes) {
        this(Arrays.asList(classes));
    }

    protected SimpleInitializer(List<Class<?>> classes) {
        this.classes = classes;
    }

    @Override
    public boolean canProvideValueFor(Class<?> type, Type genericType) {
        return classes.contains(type);
    }
}
