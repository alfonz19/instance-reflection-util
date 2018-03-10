package utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


public class InstanceReflectionUtil {

    //<editor-fold desc="SpecificInitializers">
    private static abstract class RandomCollectionInitializerParent extends RandomInitializer {
        protected Type typeOfListElements(TraverserNode traverserNode) {
            Optional<ParameterizedType> parameterizedType = traverserNode.getParameterizedType();

            if (!parameterizedType.isPresent()) {
                //TODO MM: allow to specify global values for not parameterized lists. Also allow to override it per field. Also allow to specify which values should we select from per individual field.
                throw new RuntimeException("Unknown type of instances to be created.");
            }


            //TODO MM: allow to specify subclasses to be instantiated as well.
            //list have just one type argument.
            return parameterizedType.get().getActualTypeArguments()[0];
        }

        protected List createItemsForCollection(Type typeOfListElements, Traverser traverser) {
            //TODO MM: allow specification number of items. Globally 0/1..N, locally. Allow null for whole container? Allow null internal values?
            int itemCount = 1 + random.nextInt(5);

            List result = new ArrayList(itemCount);
            for (int i = 0; i < itemCount; i++) {
                try {
                    //TODO MM: here cannot be simple instantiation, we have to go here through initialized detection again.
                    Object newInstance = ((Class) typeOfListElements).newInstance();

                    traverser.process(newInstance);

                    //noinspection unchecked
                    result.add(newInstance);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            return result;
        }

        @Override
        public Object generateRandomValue(TraverserNode traverserNode) {
            Class<?> classType = traverserNode.getClassType();
            List listItems = createItemsForCollection(typeOfListElements(traverserNode), traverserNode.getTraverser());
            return instantiateCollection(classType, listItems);
        }

        private Object instantiateCollection(Class<?> classType, List itemsForList) {
            int modifiers = classType.getModifiers();
            boolean cannotInstantiateSpecificClass = classType.isInterface() || Modifier.isAbstract(modifiers);
            if (cannotInstantiateSpecificClass) {
                return instantiateCollectionForInterface(classType, itemsForList);
            } else {
                try {
                    List result = (List) classType.newInstance();
                    //noinspection unchecked
                    result.addAll(itemsForList);
                    return result;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        protected abstract Object instantiateCollectionForInterface(Class<?> classType, List itemsForList);
    }

    private static class ListInitializer extends RandomCollectionInitializerParent {    //TODO MM: make superclass to allow extend this to set, array, etc.

        @Override
        public boolean canProvideValueFor(TraverserNode traverserNode) {
            Class<?> classType = traverserNode.getClassType();
            return List.class.isAssignableFrom(classType);
        }

        @Override
        protected Object instantiateCollectionForInterface(Class<?> classType, List itemsForList) {
            //TODO MM: allow to specify which lists are created.
            //noinspection unchecked
            return new ArrayList(itemsForList);
        }
    }

    private static class SetInitializer extends RandomCollectionInitializerParent {

        @Override
        public boolean canProvideValueFor(TraverserNode traverserNode) {
            Class<?> classType = traverserNode.getClassType();
            return Set.class.isAssignableFrom(classType);
        }

        @Override
        protected Object instantiateCollectionForInterface(Class<?> classType, List itemsForList) {
            //TODO MM: allow to specify which set are created.
            //noinspection unchecked
            return new HashSet(itemsForList);
        }
    }

    private static class ArraInitializer extends RandomCollectionInitializerParent {

        @Override
        public boolean canProvideValueFor(TraverserNode traverserNode) {
            return traverserNode.getClassType().isArray();
        }

        @Override
        public Object generateRandomValue(TraverserNode traverserNode) {
            Class<?> typeOfArray = traverserNode.getClassType().getComponentType();

            List listItems = createItemsForCollection(typeOfArray, traverserNode.getTraverser());

            Object newArray = Array.newInstance(typeOfArray, listItems.size());
            for(int i = 0; i < listItems.size(); i++) {
                Array.set(newArray, i, listItems.get(i));
            }

            return newArray;
        }

        @Override
        protected Type typeOfListElements(TraverserNode traverserNode) {
            throw new UnsupportedOperationException("Should not be reachable");  //TODO MM: fix invalid hierarchy.
        }

        @Override
        protected Object instantiateCollectionForInterface(Class<?> classType, List itemsForList) {
            throw new UnsupportedOperationException("Should not be reachable");
        }
    }


    //<editor-fold desc="TrivialInitializers">
    private static class IntInitializer extends SimpleInitializer {
        public IntInitializer() {
            super(Integer.TYPE, Integer.class);
        }

        @Override
        public Object generateRandomValue(TraverserNode traverserNode) {
            return random.nextInt();
        }
    }

    private static class EnumInitializer extends RandomInitializer {
        @Override
        public boolean canProvideValueFor(TraverserNode traverserNode) {
            return traverserNode.getClassType().isEnum();
        }

        @Override
        public Object generateRandomValue(TraverserNode traverserNode) {
            Object[] values = traverserNode.getClassType().getEnumConstants();
            return values[random.nextInt(values.length)];
        }
    }

    private static class BooleanInitializer extends SimpleInitializer {
        public BooleanInitializer() {
            super(Boolean.TYPE, Boolean.class);
        }

        @Override
        public Object generateRandomValue(TraverserNode traverserNode) {
            return random.nextBoolean();
        }
    }

    private static class JavaUtilDateInitializer extends SimpleInitializer {
        public JavaUtilDateInitializer() {
            super(Date.class);
        }

        @Override
        public Object generateRandomValue(TraverserNode traverserNode) {
            int date = random.nextInt();
            date = date < 0 ? -1 * date : date;
            return new Date(date);
        }
    }

    private static class UuidInitializer extends SimpleInitializer {
        public UuidInitializer() {
            super(UUID.class);
        }

        @Override
        public Object generateRandomValue(TraverserNode traverserNode) {
            return UUID.randomUUID();
        }
    }

    private static class StringInitializer extends SimpleInitializer {
        public StringInitializer() {
            super(String.class);
        }

        @Override
        public Object generateRandomValue(TraverserNode traverserNode) {
            return "RandomString: " + Long.toString(random.nextLong());
        }
    }
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="InitializersDefinition">
    protected static abstract class RandomInitializer implements Initializer {
        protected static final Random random = new Random();

    }
    
    private static abstract class SimpleInitializer extends RandomInitializer {

        private final List<Class<?>> classes;

        public SimpleInitializer(Class<?> ... classes) {
            this(Arrays.asList(classes));
        }

        public SimpleInitializer(List<Class<?>> classes) {
            this.classes = classes;
        }

//        @Override
//        public List<Class<?>> providesValueFor() {
//            return classes;
//        }

        @Override
        public boolean canProvideValueFor(TraverserNode traverserNode) {
            return classes.contains(traverserNode.getClassType());
        }
    }

    private interface Initializer {
        boolean canProvideValueFor(TraverserNode traverserNode);
        Object generateRandomValue(TraverserNode traverserNode);
    }
    //</editor-fold>

    public interface Processor {
        void process(FieldTraverserNode node);
    }

    public static class InitializingProcessor implements Processor {

        private List<Initializer> initializers = Arrays.asList(new BooleanInitializer(),
                new JavaUtilDateInitializer(),
                new UuidInitializer(),
                new IntInitializer(),
                new StringInitializer(),
                new EnumInitializer(),
                new ListInitializer(),
                new SetInitializer(),
                new ArraInitializer());

        @Override
        public void process(FieldTraverserNode node) {
            //TODO MM: decision whether to set primitive values, or all values or only null values
//            if (node.getValue() == null) {
                Class<?> classType = node.getClassType();
                List<Initializer> suitableInitializers = initializers.stream()
                    .filter(e -> e.canProvideValueFor(node))
                    .collect(Collectors.toList());


                //TODO MM: allow to configure.
                if (suitableInitializers.isEmpty()) {
                    throw new IllegalStateException("Unknown initializer for type: " + classType.getName());
                }

                if (suitableInitializers.size() > 1) {
                    throw new IllegalStateException("Multiple initializers for type: " + classType.getName());
                }

                Initializer initializer = suitableInitializers.get(0);
                node.setValue(initializer.generateRandomValue(node));
//            }
        }
    }

    public interface Traverser {

        <T> T process(T instance);

        <T> T process(T instance, Class<?> instanceClass);
    }

    public static class FieldTraverser implements Traverser {
        private final Processor processor;

        public FieldTraverser(Processor processor) {
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
    }

    private interface TraverserNode {
        Object getValue();

        void setValue(Object value);

        Class<?> getClassType();

        Optional<ParameterizedType> getParameterizedType();

        Traverser getTraverser();
    }

    private static class FieldTraverserNode implements TraverserNode{
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
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Class<?> getClassType() {
            return field.getType();
        }

        @Override
        public Optional<ParameterizedType> getParameterizedType() {
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                return Optional.of(((ParameterizedType) genericType));
            } else {
                return Optional.empty();
            }
        }

        @Override
        public FieldTraverser getTraverser() {
            return fieldTraverser;
        }
    }

//
//    public static class Config {
//        private Class<?> stopClass = Object.class;
//
//        public Class<?> getStopClass() {
//            return stopClass;
//        }
//
//        public void setStopClass(Class<?> stopClass) {
//            this.stopClass = stopClass;
//        }
//    }

}
