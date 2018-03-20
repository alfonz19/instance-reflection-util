package utils.traverser;

import java.lang.reflect.Type;

public interface TraverserNode {
    Object getValue();


    Type getGenericType();

    Class<?> getType();
}
