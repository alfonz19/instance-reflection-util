package utils;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.initializers.ArrayInitializer;
import utils.initializers.BooleanInitializer;
import utils.initializers.CollectionOrIterableInitializer;
import utils.initializers.DefaultConstructorInitializer;
import utils.initializers.EnumInitializer;
import utils.initializers.Initializer;
import utils.initializers.IntInitializer;
import utils.initializers.JavaUtilDateInitializer;
import utils.initializers.ListInitializer;
import utils.initializers.MapInitializer;
import utils.initializers.SetInitializer;
import utils.initializers.StringInitializer;
import utils.initializers.TypeVariableInitializer2;
import utils.initializers.UuidInitializer;
import utils.traverser.ClassTreeTraverserContext;
import utils.traverser.PathNode;

public class Initializers {
    private final Logger logger = LoggerFactory.getLogger(Initializers.class);

    private List<Initializer> initializers = createInitializers();

    private List<Initializer> createInitializers() {
        List<Initializer> result = Arrays.asList(
                new ListInitializer(),
                new SetInitializer(),
                new ArrayInitializer(),
                new MapInitializer(),
                new CollectionOrIterableInitializer(),

                new BooleanInitializer(),
                new JavaUtilDateInitializer(),
                new UuidInitializer(),
                new IntInitializer(),
                new StringInitializer(),
                new EnumInitializer(),

                new TypeVariableInitializer2(),
                new DefaultConstructorInitializer());

        result.forEach(e->e.setInitializers(this));
        return result;
    }

    public Initializer getSoleInitializer(Class<?> type, Type genericType, PathNode pathNode) {
        logger.debug("\n\nLooking for sole initializer for:\n\ttype={},\n\tgenericType={}\n\tat path={}", type, genericType, pathNode.getPath());
        List<Initializer> suitableInitializers = initializers.stream()
                .filter(e -> e.canProvideValueFor(type, genericType, pathNode))
                .collect(Collectors.toList());


        //TODO MM: allow to configure.
        if (suitableInitializers.isEmpty()) {
            throw new InstanceReflectionUtilException("Unknown initializer for type: " + genericType.getTypeName());
        }


        if (suitableInitializers.size() > 1) {
            logger.debug("Found multiple initializers for type={}, genericType={} at path={}", type, genericType, pathNode.getPath());
            //    throw new IllegalStateException("Multiple initializers for type: " + genericType.getTypeName());  //TODO MM: exception?
        }

        Initializer initializer = suitableInitializers.get(0);
        logger.debug("Using initializer {} for:\n\t type={},\n\t genericType={}\n\t at path={}", initializer.getClass(), type, genericType, pathNode.getPath());
        return initializer;
    }

    public Object generateValue(Type genericType, PathNode pathNode, ClassTreeTraverserContext context) {
        Class<?> classType = GenericTypeUtil.getClassType(genericType); //TODO MM: check if we can calculate classType from genericType properly!
        return generateValue(classType, genericType, pathNode, context);
    }

    public Object generateValue(Class<?> type,
                                Type genericType,
                                PathNode pathNode,
                                ClassTreeTraverserContext context) {
        Initializer initializer = getSoleInitializer(type, genericType, pathNode);

        return initializer.getValue(type, genericType, pathNode, context);
    }
}
