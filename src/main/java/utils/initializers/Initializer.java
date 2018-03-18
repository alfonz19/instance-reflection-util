package utils.initializers;

import java.lang.reflect.Type;

import utils.Initializers;
import utils.traverser.ClassTreeTraverserContext;

public interface Initializer {
    boolean canProvideValueFor(Class<?> type, Type genericType);
    Object getValue(Class<?> type, Type genericType, ClassTreeTraverserContext context);

    /** sets reference to all initializers known to system, in order to be able to do composite initializations. Example: when you initializing list, which contains sets of integers. So you need to initialize list, for each item new set, and for each set several integers */
    void setInitializers(Initializers initializers);
}
