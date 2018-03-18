package utils.initializers;

import java.lang.reflect.Type;

import utils.traverser.ClassTreeTraverserContext;

public class IntInitializer extends SimpleInitializer {
    public IntInitializer() {
        super(Integer.TYPE, Integer.class);
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, ClassTreeTraverserContext context) {
        return random.nextInt();
    }
}
