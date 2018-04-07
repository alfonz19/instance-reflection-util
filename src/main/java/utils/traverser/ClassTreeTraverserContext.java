package utils.traverser;

/**
 *
 * Class knows (internally) reference to used {@link ClassTreeTraverser}, and one can use method {@link #processCurrentNodeInstance} to instruct traverser to process given instance. Traverser does not delve
 * automatically into associations, because given nodes need not to be initialized at the time of execution. Moreover,
 * they can be 'process-once' associations like Streams, which complicates things a little. So if working with null
 * valued nodes, one can create instance, and then ask for traversing it, or creates items for stream, ask to traverse all of them, and then create stream instance from them.
 *
 * Class has to be unmodifiable! Class should not give direct access to traverser instance.
 * */
public class ClassTreeTraverserContext {
    private final ClassTreeTraverser classTreeTraverser;

    public ClassTreeTraverserContext(ClassTreeTraverser classTreeTraverser) {
        this.classTreeTraverser = classTreeTraverser;
    }

    public Object processCurrentNodeInstance(Object instance, PathNode pathNode) {
        return classTreeTraverser.process(instance, pathNode, this);
    }
}
