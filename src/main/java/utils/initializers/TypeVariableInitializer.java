package utils.initializers;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import utils.traverser.ClassTreeTraverserContext;

public class TypeVariableInitializer extends InitializerParent {


    @Override
    public boolean canProvideValueFor(Class<?> type, Type genericType) {
        return genericType instanceof TypeVariable;
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, ClassTreeTraverserContext context) {

        TypeVariable typeVariable = (TypeVariable) genericType;
        Type typeOfTypeVariable = TypeVariableUtil.findActualTypeForTypeVariable(typeVariable, context);

        return this.getInitializers().generateValue(typeOfTypeVariable, context);//TODO MM: traverse over returned instance?
    }
}
