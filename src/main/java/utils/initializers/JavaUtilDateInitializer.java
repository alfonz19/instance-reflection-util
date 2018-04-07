package utils.initializers;

import java.lang.reflect.Type;
import java.util.Date;

import utils.traverser.ClassTreeTraverserContext;
import utils.traverser.PathNode;

public class JavaUtilDateInitializer extends SimpleInitializer {
    public JavaUtilDateInitializer() {
        super(Date.class);
    }

    @Override
    public Object getValue(Class<?> type, Type genericType, PathNode pathNode, ClassTreeTraverserContext context) {
        int date = random.nextInt();
        date = date < 0 ? -1 * date : date;
        return new Date(date);
    }
}
