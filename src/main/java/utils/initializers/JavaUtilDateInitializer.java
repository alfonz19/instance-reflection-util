package utils.initializers;

import java.lang.reflect.Type;
import java.util.Date;

import utils.traverser.ClassTreeTraverserContext;

public class JavaUtilDateInitializer extends SimpleInitializer {
    public JavaUtilDateInitializer() {
        super(Date.class);
    }

    @Override
    public Object getValue(Type genericType, ClassTreeTraverserContext context) {
        int date = random.nextInt();
        date = date < 0 ? -1 * date : date;
        return new Date(date);
    }
}
