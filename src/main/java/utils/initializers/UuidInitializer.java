package utils.initializers;

import java.lang.reflect.Type;
import java.util.UUID;

import utils.traverser.ClassTreeTraverserContext;
import utils.traverser.PathNode;

public class UuidInitializer extends SimpleInitializer {
    public UuidInitializer() {
        super(UUID.class);
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, PathNode pathNode, ClassTreeTraverserContext context) {
        return UUID.randomUUID();
    }
}
