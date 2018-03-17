package utils;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GenericTypeUtil {
    private static final Logger log = LogManager.getLogger(GenericTypeUtil.class);

    public static Class<?> getClassType(Type genericType) {
        if (genericType instanceof Class) {
            return (Class) genericType;
        } else if (genericType instanceof ParameterizedType) {
            return (Class)((ParameterizedType)genericType).getRawType();
        } else if (genericType instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) genericType;
            Type genericComponentType = genericArrayType.getGenericComponentType();
            if (genericComponentType instanceof ParameterizedType) {
                //hack to get classType of Array from generic type. There's no way how to do that
                //so we create here new array based on generic type and use that instance to get class type.
                //this is far from ideal, thus one should avoid using this method if possible for finding out
                //class type of array from generic type.
                log.warn("Used inefficient query to get array class type.");
                return Array.newInstance((Class) ((ParameterizedType) genericArrayType.getGenericComponentType()).getRawType(),
                        0).getClass();
            } else {
                throw new UnsupportedOperationException("Not implemented yet");
            }
        } else if (genericType instanceof WildcardType) {
            //solves only ? extends ...
            WildcardType wildcardType = (WildcardType) genericType;
            Type[] upperBounds = wildcardType.getUpperBounds();
            return (Class) upperBounds[0];  //TODO MM: recursion?
        } else {
            throw new UnsupportedOperationException("Not implemented yet");
        }
    }

    public static boolean isArray(Type genericType) {
        if (genericType instanceof Class) {
            return ((Class)genericType).isArray();
        } else if (genericType instanceof GenericArrayType) {
            return true;
        } else {
            return false;
        }
    }

    public static Type getTypeOfArrayElements(Type genericType) {
        if (genericType instanceof Class) {
            return ((Class)genericType).getComponentType();
        } else if (genericType instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) genericType;
            return genericArrayType.getGenericComponentType();
        } else {
            throw new UnsupportedOperationException("Not implemented yet");
        }
    }

    public static Type typeOfListSetElements(Type genericType) {
        if (genericType instanceof Class) {
            throw new RuntimeException("Unknown type of instances to be created.");
        } else if (genericType instanceof ParameterizedType) {

            ParameterizedType parameterizedType = (ParameterizedType) genericType;

            //lists have just one type parameter. //TODO MM: reuse for maps etc.
            return parameterizedType.getActualTypeArguments()[0];
        } else if (genericType instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) genericType;
            return genericArrayType.getGenericComponentType();
        } else {
            throw new RuntimeException("Unknown type of instances to be created.");
        }
    }
}
