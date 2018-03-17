package utils.initializers;

import java.lang.reflect.Type;

import utils.traverser.ClassTreeTraverser;

public class IntInitializer extends SimpleInitializer {
    public IntInitializer() {
        super(Integer.TYPE, Integer.class);
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, ClassTreeTraverser traverser) {
        return random.nextInt();
    }
}
