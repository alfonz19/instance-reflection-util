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
import utils.traverser.PathNode;
import utils.traverser.TraverserNode;

public class TypeVariableUtil {
    private static final Logger logger = LoggerFactory.getLogger(TypeVariableUtil.class);

    /**
     * @param typeVariable type variable we're looking for it's actual type.
     * @param context context describing position it traversing tree.
     * @return generic type used to define type variable.
     */
    public static Type findActualTypeForTypeVariable(TypeVariable typeVariable, PathNode pathNode, ClassTreeTraverserContext context) {
        logger.debug("Looking for type variable '{}' for at path '{}'",
            typeVariable,
            pathNode.getPath().getPathAsString());

        return findActualTypeForTypeVariable(typeVariable, context, pathNode);
    }

    private static Type findActualTypeForTypeVariable(Type typeVariable,
                                                      ClassTreeTraverserContext context,
                                                      PathNode pathNode) {

        TraverserNode node = pathNode.getTraverserNode();
        logger.debug("Looking for type variable '{}' in node:\n\t\tdeclaringClass ='{}',\n\t\t instanceClass='{}'",
                typeVariable,
                node.getDeclaringClass(),
                node.getInstanceClass());


        //----//TODO MM: probably from here to and of scan in parents — this can be extracted to one method/util, since optional is used anyway.
        //we check, if class 'node' is declared at is the same class as the one instance is class of.
        boolean canCheckSuperClasses = !node.declaredInInstanceClass();
        if (canCheckSuperClasses) {
            Optional<Type> foundType = findTypeVariableInSuperClassOfDeclaringClass(
                    node.getDeclaringClass(),
                    node.getInstanceClass(),
                    typeVariable);

            if (foundType.isPresent()) {
                return foundType.get();
            }

            //if we reach here, it means that type wasn't found in superclasses.
        }

        //scan in superclasses couldn't be used or failed. We proceed scan in node through we got here,
        //as the type variable has to be defined there.
        if (pathNode.hasPreviousPathNode()) {
            PathNode previousPathNode = pathNode.getPreviousPathNode();
            TraverserNode previousNode = previousPathNode.getTraverserNode();
            //there is some up-the-path node we can scan.

            logger.debug("Node is defined in instance class, not checking superclass, proceeding with node up the path.");
            Type genericTypeOfPreviousNode = previousNode.getGenericType();
            if (genericTypeOfPreviousNode instanceof Class) {
                throw new UnsupportedOperationException("Previous node is not parameterized, which means, that type cannot be inferred.");
            }

            if (genericTypeOfPreviousNode instanceof ParameterizedType) {
                int indexOfTypeVariable = getIndexOfTypeVariableInGenericClassTypes(typeVariable,
                        node.getDeclaringClass());

                Type typeToSearchFor =
                    ((ParameterizedType) genericTypeOfPreviousNode).getActualTypeArguments()[indexOfTypeVariable];

                if (typeToSearchFor instanceof Class) {
                    return typeToSearchFor;
                }

                if (typeToSearchFor instanceof TypeVariable) {
                    logger.debug("have to look up-the-path node for {}", typeToSearchFor);
                    return findActualTypeForTypeVariable(typeToSearchFor, context, previousPathNode);
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
            } else if (typeFoundInParent instanceof ParameterizedType) {
                logger.debug("Found type in parent, which is parameterized type: '{}'. " +
                        "We have to instantiate class of that type and proceed to search for generic types",
                        typeFoundInParent);
                //here I got: ClassWithPairHavingTypeDefinedInClass<X,Y> — so I have to instantiate this class, and continue scan for type variables X and Y

                throw new UnsupportedOperationException("Not implemented yet");
            } else {
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
