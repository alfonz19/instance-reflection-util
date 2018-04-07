package utils.traverser;

public interface ClassTreeTraverser {

    //TODO MM: verify, after Stream support, if we actually can return value here.
    <T> T process(T instance); //TODO MM: rename process to traverse?

    <T> T process(T instance, PathNode pathNode, ClassTreeTraverserContext context);

    <T> T process(T instance, Class<?> startClass);

    <T> T process(T instance, Class<?> startClass, PathNode pathNode, ClassTreeTraverserContext context);
}
