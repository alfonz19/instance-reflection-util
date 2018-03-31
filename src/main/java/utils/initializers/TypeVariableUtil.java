package utils.initializers;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.traverser.ClassTreeTraverserContext;
import utils.traverser.TraverserNode;

public class TypeVariableUtil {
    private static final Logger logger = LoggerFactory.getLogger(TypeVariableUtil.class);

    /**
     * @param typeVariable type variable we're looking for it's actual type.
     * @param context context describing position it traversing tree.
     * @return generic type used to define type variable.
     */
    public static Type findActualTypeForTypeVariable(TypeVariable typeVariable, ClassTreeTraverserContext context) {
        logger.debug("Looking for type variable '{}' for at path '{}'", typeVariable, context.getNodesFromRootAsNamesPath());

        return findActualTypeForTypeVariable(typeVariable, context, context.getNodesFromRoot().size() -1);
    }

    private static Type findActualTypeForTypeVariable(Type typeVariable,
                                                      ClassTreeTraverserContext context,
                                                      int nodeIndex) {

        TraverserNode node = context.getNodesFromRoot().get(nodeIndex);
        logger.debug("Looking for type variable '{}' in node:\n\t\tdeclaringClass ='{}',\n\t\t instanceClass='{}'",
                typeVariable,
                node.getDeclaringClass(),
                node.getInstanceClass());

        Class<?> declaringClass = node.getDeclaringClass();


        Class<?> instanceClass = node.getInstanceClass();

        //we check, if class 'node' is declared at is the same class as the one instance is class of.
        boolean declaredInInstanceClass = declaringClass.equals(instanceClass);

        if (!declaredInInstanceClass) {
            Optional<Type> foundType = findTypeVariableInSuperClassOfDeclaringClass(
                    declaringClass,
                    instanceClass,
                    typeVariable);

            if (foundType.isPresent()) {
                return foundType.get();
            }

            //if we reach here, it means that type wasn't found in superclasses.
        }

        //scan in superclasses couldn't be used or failed. We proceed scan in node through we got here,
        //as the type variable has to be defined there.
        if (nodeIndex >0) {
            //there is some up-the-path node we can scan.

            logger.debug("Node is defined in instance class, not checking superclass, proceeding with node up the path.");
            int previousNodeIndex = nodeIndex - 1;
            TraverserNode previousNode = context.getNodesFromRoot().get(previousNodeIndex);
            Type genericTypeOfPreviousNode = previousNode.getGenericType();
            if (genericTypeOfPreviousNode instanceof Class) {
                throw new UnsupportedOperationException("Previous node is not parameterized, which means, that type cannot be inferred.");
            }

            if (genericTypeOfPreviousNode instanceof ParameterizedType) {
                int indexOfTypeVariable = getIndexOfTypeVariableInGenericClassTypes(typeVariable, declaringClass);

                Type typeToSearchFor =
                    ((ParameterizedType) genericTypeOfPreviousNode).getActualTypeArguments()[indexOfTypeVariable];

                if (typeToSearchFor instanceof Class) {
                    return typeToSearchFor;
                }

                if (typeToSearchFor instanceof TypeVariable) {
                    logger.debug("have to look up-the-path node for {}", typeToSearchFor);
                    return findActualTypeForTypeVariable(typeToSearchFor, context, previousNodeIndex);
                } else {
                    throw new UnsupportedOperationException("Not implemented yet");
                }
            } else {
                throw new UnsupportedOperationException("Not implemented yet");
            }

        } else {
            //the type value can be only in up-the-path node, but there's none.

            logger.debug("Node is defined in instance class, no super class, no parent node, nowhere to look for generic type definition.");
            throw new RuntimeException("Unable to determine type, due to type erasure or object tree.");   //TODO MM: better exception.
        }
    }

