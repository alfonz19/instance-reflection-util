package utils.traverser;

public interface TraversingProcessor {
    void process(ModifiableTraverserNode modifiableTraverserNode, PathNode pathNode, ClassTreeTraverserContext context);
}
