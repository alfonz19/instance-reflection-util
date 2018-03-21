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
            ModifiableFieldTraverserNode modifiableNode = new ModifiableFieldTraverserNode(field, instance);

            traversingProcessor.process(modifiableNode, context.subNode(modifiableNode.getTraverserNode()));
        }
    }

    private static class FieldTraverserNode implements TraverserNode {

        private final Object nodeValue;
        private final Type genericType;
        private final Class<?> type;

        public FieldTraverserNode(ModifiableFieldTraverserNode node) {
            Field field = node.getField();
            Object instance = node.getInstance();

            try {
                nodeValue = field.get(instance);
                genericType = field.getGenericType();
                type = field.getType();
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }

        @Override
        public Object getValue() {
            return nodeValue;
        }

        @Override
        public Type getGenericType() {
            return genericType;
        }

        @Override
        public Class<?> getType() {
            return type;
        }
    }

    private static class ModifiableFieldTraverserNode implements ModifiableTraverserNode {
        private final Field field;
        private final Object instance;
        private final FieldTraverserNode traverserNode;

        private ModifiableFieldTraverserNode(Field field, Object instance) {
            this.field = field;
            this.instance = instance;
            traverserNode = new FieldTraverserNode(this);
        }

        @Override
        public void setValue(Object value) {
            try {
                field.set(instance, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Field getField() {
            return field;
        }

        public Object getInstance() {
            return instance;
        }

        //-- delegated


        @Override
        public Object getValue() {
            return traverserNode.getValue();
        }

        @Override
        public Type getGenericType() {
            return traverserNode.getGenericType();
        }

        @Override
        public Class<?> getType() {
            return traverserNode.getType();
        }

        public FieldTraverserNode getTraverserNode() {
            return traverserNode;
        }
    }
}
