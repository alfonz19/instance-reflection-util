package utils.traverser;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ClassTreeTraverserContext encapsulates traverse data over associations, can provide information about association
 * path etc. Can provide access to current TraverserNode, but also information about nodes up the path, the nodes we
 * had to traverse to get to the current node.
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
    /**
     * List of TraverserNode instances, from traverse root to current node.
     *
     * If list is empty, it means, that no node is being processed, and traverse did not start yet.
     * */
    private List<TraverserNode> nodesFromRoot;

    //creates root, empty context
    public ClassTreeTraverserContext(ClassTreeTraverser classTreeTraverser) {
        this(classTreeTraverser, new LinkedList<>());
    }

    private ClassTreeTraverserContext(ClassTreeTraverser classTreeTraverser, List<TraverserNode> nodesFromRoot) {
        this.classTreeTraverser = classTreeTraverser;
        this.nodesFromRoot = Collections.unmodifiableList(nodesFromRoot);
    }

    public ClassTreeTraverserContext subNode(TraverserNode node) {
        List<TraverserNode> newNodesFromRoot =
            Stream.concat(nodesFromRoot.stream(), Stream.of(node))
                .collect(Collectors.toList());

        return new ClassTreeTraverserContext(classTreeTraverser, newNodesFromRoot);
    }

    public List<TraverserNode> getNodesFromRoot() {
        return this.nodesFromRoot;
    }

    public String getNodesFromRootAsNamesPath() {
        return getNodesFromRoot().stream().map(TraverserNode::getNodeName).collect(Collectors.joining("."));
    }

    public TraverserNode getCurrentNode() {
        if (this.nodesFromRoot.isEmpty()) {
            throw new IllegalStateException("Cannot get current node, when there's no current node. Traverse did not start yet?");
        }

        return nodesFromRoot.get(nodesFromRoot.size() - 1);
    }

    public Object processCurrentNodeInstance(Object instance) {
        return classTreeTraverser.process(instance, this);
    }
}
