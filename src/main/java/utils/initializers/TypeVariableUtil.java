package utils.initializers;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;

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
        logger.debug("Looking for type variable '{}' for context '{}'", typeVariable, context);

        return findActualTypeForTypeVariable(typeVariable, context, context.getNodesFromRoot().size() -1);
    }

    /**
     *
     * @param typeVariable see {@link #findActualTypeForTypeVariable(TypeVariable, ClassTreeTraverserContext)}
     * @param context see {@link #findActualTypeForTypeVariable(TypeVariable, ClassTreeTraverserContext)}
     * @param nodeIndex index of node in traversing path which should be scanned.
     * @return
     */
    private static Type findActualTypeForTypeVariable(Type typeVariable, ClassTreeTraverserContext context, int nodeIndex) {
        logger.debug("------------------------");

        TraverserNode node = context.getNodesFromRoot().get(nodeIndex);
        logger.debug("Looking for type variable '{}' in node '{}'", typeVariable, node);

        TypeVariable[] typeParametersOfDeclaringClass =
                node.getDeclaringClassTypeParameters();

        logger.debug("find declaring class parameters: '{}'", Arrays.asList(typeParametersOfDeclaringClass));
        int indexOfTypeVariable = indexOfTypeVariable(typeVariable, typeParametersOfDeclaringClass);
        if (indexOfTypeVariable == -1) {
            throw new UnsupportedOperationException("Not implemented yet -- declaring class does not have type parameters, the type parameter must come from different location");
        }

        logger.debug("Found that type variable defined in declaring class at index '{}'.", indexOfTypeVariable);

        //we check, if class 'node' is declared at is the same class as the one instance is class of.
        boolean declaredInInstanceClass = node.getDeclaringClassOfNode().equals(node.getInstanceClass());

        if (!declaredInInstanceClass) {
            //here it's not the same class, meaning, that this node was inherited, so we have to check parent class
            //and keep doing that, until condition above is true.
            logger.debug("Node is defined in '{}', but instance has different class: '{}'. Checking superclass.",
                node.getDeclaringClassOfNode(),
                node.getInstanceClass());
            throw new UnsupportedOperationException("Not implemented yet");//return findActualTypeForTypeVariable(typeVariable);
        } else {
            //if it is, we proceed scan in node through we got here, as the type variable has to be defined there.

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
                    Type typeToSearchFor =
                    ((ParameterizedType) genericTypeOfPreviousNode).getActualTypeArguments()[indexOfTypeVariable];

                    logger.debug("have to look up-the-path node for {}", typeToSearchFor);
                    return findActualTypeForTypeVariable(typeToSearchFor, context, previousNodeIndex);
                } else {
                    throw new UnsupportedOperationException("Not implemented yet");
                }

            } else {
                //the type value can be only in up-the-path node, but there's none.

                logger.debug("Node is defined in instance class.");
                throw new UnsupportedOperationException("Not implemented yet");
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
    }


    private static int indexOfTypeVariable(Type type,
                                    Type[] types) {
        for (int i = 0, length = types.length; i < length; i++) {
            Type itemAtIndex = types[i];
            if (itemAtIndex.equals(type)) {
                return i;
            }
        }
        return -1;
    }

//        private Type getTypeFromSuperClass(TypeVariable typeVariable, TraverserNode traverserNode) {
//            TypeVariable[] classTypeParametersOfSuperClassUpThePath = traverserNode.getDeclaringClassTypeParameters();
//            int index3 = indexOfTypeVariable(typeVariable, classTypeParametersOfSuperClassUpThePath);
//            return classTypeParametersOfSuperClassUpThePath[index3];
//        }
}
