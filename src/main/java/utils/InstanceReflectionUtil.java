package utils;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.initializers.Initializer;
import utils.traverser.FieldTraverser;
import utils.traverser.TraverserNode;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class InstanceReflectionUtil {


    public interface Processor {
        void process(TraverserNode node);
    }

    public static class InitializingProcessor implements Processor {
        private final Initializers initializers = new Initializers();


        @Override
        public void process(TraverserNode node) {
            //TODO MM: decision whether to set primitive values, or all values or only null values
//            if (node.getValue() == null) {
            Initializer initializer = initializers.getSoleInitializer(node.getType(), node.getGenericType());

            node.setValue(initializer.getValue(node.getType(), node.getGenericType(), node.getTraverser()));
//            }
        }
    }

}
