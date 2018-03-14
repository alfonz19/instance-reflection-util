package utils.initializers;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;

import utils.traverser.ClassTreeTraverserContext;
import utils.traverser.TraverserNode;

public class TypeVariableInitializer extends InitializerParent {

    @Override
    public boolean canProvideValueFor(Class<?> type, Type genericType) {
        return genericType instanceof TypeVariable;
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, ClassTreeTraverserContext context) {

//        this return some type variable, like T.
        TypeVariable typeVariable = (TypeVariable) genericType;
        Type typeOfTypeVariable = findActualTypeForTypeVariable(typeVariable, context);

        return this.getInitializers().generateValue(typeOfTypeVariable, context);//TODO MM: descend?
    }


    //TODO MM: document!
    //correct call looks like: ((ParameterizedType)((FieldTraverser.FieldTraverserNode)context.nodesFromRoot.get(0)).instance.getClass().getGenericSuperclass()).getActualTypeArguments()

    //we can scan current class, if it defines that type, using:
//        ((FieldTraverser.FieldTraverserNode)context.getCurrentNode()).field.getDeclaringClass().getTypeParameters()
    //this should return ordered type parameters, like T,K. Then we can navigate to parent, and check actual type parameters. So the first one will be our T.
    private Type findActualTypeForTypeVariable(TypeVariable typeVariable, ClassTreeTraverserContext context) {

        TypeVariable[] typeParametersOfDeclaringClass =
                context.getCurrentNode().getDeclaringClassTypeParameters();

        int indexOfTypeVariable = indexOfTypeVariable(typeVariable, typeParametersOfDeclaringClass);
        if (indexOfTypeVariable == -1) {
            throw new UnsupportedOperationException("Not implemented yet -- declaring class does not have type parameters, the type parameter must come from different location");
        }

        List<TraverserNode> nodesFromRoot = context.getNodesFromRoot();
        TraverserNode previousNode = nodesFromRoot.get(nodesFromRoot.size() - 2);


        Type genericTypeOfPreviousNode = previousNode.getGenericType();
        if (!(genericTypeOfPreviousNode instanceof ParameterizedType)) {
            throw new UnsupportedOperationException("Not implemented yet -- previous node is not parameterized, type parameter must come from different location, or it's not defined.");
        }

        Type typeDefinedInSuperclassField =
            ((ParameterizedType) genericTypeOfPreviousNode).getActualTypeArguments()[indexOfTypeVariable];
        if (typeDefinedInSuperclassField instanceof Class) {
            return typeDefinedInSuperclassField;
        }

        if (!(typeDefinedInSuperclassField instanceof TypeVariable)) {
            throw new UnsupportedOperationException("Not implemented yet -- it's not type variable");
        }

        TypeVariable[] classTypeParametersOfPreviousNodeDeclaringClass = previousNode.getDeclaringClassTypeParameters();
        int typeVariableIndex = indexOfTypeVariable((TypeVariable)typeDefinedInSuperclassField, classTypeParametersOfPreviousNodeDeclaringClass);
        if (typeVariableIndex == -1) {
            return getTypeFromSuperClass(typeVariable, previousNode);
        } else {
            Type possibleResult = classTypeParametersOfPreviousNodeDeclaringClass[typeVariableIndex];
            if (possibleResult instanceof Class) {
                return possibleResult;
            } else {
                //we'are missing scanning superclasses of declared class for type variables. And also wildcards. Looping.
                Type genericSuperclassSignature = previousNode.getInstanceClass().getGenericSuperclass();
                if (!(genericSuperclassSignature instanceof ParameterizedType)) {
                    throw new UnsupportedOperationException("Not implemented yet");
                } else {
                    return ((ParameterizedType) genericSuperclassSignature).getActualTypeArguments()[typeVariableIndex];
                }
            }
        }
    }

    private Type getTypeFromSuperClass(TypeVariable typeVariable, TraverserNode traverserNode) {
        TypeVariable[] classTypeParametersOfSuperClassUpThePath = traverserNode.getDeclaringClassTypeParameters();
        int index3 = indexOfTypeVariable(typeVariable, classTypeParametersOfSuperClassUpThePath);
        return classTypeParametersOfSuperClassUpThePath[index3];
    }

    private int indexOfTypeVariable(TypeVariable typeVariable,
                                    TypeVariable[] typeParameters) {
        //we look for this
        String typeVariableName = typeVariable.getName();


        for (int i = 0, length = typeParameters.length; i < length; i++) {
            TypeVariable itemAtIndex = typeParameters[i];
            if (itemAtIndex.getName().equals(typeVariableName)) {
                return i;
            }
        }
        return -1;
    }
}
