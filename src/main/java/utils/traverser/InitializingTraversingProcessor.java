package utils.traverser;

import utils.Initializers;

public class InitializingTraversingProcessor implements TraversingProcessor {
    private final Initializers initializers = new Initializers();


    @Override
    public void process(ModifiableTraverserNode modifiableNode, ClassTreeTraverserContext context) {
        TraverserNode node = context.getCurrentNode();

        //TODO MM: decision whether to set primitive values, or all values or only null values
//            if (node.getValue() == null) {
        Object newValue = initializers.generateValue(node.getType(), node.getGenericType(), context);

        modifiableNode.setValue(newValue);
//            }
    }
}
