package utils.initializers;

import java.lang.reflect.Type;

import utils.traverser.ClassTreeTraverserContext;
import utils.traverser.PathNode;

public class BooleanInitializer extends SimpleInitializer {
    public BooleanInitializer() {
        super(Boolean.TYPE, Boolean.class);
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, PathNode pathNode, ClassTreeTraverserContext context) {
        return random.nextBoolean();
    }
}
