package utils.traverser;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.InstanceReflectionUtilException;
import utils.traverser.Path.InstancePath;

public class FieldTraverser implements ClassTreeTraverser {
    private final Logger logger = LoggerFactory.getLogger(FieldTraverser.class);

    private final TraversingProcessor traversingProcessor;

    public FieldTraverser(TraversingProcessor traversingProcessor) {
        this.traversingProcessor = traversingProcessor;
    }

    @Override
    public <T> T process(T instance) {
        return process(instance, instance.getClass());
    }

    @Override
    public <T> T process(T instance, PathNode pathNode, ClassTreeTraverserContext context) {
        return process(instance, instance.getClass(), pathNode, context);
    }

    @Override
    public <T> T process(T instance, Class<?> startClass) {
        return process(instance, startClass, new PathNode(new InstancePath()), new ClassTreeTraverserContext(this));
    }

    @Override
    public <T> T process(T instance, Class<?> startClass, PathNode pathNode, ClassTreeTraverserContext context) {
        if (!startClass.isAssignableFrom(instance.getClass())) {
            throw new IllegalArgumentException();
        }

        logger.debug("\n\n" +
                "--------------------------------------------------------------------------------\n" +
                "Traversing:\n\tfrom path: '{}'\n\tfrom instance: '{}' ({}),\n\t starting with: '{}'.\n" +
                "--------------------------------------------------------------------------------\n",
            pathNode.getPath(),
            instance,
            instance.getClass(),
            startClass);


        Class<?> instanceClass = startClass;
        Class<Object> stopClazz = Object.class;
        //we're looping from instance class to it's top most parent. So this subclass is genericSuperclass of class from preceding loop.
        Type genericSuperclassOfSubclass = null;

        logger.debug("Analyzing class hierarchy <{}, {}) nodes in {}\n", startClass, stopClazz, instance.getClass());
        do {
            logger.debug("Analyzing nodes in class {}", instanceClass);


            //this is what is written after extends keyword.
            Type genericSuperclass = instanceClass.getGenericSuperclass();
            //these are declared type parameters in class.
            TypeVariable<? extends Class<?>>[] typeParameters = instanceClass.getTypeParameters();

            //TODO MM: this does not require modifiable path node. Extract to method, calculate before new pathnode is created.
            resolveTypeParametersInClassHierarchy(instanceClass, genericSuperclassOfSubclass, typeParameters, pathNode);


            genericSuperclassOfSubclass = genericSuperclass;

            processFieldsInCurrentClass(instance, instanceClass, pathNode, context);
            instanceClass = instanceClass.getSuperclass();
        } while (instanceClass != null && !instanceClass.isAssignableFrom(stopClazz));
        logger.debug("End of analyzing class hierarchy <{}, {}>nodes in {}\n", startClass, stopClazz);


        return instance;
    }

    private void resolveTypeParametersInClassHierarchy(Class<?> instanceClass,
                                                                                    Type genericSuperclassOfSubclass,
                                                                                    TypeVariable<? extends Class<?>>[] typeParameters,
                                                                                    PathNode pathNode) {
        //there are type parameters to be resolved.
        if (typeParameters.length > 0) {
            if (!ReflectUtil.isParameterizedType(genericSuperclassOfSubclass)) {
                logger.debug("Unable to determine type parameters {} from class hierarchy scan, subclass of {} is not parameterized", typeParameters, instanceClass);
            } else {
                List<Type> typeListOfSubclass = ReflectUtil.getActualArgumentsList(genericSuperclassOfSubclass);
                for (int index = 0, typeParametersLength = typeParameters.length; index < typeParametersLength; index++) {
                    TypeVariable<? extends Class<?>> typeParameter = typeParameters[index];
                    if (index >= typeListOfSubclass.size()) {
                        logger.info(
                            "Unable to determine type parameter {}, subclass of {} does not contain definition for it.",
                            typeParameter,
                            instanceClass);
                    } else {
                        Type type = typeListOfSubclass.get(index);
//                            pathNode.setTypeOfTypeVariable(typeParameter, type);

                        //found type is already known type variable, substitute it.
                        if (type instanceof TypeVariable) {
                            Optional<Type> val = pathNode.getTypeOfTypeVariable((TypeVariable) type);
                            if (val.isPresent()) {
                                pathNode.setTypeOfTypeVariable(typeParameter, val.get());
                                logger.debug("Resolved type variable '{}' as ''", typeParameter, val.get());
                            } else {
                                throw new UnsupportedOperationException("Not implemented yet");
                            }
                        } else if (type instanceof Class) {
                            pathNode.setTypeOfTypeVariable(typeParameter, type);
                            logger.debug("Resolved type variable '{}' as ''", typeParameter, type);
                        } else if (type instanceof ParameterizedType) {
                            ParameterizedType asParameterizedType = (ParameterizedType) type;
                            pathNode.setTypeOfTypeVariable(typeParameter, asParameterizedType);
                        } else {
                            throw new UnsupportedOperationException("Not implemented yet");
                        }
                    }
                }

            }
        }
    }

    private <T> void processFieldsInCurrentClass(T instance,
                                                 Class<?> instanceClass,
                                                 PathNode pathNode,
                                                 ClassTreeTraverserContext context) {
        Field[] fields = instanceClass.getDeclaredFields();

        for (Field field : fields) {
            if (field.getName().equals("this$0")) {
                continue;
            }

            field.setAccessible(true);
            TraverserNode traverserNode = createTraverserNode(instance, field);
            ModifiableTraverserNode modifiableNode = new ModifiableFieldTraverserNode(instance, field, traverserNode);

            traversingProcessor.process(modifiableNode, new PathNode(pathNode, traverserNode), context);
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
            throw new InstanceReflectionUtilException(e);
        }
    }

    private static class FieldTraverserNode implements TraverserNode {

        private final Object nodeValue;
        private final Type genericType;
        private final Class<?> type;
        private final Class<?> declaringClass;
        private final Class<?> instanceClass;
        private final String fieldName;

        private FieldTraverserNode(Object nodeValue,
                                   Type genericType,
                                   Class<?> type,
                                   Class<?> declaringClass,
                                   Class<?> instanceClass,
                                   String fieldName) {
            this.nodeValue = nodeValue;
            this.genericType = genericType;
            this.type = type;
            this.declaringClass = declaringClass;
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
            return declaringClass;
        }

        @Override
        public Class<?> getInstanceClass() {
            return instanceClass;
        }

        @Override
        public  boolean declaredInInstanceClass() {
            return declaringClass.equals(instanceClass);
        }

        @Override
        public String toString() {
            return "FieldTraverserNode{" +
                "declaringClassOfNode=" + declaringClass +
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
                throw new InstanceReflectionUtilException(e);
            }
        }

        @Override
        public TraverserNode getTraverserNode() {
            return traverserNode;
        }
    }
}
