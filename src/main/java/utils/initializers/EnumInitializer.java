package utils.initializers;

import java.lang.reflect.Type;

import utils.traverser.ClassTreeTraverserContext;
import utils.traverser.PathNode;

public class EnumInitializer extends RandomInitializer {
    @Override
    public boolean canProvideValueFor(Class<?> type, Type genericType, PathNode pathNode) {
        return type.isEnum();
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, PathNode pathNode, ClassTreeTraverserContext context) {
        Object[] values = type.getEnumConstants();
        return values[random.nextInt(values.length)];
    }
}
