package utils.initializers;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.InstanceReflectionUtilException;
import utils.traverser.ClassTreeTraverserContext;
import utils.traverser.PathNode;
import utils.traverser.TraverserNode;

public class TypeVariableInitializer2 extends InitializerParent {

    private final Logger logger = LoggerFactory.getLogger(TypeVariableInitializer2.class);

    @Override
    public boolean canProvideValueFor(Class<?> type, Type genericType, PathNode pathNode) {
        return genericType instanceof TypeVariable;
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, PathNode pathNode, ClassTreeTraverserContext context) {

//        TypeVariable typeVariable = (TypeVariable) genericType;
//        Type typeOfTypeVariable = typeVariableUtil.findActualTypeForTypeVariable(typeVariable, pathNode, context);
//
//        return this.getInitializers().generateValue(typeOfTypeVariable, pathNode, context);//TODO MM: traverse over returned instance?



        TypeVariable typeVariable = (TypeVariable) genericType;
        if (pathNode.hasPreviousPathNode()) {
            Optional<Type> typeOfTypeVariable = pathNode.getPreviousPathNode().getTypeOfTypeVariable(typeVariable);
            if (typeOfTypeVariable.isPresent()) {
                return getInitializers().generateValue(typeOfTypeVariable.get(), pathNode, context);
            }
        }


        return a(typeVariable, pathNode, context);
//            throw new UnsupportedOperationException("Not implemented yet");



    }


    public Object a(TypeVariable typeVariable, PathNode pathNode, ClassTreeTraverserContext context) {
        TraverserNode traverserNode = pathNode.getTraverserNode();

        if (pathNode.hasPreviousPathNode() && !pathNode.getPreviousPathNode().isRootNode()) {
            PathNode previousPathNode = pathNode.getPreviousPathNode();
            TraverserNode previousNode = previousPathNode.getTraverserNode();
            //there is some up-the-path node we can scan.

            logger.debug("Node is defined in instance class, not checking superclass, proceeding with node up the path.");
            Type genericTypeOfPreviousNode = previousNode.getGenericType();
            if (genericTypeOfPreviousNode instanceof Class) {
                throw new InstanceReflectionUtilException("Previous node is not parameterized, which means, that type cannot be inferred.");
            }

            if (genericTypeOfPreviousNode instanceof ParameterizedType) {
                int indexOfTypeVariable = getIndexOfTypeVariableInGenericClassTypes(typeVariable,
                    traverserNode.getDeclaringClass());

                Type typeToSearchFor =
                    ((ParameterizedType) genericTypeOfPreviousNode).getActualTypeArguments()[indexOfTypeVariable];

                if (typeToSearchFor instanceof Class) {
                    return getInitializers().generateValue(typeToSearchFor, pathNode, context);
                }

                if (typeToSearchFor instanceof TypeVariable) {
                    while (previousPathNode.hasPreviousPathNode()) {
                        previousPathNode = previousPathNode.getPreviousPathNode();
                        logger.debug("have to look up-the-path node for {}", typeToSearchFor);
                        Optional<Type> optionallyFoundTypeForTypeVariable =
                            previousPathNode.getTypeOfTypeVariable((TypeVariable) typeToSearchFor);

                        if (optionallyFoundTypeForTypeVariable.isPresent()) {
                            Type foundType = optionallyFoundTypeForTypeVariable.get();
                            if (foundType instanceof Class<?>) {
                                return getInitializers().generateValue(foundType, pathNode, context);
                            } else {
                                throw new UnsupportedOperationException("Not implemented yet");
                            }
                        }
                    }

                    throw new InstanceReflectionUtilException(String.format("Unable to find actual type for type " +
                        "variable %s.",typeToSearchFor));

                } else {
                    throw new UnsupportedOperationException("Not implemented yet");
                }
            } else {
                throw new UnsupportedOperationException("Not implemented yet");
            }

        } else {
            //the type value can be only in up-the-path node, but there's none.

            logger.debug("Node is defined in instance class, no super class, no parent node, nowhere to look for generic type definition.");
            throw new InstanceReflectionUtilException("Unable to determine type, due to type erasure or object tree.");   //TODO MM: better exception.
        }
    }

    private int getIndexOfTypeVariableInGenericClassTypes(Type typeVariable, Class<?> declaringClass) {
        logger.debug("Searching type parameter '{}' in class '{}' definition", typeVariable, declaringClass);

        TypeVariable[] typeParametersOfClass = declaringClass.getTypeParameters();

        logger.debug("Class has type parameters: '{}'", Arrays.asList(typeParametersOfClass));
        int indexOfTypeVariable = Arrays.asList((Type[]) typeParametersOfClass).indexOf(typeVariable);
        if (indexOfTypeVariable == -1) {
            throw new UnsupportedOperationException("Not implemented yet -- declaring class does not have type parameters, the type parameter must come from different location. Method parameter?");
        }

        logger.debug("Found that type variable defined in declaring class at index '{}'.", indexOfTypeVariable);
        return indexOfTypeVariable;
    }

}
