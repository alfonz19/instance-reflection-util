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
        } while (instanceClass != null && !instanceClass.isAssignableFrom(stopClazz));



        return instance;
    }

    private <T> void processFieldsInCurrentClass(T instance,
                                                 Class<?> instanceClass,
                                                 ClassTreeTraverserContext context) {
        Field[] fields = instanceClass.getDeclaredFields();

        for (Field field : fields) {
            if (field.getName().equals("this$0")) {
                continue;
            }

            field.setAccessible(true);
            TraverserNode traverserNode = createTraverserNode(instance, field);
            ModifiableTraverserNode modifiableNode = new ModifiableFieldTraverserNode(instance, field, traverserNode);

            traversingProcessor.process(modifiableNode, context.subNode(traverserNode));
        }
    }

    private <T> FieldTraverserNode createTraverserNode(T instance, Field field) {
        try {
            return new FieldTraverserNode(field.get(instance),
                field.getGenericType(),
                field.getType(),
                field.getDeclaringClass(),
                instance.getClass(),
                field.getName());
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private static class FieldTraverserNode implements TraverserNode {

        private final Object nodeValue;
        private final Type genericType;
        private final Class<?> type;
        private final Class<?> declaringClassOfNode;
        private final Class<?> instanceClass;
        private final String fieldName;

        private FieldTraverserNode(Object nodeValue,
                                   Type genericType,
                                   Class<?> type,
                                   Class<?> declaringClassOfNode,
                                   Class<?> instanceClass,
                                   String fieldName) {
            this.nodeValue = nodeValue;
            this.genericType = genericType;
            this.type = type;
            this.declaringClassOfNode = declaringClassOfNode;
            this.instanceClass = instanceClass;
            this.fieldName = fieldName;
        }

        @Override
        public String getNodeName() {
            return fieldName;
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

        @Override
        public Class<?> getDeclaringClass() {
            return declaringClassOfNode;
        }

        @Override
        public Class<?> getInstanceClass() {
            return instanceClass;
        }

        @Override
        public String toString() {
            return "FieldTraverserNode{" +
                "declaringClassOfNode=" + declaringClassOfNode +
                ", genericType=" + genericType +
                ", instanceClass=" + instanceClass +
                '}';
        }
    }

    private static class ModifiableFieldTraverserNode implements ModifiableTraverserNode {
        private final Field field;
        private final Object instance;
        private final TraverserNode traverserNode;

        private ModifiableFieldTraverserNode(Object instance,
                                             Field field,
                                             TraverserNode traverserNode) {
            this.field = field;
            this.instance = instance;
            this.traverserNode = traverserNode;
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
        public TraverserNode getTraverserNode() {
            return traverserNode;
        }
    }
}
