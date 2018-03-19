package utils;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
import utils.initializers.UuidInitializer;
import utils.traverser.ClassTreeTraverserContext;

public class Initializers {
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

                new DefaultConstructorInitializer());

        result.forEach(e->e.setInitializers(this));
        return result;
    }

    public Initializer getSoleInitializer(Class<?> type, Type genericType) {
        List<Initializer> suitableInitializers = initializers.stream()
                .filter(e -> e.canProvideValueFor(type, genericType))
                .collect(Collectors.toList());


        //TODO MM: allow to configure.
        if (suitableInitializers.isEmpty()) {
            throw new IllegalStateException("Unknown initializer for type: " + genericType.getTypeName());
        }

//            if (suitableInitializers.size() > 1) {
//                throw new IllegalStateException("Multiple initializers for type: " + genericType.getTypeName());
//            }

        Initializer initializer = suitableInitializers.get(0);
        return initializer;
    }

    public Object generateValue(Type genericType, ClassTreeTraverserContext context) {
        Class<?> classType = GenericTypeUtil.getClassType(genericType); //TODO MM: check if we can calculate classType from genericType properly!
        return generateValue(classType, genericType, context);
    }

    public Object generateValue(Class<?> type,
                                Type genericType,
                                ClassTreeTraverserContext context) {
        Initializer initializer = getSoleInitializer(type, genericType);

        return initializer.getValue(type, genericType, context);
    }
}