    private static int getIndexOfTypeVariableInGenericClassTypes(Type typeVariable, Class<?> declaringClass) {
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

    private static Optional<Type> findTypeVariableInSuperClassOfDeclaringClass(Class<?> declaringClass,
                                                                               Class<?> instanceClass,
                                                                               Type typeVariable) {

        //start with instance class, and find Class, which superclass is superclass of given declaring class.
        //so 2 classes above. Why? Because we need to get Type, and that can be obtained only via getGenericSuperclass
        //but you cannot repeatedly call that method ...
        Class<?> scanInClass = instanceClass;
        while (!scanInClass.getSuperclass().equals(declaringClass)) {
            scanInClass = scanInClass.getSuperclass();
        }

        //after found that node as mentioned above, we finally get Type of superclass of declaring class.
        Type genericSuperclass = scanInClass.getGenericSuperclass();

        int indexOfTypeVariable = getIndexOfTypeVariableInGenericClassTypes(typeVariable, declaringClass);

        if (genericSuperclass instanceof ParameterizedType) {
            Type typeFoundInParent = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[indexOfTypeVariable];
            if (typeFoundInParent instanceof Class) {
                return Optional.of(typeFoundInParent);
            } else if (typeFoundInParent instanceof TypeVariable){
                return findTypeVariableInSuperClassOfDeclaringClass(scanInClass, instanceClass, typeFoundInParent);
            } else {

                //here I got: ClassWithPairHavingTypeDefinedInClass<X,Y> — so I have to instanciate this class, and continue scan for type variables X and Y
                throw new UnsupportedOperationException("Not implemented yet");
            }
        } else if (genericSuperclass instanceof Class) {
            return Optional.empty();
        } else if (genericSuperclass instanceof GenericArrayType) {
            return Optional.of(((GenericArrayType) genericSuperclass).getGenericComponentType());
        } else if (genericSuperclass instanceof WildcardType) {
            throw new UnsupportedOperationException("Not implemented yet");
        } else {
            throw new UnsupportedOperationException("Not implemented yet");
        }
    }
}






//        List<TraverserNode> nodesFromRoot = context.getNodesFromRoot();
//        TraverserNode previousNode = nodesFromRoot.get(nodesFromRoot.size() - 2);
//
//
//        Type genericTypeOfPreviousNode = previousNode.getGenericType();
//        if (!(genericTypeOfPreviousNode instanceof ParameterizedType)) {
//            throw new UnsupportedOperationException("Not implemented yet -- previous node is not parameterized, type parameter must come from different location, or it's not defined.");
//        }
//
//        Type typeDefinedInSuperclassField =
//            ((ParameterizedType) genericTypeOfPreviousNode).getActualTypeArguments()[indexOfTypeVariable];
//        if (typeDefinedInSuperclassField instanceof Class) {
//            return typeDefinedInSuperclassField;
//        }
//
//        if (!(typeDefinedInSuperclassField instanceof TypeVariable)) {
//            throw new UnsupportedOperationException("Not implemented yet -- it's not type variable");
//        }
//
//        TypeVariable[] classTypeParametersOfPreviousNodeDeclaringClass = previousNode.getDeclaringClassTypeParameters();
//        int typeVariableIndex = indexOfTypeVariable((TypeVariable)typeDefinedInSuperclassField, classTypeParametersOfPreviousNodeDeclaringClass);
//        if (typeVariableIndex == -1) {
//            return getTypeFromSuperClass(typeVariable, previousNode);
//        } else {
//            Type possibleResult = classTypeParametersOfPreviousNodeDeclaringClass[typeVariableIndex];
//            if (possibleResult instanceof Class) {
//                return possibleResult;
//            } else {
//                //we'are missing scanning superclasses of declared class for type variables. And also wildcards. Looping.
//                Type genericSuperclassSignature = previousNode.getInstanceClass().getGenericSuperclass();
//                if (!(genericSuperclassSignature instanceof ParameterizedType)) {
//                    throw new UnsupportedOperationException("Not implemented yet");
//                } else {
//                    return ((ParameterizedType) genericSuperclassSignature).getActualTypeArguments()[typeVariableIndex];
//                }
//            }
//        }
