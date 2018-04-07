package utils.initializers;

import java.lang.reflect.Type;

import utils.traverser.ClassTreeTraverserContext;
import utils.traverser.PathNode;

public class IntInitializer extends SimpleInitializer {
    public IntInitializer() {
        super(Integer.TYPE, Integer.class);
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, PathNode pathNode, ClassTreeTraverserContext context) {
        return random.nextInt();
    }
}
