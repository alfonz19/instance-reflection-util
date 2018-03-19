package utils.initializers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Optional;

import utils.GenericTypeUtil;
import utils.traverser.ClassTreeTraverserContext;

public class DefaultConstructorInitializer extends InitializerParent {

    @Override
    public boolean canProvideValueFor(Type genericType) {
        return GenericTypeUtil.isClassType(genericType)
            && getDefaultConstructor(genericType).isPresent();
    }

    @Override
    public Object getValue(Type genericType, ClassTreeTraverserContext context) {
        try {
            Constructor<?> publicNoArgConstructor = getDefaultConstructor(genericType).orElseThrow(()->new IllegalStateException("weird state, either canProvideValueFor should return false, or this should be able to find constructor."));
            Object instance = publicNoArgConstructor.newInstance();
            return context.processCurrentNodeInstance(instance);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<Constructor<?>> getDefaultConstructor(Type genericType)  {
        Class<?> type = GenericTypeUtil.getClassType(genericType);
        try {
            return Optional.of(type.getConstructor());
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }
}
