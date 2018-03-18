package utils.traverser;

public interface ClassTreeTraverser {

    <T> T process(T instance);

    <T> T process(T instance, Class<?> startClass);
}
