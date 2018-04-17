package utils.traverser;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents two dimensional state in processing Classes/instances. Identifies position from root Class/instance and place in class hierarchy of that Class/instance.
 */
public class PathNode {
    private final Logger logger = LoggerFactory.getLogger(PathNode.class);

    private final PathNode previousPathNode;
    private final Path path;
    private final TraverserNode traverserNode;
    private final Map<TypeVariable, Type> typeVariableMap = new HashMap<>();

    /**
     * Denotes at which Class in traverserNode.getDeclaringClass class hierarchy we should do upcoming processing.
     */
    private Class<?> processTraverserNodeAtClass;
    private final boolean rootNode;

    /**
     * creates PathNode for {@link TraverserNode} defined in root Class/instance. Ie. the Class/instance where traverse starts.
     */
    public PathNode(Path path) {
        this(requireRootPath(Objects.requireNonNull(path)),
            null,
            null,
            null,
            true);
    }

    private static Path requireRootPath(Path path) {
        if (!path.isRootPath()) {
            throw new IllegalArgumentException();
        }

        return path;
    }

    /**
     * creates PathNode for {@link TraverserNode} NOT defined in root Class/instance.
     */
    public PathNode(PathNode previousPathNode, TraverserNode traverserNode) {
        this(Objects.requireNonNull(previousPathNode).getPath().createSubPath(Objects.requireNonNull(traverserNode)),
            previousPathNode,
            traverserNode,
            traverserNode.getDeclaringClass(),
            false);
    }

    private PathNode(Path path,
                    PathNode previousPathNode,
                    TraverserNode traverserNode,
                    Class<?> processTraverserNodeAtClass,
                     boolean rootNode) {
        this.previousPathNode = previousPathNode;
        this.path = path;
        this.traverserNode = traverserNode;
        this.processTraverserNodeAtClass = processTraverserNodeAtClass;
        this.rootNode = rootNode;
    }

    public boolean hasPreviousPathNode() {
        return previousPathNode != null/* && !previousPathNode.rootNode*/;
    }
    
    public PathNode getPreviousPathNode() {
        if (!hasPreviousPathNode()) {
            throw new IllegalStateException("There isn't previous PathNode");
        }
        
        return this.previousPathNode;
    }

    public Path getPath() {
        return path;
    }

    public TraverserNode getTraverserNode() {
        return traverserNode;
    }

    public Optional<Type> getTypeOfTypeVariable(TypeVariable typeVariable) {
        if (typeVariableMap == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(typeVariableMap.get(typeVariable));
    }

    public void setTypeOfTypeVariable(TypeVariable typeVariable, Type type) {
        Optional<Type> optional = getTypeOfTypeVariable(typeVariable);
        if (optional.isPresent()) {
            if (!optional.get().equals(type)) {
                logger.warn(
                    "Trying to set type variable {} to {}, but this varible is already resolved as {}. Ignoring request.",
                    typeVariable,
                    type,
                    optional.get());
            }
        } else {
            typeVariableMap.put(typeVariable, type);
        }
    }

    public Class<?> getProcessTraverserNodeAtClass() {
        return processTraverserNodeAtClass;
    }

    public void setProcessTraverserNodeAtClass(Class<?> newProcessTraverserNodeAtClass) {
        if (!newProcessTraverserNodeAtClass.isAssignableFrom(getProcessTraverserNodeAtClass().getClass())) {
            throw new IllegalArgumentException("processTraverserNodeAtClass can be set only to some superClass of declaring class. Once set, it can be again only reset to super class of previously set value");
        }
        
        this.processTraverserNodeAtClass = newProcessTraverserNodeAtClass;
    }

    public boolean isRootNode() {
        return rootNode;
    }
}
