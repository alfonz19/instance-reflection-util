package utils;

import java.util.LinkedList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static utils.InstanceReflectionUtil.FieldTraverser;

public class InstanceReflectionUtilTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testBasicFieldsOverHierarchy()  {
        B instance = new B();
        B processed = processInstance(instance);


        assertThat(processed.i, notNullValue());
        assertThat(processed.ii, notNullValue());
    }

    @Test
    public void testBasicFieldsOverHierarchyWithStartClass()  {
        B instance = new B();
        B processed = processInstance(instance, A.class);


        assertThat(processed.i, notNullValue());
        assertThat(processed.ii, nullValue());
    }

    @Test
    public void testInitializingLists()  {
        C instance = new C();
        C processed = processInstance(instance);

        assertThat(processed.bList, notNullValue());
        assertThat(processed.bList.isEmpty(), is(false));

        for (B b : processed.bList) {
            assertThat(b.i, notNullValue());
            assertThat(b.ii, notNullValue());
        }
    }

    @Test
    public void testInitializingListsDeeply()  {
        C instance = new C();
        C processed = processInstance(instance);

        assertThat(processed.bList, notNullValue());
        assertThat(processed.bList.isEmpty(), is(false));
    }

    @Test
    public void testInitializingUntypedLists()  {
        ClassWithUntypedList instance = new ClassWithUntypedList();

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Unknown type of instances to be created.");

        processInstance(instance);
    }

    @Test
    public void testInitializingClassWithSpecificList()  {
        ClassWithSpecificList instance = new ClassWithSpecificList();

        ClassWithSpecificList actual = processInstance(instance);

        Class<?> aClass = actual.bList.getClass();
        assertEquals(aClass, LinkedList.class);
    }

    @Test
    public void testInitializingClassWithEnum()  {
        ClassWithEnum instance = new ClassWithEnum();

        ClassWithEnum actual = processInstance(instance);


        assertThat(actual.someEnum, notNullValue());
    }

    //--------------------------------------------------

    public static class A {
        public Integer i = null;
    }

    public static class B extends A{
        public Integer ii = null;

    }

    public static class C {
        public List<B> bList;
    }

    public static class ClassWithUntypedList {
        public List bList;
    }

    public static class ClassWithSpecificList {
        public LinkedList<B> bList;
    }

    public static class ClassWithExtendsList {//TODO MM: implement
        public LinkedList<? extends A> bList;
    }

    public static class ClassWithEnum {//TODO MM: implement
        public SomeEnum someEnum;
    }

    public static enum SomeEnum {
        A,B,C
    }

    //----------
    private <T> T processInstance(T instance) {
        new FieldTraverser<>(instance).accept(new InstanceReflectionUtil.InitializingTraverserNodeProcessor());
        return instance;
    }

    private <T> T processInstance(T instance, Class<?> startClass) {
        new FieldTraverser<>(instance, startClass)
            .accept(new InstanceReflectionUtil.InitializingTraverserNodeProcessor());
        return instance;
    }

}
