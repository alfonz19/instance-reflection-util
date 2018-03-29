package utils.initializers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

import utils.traverser.ClassTreeTraverserContext;

public class DefaultConstructorInitializer extends InitializerParent {

    @Override
    public boolean canProvideValueFor(Class<?> type, Type genericType) {
        try {
            if (Object.class.equals(type)) {
                return false;
            }
            type.getConstructor();
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, ClassTreeTraverserContext context) {
        try {
            Constructor<?> publicNoArgConstructor = type.getConstructor();
            Object instance = publicNoArgConstructor.newInstance();
            return context.processCurrentNodeInstance(instance);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
