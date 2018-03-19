package utils.initializers;

import java.lang.reflect.Type;
import java.util.UUID;

import utils.traverser.ClassTreeTraverserContext;

public class UuidInitializer extends SimpleInitializer {
    public UuidInitializer() {
        super(UUID.class);
    }

    @Override
    public Object getValue(Type genericType, ClassTreeTraverserContext context) {
        return UUID.randomUUID();
    }
}
