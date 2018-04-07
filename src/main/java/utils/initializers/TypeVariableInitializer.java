package utils.initializers;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import utils.traverser.ClassTreeTraverserContext;
import utils.traverser.PathNode;

public class TypeVariableInitializer extends InitializerParent {


    @Override
    public boolean canProvideValueFor(Class<?> type, Type genericType, PathNode pathNode) {
        return genericType instanceof TypeVariable;
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, PathNode pathNode, ClassTreeTraverserContext context) {

        TypeVariable typeVariable = (TypeVariable) genericType;
        Type typeOfTypeVariable = TypeVariableUtil.findActualTypeForTypeVariable(typeVariable, pathNode, context);

        return this.getInitializers().generateValue(typeOfTypeVariable, pathNode, context);//TODO MM: traverse over returned instance?
    }
}
