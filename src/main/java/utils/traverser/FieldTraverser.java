package utils.traverser;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import utils.InstanceReflectionUtil;

public class FieldTraverser implements ClassTreeTraverser {
    private final InstanceReflectionUtil.Processor processor;

    public FieldTraverser(InstanceReflectionUtil.Processor processor) {
        this.processor = processor;
    }

    @Override
    public <T> T process(T instance) {
        return process(instance, instance.getClass());
    }

    @Override
    public <T> T process(T instance, Class<?> startClass) {
        if (!startClass.isAssignableFrom(instance.getClass())) {
            throw new IllegalArgumentException();
        }

        Class<?> instanceClass = startClass;
        Class<Object> stopClazz = Object.class;

        do {
            processFieldsInCurrentClass(instance, instanceClass);
            instanceClass = instanceClass.getSuperclass();
        } while (!instanceClass.isAssignableFrom(stopClazz));



        return instance;
    }

    private <T> void processFieldsInCurrentClass(T instance, Class<?> instanceClass) {
        Field[] fields = instanceClass.getDeclaredFields();
        FieldTraverserNode node = new FieldTraverserNode();

        for (Field field : fields) {
            field.setAccessible(true);
            node.setContext(field, instance, this);

            processor.process(node);
        }
    }

    public static class FieldTraverserNode implements TraverserNode {
        private Field field;
        private Object instance;
        private FieldTraverser fieldTraverser;

        public <T> void setContext(Field field,
                                   Object instance,
                                   FieldTraverser fieldTraverser) {
            this.field = field;
            this.instance = instance;
            this.fieldTraverser = fieldTraverser;
        }

        @Override
        public Object getValue() {
            try {
                return field.get(instance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void setValue(Object value) {
            try {
                field.set(instance, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Type getGenericType() {
            return field.getGenericType();
        }

        @Override
        public Class<?> getType() {
            return field.getType();
        }

        @Override
        public FieldTraverser getTraverser() {
            return fieldTraverser;
        }
    }
}
