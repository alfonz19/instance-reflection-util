package utils.traverser;

import java.lang.reflect.Type;

public interface TraverserNode {
    Object getValue();

    void setValue(Object value);

    Type getGenericType();

    Class<?> getType();

    ClassTreeTraverser getTraverser();
}
