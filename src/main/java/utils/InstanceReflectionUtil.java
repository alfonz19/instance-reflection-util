package utils;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class InstanceReflectionUtil {

    private static final Logger log = LogManager.getLogger(InstanceReflectionUtil.class);
    public static final int MAX_ITEMS_TO_CREATE_IN_COLLECTIONS = 5;

    //<editor-fold desc="SpecificInitializers">
    private static abstract class ArrayLikeInitializerParent extends RandomInitializer {

        protected List createItemsForCollection(Type typeOfListElements, Traverser traverser) {
            //TODO MM: allow specification number of items. Globally 0/1..N, locally. Allow null for whole container? Allow null internal values?
            int itemCount = 1 + random.nextInt(MAX_ITEMS_TO_CREATE_IN_COLLECTIONS);

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
            //TODO MM: allow to specify subclasses to be instantiated as well.
            Type typeOfElements = getTypeOfElements(genericType);

            List listItems = createItemsForCollection(typeOfElements, traverser);
            return instantiateCollection(type, typeOfElements, listItems);
        }

        protected abstract Type getTypeOfElements(Type genericType);

        protected abstract Object instantiateCollection(Class<?> type,
                                                        Type typeOfElements,
                                                        List items);
    }

    private static class ListInitializer extends ArrayLikeInitializerParent {
        @Override
        public boolean canProvideValueFor(Class<?> type, Type genericType) {
            return List.class.isAssignableFrom(type);
        }

        @Override
        protected Object instantiateCollection(Class<?> type, Type typeOfElements, List items) {
            int modifiers = type.getModifiers();
            boolean interfaceOrAbstractClass = type.isInterface() || Modifier.isAbstract(modifiers);
            if (interfaceOrAbstractClass) {
                //TODO MM: allow to specify which lists are created.
                //noinspection unchecked
                return new ArrayList(items);
            } else {
                try {
                    List result = (List) type.newInstance();
                    //noinspection unchecked
                    result.addAll(items);
                    return result;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        protected Type getTypeOfElements(Type genericType) {
            return GenericType.typeOfListSetElements(genericType);
        }
    }

    private static class MapInitializer extends RandomInitializer {
        @Override
        public boolean canProvideValueFor(Class<?> type, Type genericType) {
            return Map.class.isAssignableFrom(type);
        }

        @Override
        public Object getValue(Class<?> type, Type genericType, Traverser traverser) {
            Map resultMap = instantiateMap(type);

            Type keyType = getKeyValueType(genericType, 0);
            Type valueType = getKeyValueType(genericType, 1);

            for(int i = 0; i < MAX_ITEMS_TO_CREATE_IN_COLLECTIONS; i++) {
                Object key = getInitializers().generateValue(keyType, traverser);
                Object value = getInitializers().generateValue(valueType, traverser);
                resultMap.put(key, value);
            }

            return resultMap;
        }

        private Type getKeyValueType(Type genericType, int index) {
            if (genericType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericType;

                return parameterizedType.getActualTypeArguments()[index];
            } else {
                throw new RuntimeException("Unknown type of instances to be created.");
            }
        }

        private Map instantiateMap(Class<?> type) {
            int modifiers = type.getModifiers();
            boolean interfaceOrAbstractClass = type.isInterface() || Modifier.isAbstract(modifiers);
            if (interfaceOrAbstractClass) {
                //TODO MM: allow to specify which maps are created.
                //noinspection unchecked
                HashMap hashMap = new HashMap();
                return hashMap;
            } else {
                try {
                    Map result = (Map) type.newInstance();
                    //noinspection uncheckbed
                    return result;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static class CollectionOrIterableInitialier extends RandomInitializer {
        @Override
        public boolean canProvideValueFor(Class<?> type, Type genericType) {
            return Collection.class.isAssignableFrom(type) || Iterable.class.isAssignableFrom(type);
        }

        @Override
        public Object getValue(Class<?> type, Type genericType, Traverser traverser) {
            List<Class<? extends Collection>> delegateTo = Arrays.asList(List.class, Set.class);
            Class<? extends Collection> delegateClass = delegateTo.get(random.nextInt(delegateTo.size()));
            return getInitializers().getSoleInitializer(delegateClass, genericType)
                    .getValue(delegateClass, genericType, traverser);
        }
    }

    private static class SetInitializer extends ArrayLikeInitializerParent {

        @Override
        public boolean canProvideValueFor(Class<?> type, Type genericType) {
            return Set.class.isAssignableFrom(type);
        }

        @Override
        protected Object instantiateCollection(Class<?> type, Type typeOfElements, List items) {
            int modifiers = type.getModifiers();
            boolean interfaceOrAbstractClass = type.isInterface() || Modifier.isAbstract(modifiers);
            if (interfaceOrAbstractClass) {
                //TODO MM: allow to specify which set are created.
                //noinspection unchecked
                return new HashSet(items);
            } else {
                try {
                    Set result = (Set) type.newInstance();
                    //noinspection unchecked
                    result.addAll(items);
                    return result;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        protected Type getTypeOfElements(Type genericType) {
            return GenericType.typeOfListSetElements(genericType);
        }
    }

    private static class ArraInitializer extends ArrayLikeInitializerParent {   //TODO MM: rename

        @Override
        public boolean canProvideValueFor(Class<?> type, Type genericType) {
            return type.isArray();
        }

        @Override
        protected Object instantiateCollection(Class<?> classType, Type typeOfElements, List items) {
            Class<?> componentType = GenericType.getClassType(typeOfElements);

            Object newArray = Array.newInstance(componentType, items.size());
            for(int i = 0; i < items.size(); i++) {
                Array.set(newArray, i, items.get(i));
            }
            return newArray;
        }

        @Override
        protected Type getTypeOfElements(Type genericType) {
            return GenericType.getTypeOfArrayElements(genericType);
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
                    new MapInitializer(),
                    new CollectionOrIterableInitialier(),

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

        public Object generateValue(Type keyType, Traverser traverser) {
            Class<?> classType = GenericType.getClassType(keyType);
            return getSoleInitializer(classType, keyType)
                .getValue(classType, keyType, traverser);
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
                    return Array.newInstance((Class) ((ParameterizedType) genericArrayType.getGenericComponentType()).getRawType(),
                            0).getClass();
                } else {
                    throw new UnsupportedOperationException("Not implemented yet");
                }
            } else if (genericType instanceof WildcardType) {
                //solves only ? extends ...
                WildcardType wildcardType = (WildcardType) genericType;
                Type[] upperBounds = wildcardType.getUpperBounds();
                return (Class) upperBounds[0];  //TODO MM: recursion?
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

        public static Type typeOfListSetElements(Type genericType) {
            if (genericType instanceof Class) {
                throw new RuntimeException("Unknown type of instances to be created.");
            } else if (genericType instanceof ParameterizedType) {

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
    }

}
