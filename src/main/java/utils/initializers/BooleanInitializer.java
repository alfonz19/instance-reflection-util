package utils.initializers;

import java.lang.reflect.Type;

import utils.InstanceReflectionUtil;

public class BooleanInitializer extends SimpleInitializer {
    public BooleanInitializer() {
        super(Boolean.TYPE, Boolean.class);
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, InstanceReflectionUtil.Traverser traverser) {
        return random.nextBoolean();
    }
}
