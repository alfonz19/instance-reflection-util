package utils.initializers;

import java.lang.reflect.Type;

import utils.InstanceReflectionUtil;

public class IntInitializer extends SimpleInitializer {
    public IntInitializer() {
        super(Integer.TYPE, Integer.class);
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, InstanceReflectionUtil.Traverser traverser) {
        return random.nextInt();
    }
}
