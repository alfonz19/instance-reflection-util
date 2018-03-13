package utils;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class InstanceReflectionUtil {

    private static final Logger log = LogManager.getLogger(InstanceReflectionUtil.class);

    //<editor-fold desc="SpecificInitializers">
    private static abstract class ArrayLikeInitializerParent extends RandomInitializer {
        private Type typeOfListElements(Type genericType) {
            if (genericType instanceof Class) {
                throw new RuntimeException("Unknown type of instances to be created.");
            } else if (genericType instanceof ParameterizedType) {
                //TODO MM: allow to specify subclasses to be instantiated as well.

                ParameterizedType parameterizedType = (ParameterizedType) genericType;

                //lists have just one type parameter. //TODO MM: reuse for maps etc.
                return parameterizedType.getActualTypeArguments()[0];
            } else if (genericType instanceof GenericArrayType) {
                GenericArrayType genericArrayType = (GenericArrayType) genericType;
                return genericArrayType.getGenericComponentType();
            } else {
                throw new RuntimeException("Unknown type of instances to be created.");
            }
        }

        protected List createItemsForCollection(Type typeOfListElements, Traverser traverser) {
            //TODO MM: allow specification number of items. Globally 0/1..N, locally. Allow null for whole container? Allow null internal values?
            int itemCount = 1 + random.nextInt(5);

            List result = new ArrayList(itemCount);
            for (int i = 0; i < itemCount; i++) {
                try {
                    Initializer initializer = this.getInitializers().getSoleInitializer(GenericType.getClassType(typeOfListElements), typeOfListElements);

                    Object newInstance = initializer.getValue(GenericType.getClassType(typeOfListElements), typeOfListElements, traverser);

                    //noinspection unchecked
                    result.add(newInstance);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            return result;
        }

        @Override
        public Object getValue(Class<?> type, Type genericType, Traverser traverser) {
            List listItems = createItemsForCollection(typeOfListElements(genericType), traverser);
            return instantiateCollection(type, listItems);
        }

        private Object instantiateCollection(Class<?> type, List itemsForList) {
            int modifiers = type.getModifiers();
            boolean cannotInstantiateSpecificClass = type.isInterface() || Modifier.isAbstract(modifiers);
            if (cannotInstantiateSpecificClass) {
                return instantiateCollectionForInterface(type, itemsForList);
            } else {
                try {
                    List result = (List) type.newInstance();
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

    private static class ListInitializer extends ArrayLikeInitializerParent {    //TODO MM: make superclass to allow extend this to set, array, etc.

        @Override
        public boolean canProvideValueFor(Class<?> type, Type genericType) {
            return List.class.isAssignableFrom(type);
        }

        @Override
        protected Object instantiateCollectionForInterface(Class<?> classType, List itemsForList) {
            //TODO MM: allow to specify which lists are created.
            //noinspection unchecked
            return new ArrayList(itemsForList);
        }
    }

    private static class SetInitializer extends ArrayLikeInitializerParent {

        @Override
        public boolean canProvideValueFor(Class<?> type, Type genericType) {
            return Set.class.isAssignableFrom(type);
        }

        @Override
        protected Object instantiateCollectionForInterface(Class<?> classType, List itemsForList) {
            //TODO MM: allow to specify which set are created.
            //noinspection unchecked
            return new HashSet(itemsForList);
        }
    }

    private static class ArraInitializer extends ArrayLikeInitializerParent {

        @Override
        public boolean canProvideValueFor(Class<?> type, Type genericType) {
            return type.isArray();
        }

        @Override
        public Object getValue(Class<?> type, Type genericType, Traverser traverser) {
            Type typeOfArrayItems = GenericType.getTypeOfArrayElements(genericType);

            List listItems = createItemsForCollection(typeOfArrayItems, traverser);

            Object newArray = Array.newInstance(GenericType.getClassType(typeOfArrayItems), listItems.size());
            for(int i = 0; i < listItems.size(); i++) {
                Array.set(newArray, i, listItems.get(i));
            }

            return newArray;
        }

        @Override
        protected Object instantiateCollectionForInterface(Class<?> classType, List itemsForList) { //TODO MM: fix invalid hierarchy.
            throw new UnsupportedOperationException("Should not be reachable");
        }
    }


    //<editor-fold desc="TrivialInitializers">
    private static class IntInitializer extends SimpleInitializer {
        public IntInitializer() {
            super(Integer.TYPE, Integer.class);
        }

        @Override
        public Object getValue(Class<?> type, Type genericType, Traverser traverser) {
            return random.nextInt();
        }
    }

    private static class EnumInitializer extends RandomInitializer {
        @Override
        public boolean canProvideValueFor(Class<?> type, Type genericType) {
            return type.isEnum();
        }

        @Override
        public Object getValue(Class<?> type, Type genericType, Traverser traverser) {
            Object[] values = type.getEnumConstants();
            return values[random.nextInt(values.length)];
        }
    }

    private static class BooleanInitializer extends SimpleInitializer {
        public BooleanInitializer() {
            super(Boolean.TYPE, Boolean.class);
        }

        @Override
        public Object getValue(Class<?> type, Type genericType, Traverser traverser) {
            return random.nextBoolean();
        }
    }

    private static class JavaUtilDateInitializer extends SimpleInitializer {
        public JavaUtilDateInitializer() {
            super(Date.class);
        }

        @Override
        public Object getValue(Class<?> type, Type genericType, Traverser traverser) {
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
        public Object getValue(Class<?> type, Type genericType, Traverser traverser) {
            return UUID.randomUUID();
        }
    }

    private static class StringInitializer extends SimpleInitializer {
        public StringInitializer() {
            super(String.class);
        }

        @Override
        public Object getValue(Class<?> type, Type genericType, Traverser traverser) {
            return "RandomString: " + Long.toString(random.nextLong());
        }
    }
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="InitializersDefinition">
    protected static abstract class InitializerParent implements Initializer {
        private Initializers initializers;

        @Override
        public final void setInitializers(Initializers initializers) {
           this.initializers = initializers;
        }

        public Initializers getInitializers() {
            return initializers;
        }
    }

    private static class DefaultConstructorInitializer extends InitializerParent {

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
        public Object getValue(Class<?> type, Type genericType, Traverser traverser) {
            try {
                Constructor<?> publicNoArgConstructor = type.getConstructor();
                Object instance = publicNoArgConstructor.newInstance();
                return traverser.process(instance);
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected static abstract class RandomInitializer extends InitializerParent {
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

        @Override
        public boolean canProvideValueFor(Class<?> type, Type genericType) {
            return classes.contains(type);
        }
    }

    private interface Initializer {
        boolean canProvideValueFor(Class<?> type, Type genericType);
        Object getValue(Class<?> type, Type genericType, Traverser traverser);

        /** sets reference to all initializers known to system, in order to be able to do composite initializations. Example: when you initializing list, which contains sets of integers. So you need to initialize list, for each item new set, and for each set several integers */
        void setInitializers(Initializers initializers);
    }
    //</editor-fold>

    public interface Processor {
        void process(FieldTraverserNode node);
    }

    public static class InitializingProcessor implements Processor {
        private final Initializers initializers = new Initializers();


        @Override
        public void process(FieldTraverserNode node) {
            //TODO MM: decision whether to set primitive values, or all values or only null values
//            if (node.getValue() == null) {
            Initializer initializer = initializers.getSoleInitializer(node.getType(), node.getGenericType());

            node.setValue(initializer.getValue(node.getType(), node.getGenericType(), node.getTraverser()));
//            }
        }
    }

    public static class Initializers {
        private List<Initializer> initializers = createInitializers();

        private List<Initializer> createInitializers() {
            List<Initializer> result = Arrays.asList(
                    new ListInitializer(),
                    new SetInitializer(),
                    new ArraInitializer(),
                    
                    new BooleanInitializer(),
                    new JavaUtilDateInitializer(),
                    new UuidInitializer(),
                    new IntInitializer(),
                    new StringInitializer(),
                    new EnumInitializer(),

                    new DefaultConstructorInitializer());

            result.forEach(e->e.setInitializers(this));
            return result;
        }

        public Initializer getSoleInitializer(Class<?> type, Type genericType) {
            List<Initializer> suitableInitializers = initializers.stream()
                    .filter(e -> e.canProvideValueFor(type, genericType))
                    .collect(Collectors.toList());


            //TODO MM: allow to configure.
            if (suitableInitializers.isEmpty()) {
                throw new IllegalStateException("Unknown initializer for type: " + genericType.getTypeName());
            }

//            if (suitableInitializers.size() > 1) {
//                throw new IllegalStateException("Multiple initializers for type: " + genericType.getTypeName());
//            }

            Initializer initializer = suitableInitializers.get(0);
            return initializer;
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

        Type getGenericType();

        Class<?> getType();

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

    public static class GenericType {
        public static Class<?> getClassType(Type genericType) {
            if (genericType instanceof Class) {
                return (Class) genericType;
            } else if (genericType instanceof ParameterizedType) {
                return (Class)((ParameterizedType)genericType).getRawType();
            } else if (genericType instanceof GenericArrayType) {
                GenericArrayType genericArrayType = (GenericArrayType) genericType;
                Type genericComponentType = genericArrayType.getGenericComponentType();
                if (genericComponentType instanceof ParameterizedType) {
                    //hack to get classType of Array from generic type. There's no way how to do that
                    //so we create here new array based on generic type and use that instance to get class type.
                    //this is far from ideal, thus one should avoid using this method if possible for finding out
                    //class type of array from generic type.
                    log.warn("Used inefficient query to get array class type.");
                    return Array.newInstance((Class)((ParameterizedType)genericArrayType.getGenericComponentType()).getRawType(), 0).getClass();
                } else {
                    throw new UnsupportedOperationException("Not implemented yet");
                }
            } else {
                throw new UnsupportedOperationException("Not implemented yet");
            }
        }

        public static boolean isArray(Type genericType) {
            if (genericType instanceof Class) {
                return ((Class)genericType).isArray();
            } else if (genericType instanceof GenericArrayType) {
                return true;
            } else {
                return false;
            }
        }

        public static Type getTypeOfArrayElements(Type genericType) {
            if (genericType instanceof Class) {
                return ((Class)genericType).getComponentType();
            } else if (genericType instanceof GenericArrayType) {
                GenericArrayType genericArrayType = (GenericArrayType) genericType;
                return genericArrayType.getGenericComponentType();
            } else {
                throw new UnsupportedOperationException("Not implemented yet");
            }
        }
    }

}
