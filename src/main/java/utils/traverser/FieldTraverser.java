package utils.traverser;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class FieldTraverser implements ClassTreeTraverser {
    private final TraversingProcessor traversingProcessor;

    public FieldTraverser(TraversingProcessor traversingProcessor) {
        this.traversingProcessor = traversingProcessor;
    }

    @Override
    public <T> T process(T instance) {
        return process(instance, instance.getClass());
    }

    @Override
    public <T> T process(T instance, ClassTreeTraverserContext context) {
        return process(instance, instance.getClass(), context);
    }

    @Override
    public <T> T process(T instance, Class<?> startClass) {
        return process(instance, startClass, new ClassTreeTraverserContext(this));
    }

    @Override
    public <T> T process(T instance, Class<?> startClass, ClassTreeTraverserContext context) {
        if (!startClass.isAssignableFrom(instance.getClass())) {
            throw new IllegalArgumentException();
        }

        Class<?> instanceClass = startClass;
        Class<Object> stopClazz = Object.class;

        do {
            processFieldsInCurrentClass(instance, instanceClass, context);
            instanceClass = instanceClass.getSuperclass();
        } while (!instanceClass.isAssignableFrom(stopClazz));



        return instance;
    }

    private <T> void processFieldsInCurrentClass(T instance,
                                                 Class<?> instanceClass,
                                                 ClassTreeTraverserContext context) {
        Field[] fields = instanceClass.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            FieldTraverserNode node = new FieldTraverserNode(field, instance);

            traversingProcessor.process(new ModifiableFieldTraverserNode(node), context.subNode(node));
        }
    }

    private static class FieldTraverserNode implements TraverserNode {
        protected final Field field;
        protected final Object instance;


        public FieldTraverserNode(Field field, Object instance) {
            this.field = field;
            this.instance = instance;
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
        public Type getGenericType() {
            return field.getGenericType();
        }

        @Override
        public Class<?> getType() {
            return field.getType();
        }
    }

    private static class ModifiableFieldTraverserNode extends FieldTraverserNode implements ModifiableTraverserNode {
        public ModifiableFieldTraverserNode(Field field, Object instance) {
            super(field, instance);
        }

        public ModifiableFieldTraverserNode(FieldTraverserNode node) {
            this(node.field, node.instance);
        }

        @Override
        public void setValue(Object value) {
            try {
                field.set(instance, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
