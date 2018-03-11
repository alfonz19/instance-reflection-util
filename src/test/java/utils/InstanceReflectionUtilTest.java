package utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import utils.InstanceReflectionUtil.FieldTraverser;
import utils.InstanceReflectionUtil.InitializingProcessor;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("Duplicates")
public class InstanceReflectionUtilTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private FieldTraverser traverser = new FieldTraverser(new InitializingProcessor());

    @Test
    public void testBasicFieldsOverHierarchy()  {
        B instance = new B();
        B processed = traverser.process(instance);


        assertThat(processed.i, notNullValue());
        assertThat(processed.ii, notNullValue());
    }

    @Test
    public void testBasicFieldsOverHierarchyWithStartClass()  {
        B instance = new B();
        B processed = traverser.process(instance, A.class);


        assertThat(processed.i, notNullValue());
        assertThat(processed.ii, nullValue());
    }

    @Test
    public void testInitializingLists()  {
        C instance = new C();
        C processed = traverser.process(instance);

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
        C processed = traverser.process(instance);

        assertThat(processed.bList, notNullValue());
        assertThat(processed.bList.isEmpty(), is(false));
    }

    @Test
    public void testInitializingUntypedLists()  {
        ClassWithUntypedList instance = new ClassWithUntypedList();

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Unknown type of instances to be created.");

        traverser.process(instance);
    }

    @Test
    public void testInitializingClassWithSpecificList()  {
        ClassWithSpecificList instance = new ClassWithSpecificList();

        ClassWithSpecificList actual = traverser.process(instance);

        Class<?> aClass = actual.bList.getClass();
        assertEquals(aClass, LinkedList.class);
    }

    @Test
    public void testInitializingClassWithSet()  {
        ClassWithSet instance = new ClassWithSet();

        ClassWithSet actual = traverser.process(instance);

        Set<A> set = actual.set;
        assertThat(set, notNullValue());
        assertThat(set.isEmpty(), is(false));
        for (A a : set) {
            assertThat(a, notNullValue());
        }
    }

    @Test
    public void testInitializingClassWithArray()  {
        ClassWithArray instance = new ClassWithArray();

        ClassWithArray actual = traverser.process(instance);

        A[] arr = actual.set;
        assertThat(arr, notNullValue());
        assertThat(arr.length > 0, is(true));
        for (A a : arr) {
            assertThat(a, notNullValue());
        }
    }

    @Test
    public void testInitializingClassWithListsOfArrays()  {
        ClassWithListsOfArray instance = new ClassWithListsOfArray();
        ClassWithListsOfArray actual = traverser.process(instance);

        List<A[]> listOfArrays = actual.listOfArrays;

        assertThat(listOfArrays, notNullValue());
        assertThat(listOfArrays.isEmpty(), is(false));

        for (A[] arr : listOfArrays) {
            assertThat(arr, notNullValue());
            assertTrue(arr.length > 0);
            for (A a : arr) {
                assertThat(a, notNullValue());
                assertThat(a.i, notNullValue());
            }
        }

    }

    @Test
    public void testInitializingClassWithArraysOfLists()  {
        ClassWithArrayOfLists instance = new ClassWithArrayOfLists();

        ClassWithArrayOfLists classWithArrayOfLists = traverser.process(instance);

        List<A>[] arrayOfLists = classWithArrayOfLists.arrayOfLists;

        assertThat(arrayOfLists, notNullValue());
        assertTrue(arrayOfLists.length > 0);

        for (List<A> list : arrayOfLists) {
            assertThat(list, notNullValue());
            assertThat(list.isEmpty(), is(false));
            for (A a : list) {
                assertThat(a, notNullValue());
                assertThat(a.i, notNullValue());
            }
        }
    }

    @Test
    public void testInitializingClassWithListOfLists()  {
        ClassWithListsOfLists instance = new ClassWithListsOfLists();

        ClassWithListsOfLists classWithListsOfLists = traverser.process(instance);
        List<List<A>> listOfLists = classWithListsOfLists.listOfLists;

        assertThat(listOfLists, notNullValue());
        assertThat(listOfLists.isEmpty(), is(false));

        for (List<A> list : listOfLists) {
            assertThat(list, notNullValue());
            assertThat(list.isEmpty(), is(false));
            for (A a : list) {
                assertThat(a, notNullValue());
                assertThat(a.i, notNullValue());
            }
        }
    }

    @Test
    public void testInitializingClassWithEnum()  {
        ClassWithEnum instance = new ClassWithEnum();

        ClassWithEnum actual = traverser.process(instance);


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

    public static class ClassWithEnum {
        public SomeEnum someEnum;
    }

    public static class ClassWithSet {
        public Set<A> set;
    }

    public static class ClassWithArray {
        public A[] set;
    }

    public static class ClassWithArrayOfLists {
        public List<A>[] arrayOfLists;
    }

    public static class ClassWithListsOfArray {
        public List<A[]> listOfArrays;
    }

    public static class ClassWithListsOfLists {
        public List<List<A>> listOfLists;
    }

    public enum SomeEnum {
        A,B,C
    }

}
