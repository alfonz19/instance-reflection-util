package utils.traverser;

import utils.Initializers;
import utils.initializers.Initializer;

public class InitializingTraversingProcessor implements TraversingProcessor {
    private final Initializers initializers = new Initializers();


    @Override
    public void process(ClassTreeTraverserContext context) {
        TraverserNode node = context.getCurrentNode();

        //TODO MM: decision whether to set primitive values, or all values or only null values
//            if (node.getValue() == null) {
        Initializer initializer = initializers.getSoleInitializer(node.getType(), node.getGenericType());

        Object newValue =
            initializer.getValue(node.getType(), node.getGenericType(), context);

        node.setValue(newValue);
//            }
    }
}
