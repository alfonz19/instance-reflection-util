package utils.traverser;

import java.util.Objects;

/**
 * Path represents 'path' along which we get from root Class (or instance), to currently processed Class (or instance)
 */
public interface Path {
    String PATH_SEPARATOR = ".";
    String ROOT_PATH = null;

    String getPathAsString();

    InstancePath createSubPath(TraverserNode node);

    default boolean isRootPath() {
        return getPathAsString() == null;
    }

    public static class InstancePath implements Path {

        private final String path;

        /**
         *  Creates root path;
         */
        public InstancePath() {
            this(ROOT_PATH);
        }

        private InstancePath(String path) {
            this.path = path;
        }

        @Override
        public String getPathAsString() {
            return path;
        }

        @Override
        public InstancePath createSubPath(TraverserNode node) {
            String nodeName = Objects.requireNonNull(node).getNodeName();
            if (Objects.requireNonNull(nodeName).contains(PATH_SEPARATOR)) {
                throw new IllegalArgumentException();
            }

            return new InstancePath(this.path == null ? nodeName : this.path + PATH_SEPARATOR + nodeName);
        }
    }
}
