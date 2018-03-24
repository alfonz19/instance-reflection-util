package utils.traverser;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public interface TraverserNode {
    String getNodeName();

    Object getValue();

    Type getGenericType();

    Class<?> getType();

    TypeVariable[] getDeclaringClassTypeParameters();

    Class<?> getDeclaringClassOfNode();

    Class<?> getInstanceClass();
}
