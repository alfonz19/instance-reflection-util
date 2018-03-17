package utils.initializers;

import java.lang.reflect.Type;

import utils.InstanceReflectionUtil;

public class StringInitializer extends SimpleInitializer {
    public StringInitializer() {
        super(String.class);
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, InstanceReflectionUtil.Traverser traverser) {
        return "RandomString: " + Long.toString(random.nextLong());
    }
}
