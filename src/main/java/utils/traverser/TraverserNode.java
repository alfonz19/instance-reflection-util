package utils.traverser;

import java.lang.reflect.Type;

public interface TraverserNode {
    String getNodeName();

    Object getValue();

    Type getGenericType();

    Class<?> getType();

    Class<?> getDeclaringClass();

    Class<?> getInstanceClass();

    boolean declaredInInstanceClass();
}
