package utils.initializers;

import java.lang.reflect.Type;

import utils.traverser.ClassTreeTraverserContext;

public class StringInitializer extends SimpleInitializer {
    public StringInitializer() {
        super(String.class);
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, ClassTreeTraverserContext context) {
        return "RandomString: " + Long.toString(random.nextLong());
    }
}
