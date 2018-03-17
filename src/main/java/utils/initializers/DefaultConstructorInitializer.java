package utils.initializers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

import utils.InstanceReflectionUtil;

public class DefaultConstructorInitializer extends InitializerParent {

    @Override
    public boolean canProvideValueFor(Class<?> type, Type genericType) {
        try {
            type.getConstructor();
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, InstanceReflectionUtil.Traverser traverser) {
        try {
            Constructor<?> publicNoArgConstructor = type.getConstructor();
            Object instance = publicNoArgConstructor.newInstance();
            return traverser.process(instance);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
