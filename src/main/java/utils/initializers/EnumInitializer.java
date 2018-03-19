package utils.initializers;

import java.lang.reflect.Type;

import utils.GenericTypeUtil;
import utils.traverser.ClassTreeTraverserContext;

public class EnumInitializer extends RandomInitializer {
    @Override
    public boolean canProvideValueFor(Type genericType) {
        return GenericTypeUtil.isClassType(genericType)
            && GenericTypeUtil.getClassType(genericType).isEnum();
    }

    @Override
    public Object getValue(Type genericType, ClassTreeTraverserContext context) {
        Class<?> type = GenericTypeUtil.getClassType(genericType);
        Object[] values = type.getEnumConstants();
        return values[random.nextInt(values.length)];
    }
}
