package utils.initializers;

import java.lang.reflect.Type;

import utils.traverser.ClassTreeTraverserContext;

public class EnumInitializer extends RandomInitializer {
    @Override
    public boolean canProvideValueFor(Class<?> type, Type genericType) {
        return type.isEnum();
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, ClassTreeTraverserContext context) {
        Object[] values = type.getEnumConstants();
        return values[random.nextInt(values.length)];
    }
}
