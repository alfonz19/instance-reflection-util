package utils.initializers;

import java.lang.reflect.Type;
import java.util.UUID;

import utils.InstanceReflectionUtil;

public class UuidInitializer extends SimpleInitializer {
    public UuidInitializer() {
        super(UUID.class);
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, InstanceReflectionUtil.Traverser traverser) {
        return UUID.randomUUID();
    }
}
