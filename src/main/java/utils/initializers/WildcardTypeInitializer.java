package utils.initializers;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import utils.traverser.ClassTreeTraverserContext;

public class WildcardTypeInitializer extends InitializerParent {

    @Override
    public boolean canProvideValueFor(Type genericType) {
        return genericType instanceof WildcardType;
    }

    @Override
    public Object getValue(Type genericType, ClassTreeTraverserContext context) {
        WildcardType wildcardType = (WildcardType) genericType;
        Type[] upperBounds = wildcardType.getUpperBounds();

        Type upperBound = upperBounds[0];
        return this.getInitializers().getSoleInitializer(upperBound).getValue(upperBound, context);
    }
}
