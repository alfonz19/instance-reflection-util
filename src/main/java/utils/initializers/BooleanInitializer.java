package utils.initializers;

import java.lang.reflect.Type;

import utils.traverser.ClassTreeTraverserContext;

public class BooleanInitializer extends SimpleInitializer {
    public BooleanInitializer() {
        super(Boolean.TYPE, Boolean.class);
    }

    @Override
    public Object getValue(Type genericType, ClassTreeTraverserContext context) {
        return random.nextBoolean();
    }
}
