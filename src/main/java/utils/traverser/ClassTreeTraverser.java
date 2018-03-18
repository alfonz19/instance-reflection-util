package utils.traverser;

public interface ClassTreeTraverser {

    <T> T process(T instance);

    <T> T process(T instance, Class<?> instanceClass); //TODO MM: rename instanceClass to startClass
}
