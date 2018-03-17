package utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import utils.traverser.FieldTraverser;
import utils.InstanceReflectionUtil.InitializingProcessor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThan;
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
    public void testClassWithList()  {
        assertThat(traverser.process(new ClassWithList()).list, notNullValue());

        //iterable does not know about size/emptiness.
        int itemCount = 0;
        for (B b : traverser.process(new ClassWithList()).list) {
            itemCount++;
            assertThat(b.i, notNullValue());
            assertThat(b.ii, notNullValue());
        }
        assertThat(itemCount, is(not(0)));
    }

    @Test
    public void testClassWithCollection()  {
        assertThat(traverser.process(new ClassWithCollection()).collection, notNullValue());

        //iterable does not know about size/emptiness.
        int itemCount = 0;
        for (B b : traverser.process(new ClassWithCollection()).collection) {
            itemCount++;
            assertThat(b.i, notNullValue());
            assertThat(b.ii, notNullValue());
        }
        assertThat(itemCount, is(not(0)));
    }

    @Test
    public void testClassWithIterable()  {
        assertThat(traverser.process(new ClassWithIterable()).iterable, notNullValue());

        //iterable does not know about size/emptiness.
        int itemCount = 0;
        for (B b : traverser.process(new ClassWithIterable()).iterable) {
            itemCount++;
            assertThat(b.i, notNullValue());
            assertThat(b.ii, notNullValue());
        }
        assertThat(itemCount, is(not(0)));
    }

    @Test
    public void testInitializingListsDeeply()  {
        ClassWithList instance = new ClassWithList();
        ClassWithList processed = traverser.process(instance);

        assertThat(processed.list, notNullValue());
        assertThat(processed.list.isEmpty(), is(false));
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
    public void testInitializingClassWithSpecificSet()  {
        ClassWithSpecificSet instance = new ClassWithSpecificSet();

        ClassWithSpecificSet actual = traverser.process(instance);

        Class<?> aClass = actual.set.getClass();
        assertEquals(aClass, HashSet.class);
    }

    @Test
    public void testInitializingClassWithExtendsList()  {
        ClassWithExtendsList instance = new ClassWithExtendsList();

        ClassWithExtendsList actual = traverser.process(instance);

//        Class<?> aClass = actual.bList.getClass();
        assertThat(actual.bList, CoreMatchers.isA(List.class));
//        assertEquals(aClass, List.class);
        assertThat(actual.bList.isEmpty(), is(false));

        for (A a : actual.bList) {
            assertThat(a, notNullValue());
        }

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

    @Test
    public void testClassWithMap()  {
        ClassWithMap process = traverser.process(new ClassWithMap());
        Map<String, String> map = process.map;
        assertThat(map, notNullValue());

        assertThat(map.isEmpty(), is(false));

        for (Map.Entry<String, String> e : map.entrySet()) {
            assertThat(e.getKey(), notNullValue());
            assertThat(e.getValue(), notNullValue());
        }
    }

    @Test
    public void testClassWithMapWithArrays()  {
        ClassWithMapWithArrays process = traverser.process(new ClassWithMapWithArrays());
        HashMap<Integer[], List<String>[]> map = process.map;
        assertThat(map, notNullValue());

        assertThat(map.isEmpty(), is(false));

        for (Map.Entry<Integer[], List<String>[]> e : map.entrySet()) {
            Integer[] key = e.getKey();
            assertThat(key, notNullValue());
            assertThat(key.length, greaterThan(0));
            for (Integer intVal : key) {
                assertThat(intVal, notNullValue());
            }

            List<String>[] value = e.getValue();
            assertThat(value, notNullValue());
            assertThat(value.length, greaterThan(0));
            for (List<String> listOfStrings : value) {
                assertThat(listOfStrings, notNullValue());
                assertThat(listOfStrings.isEmpty(), is(false));
                for (String str : listOfStrings) {
                    assertThat(str, notNullValue());
                }
            }

        }
    }
    //--------------------------------------------------

    public static class A {
        public Integer i = null;
    }

    public static class B extends A{
        public Integer ii = null;

    }

    public static class ClassWithList {
        public List<B> list;
    }

    public static class ClassWithCollection {
        public Collection<B> collection;
    }

    public static class ClassWithIterable {
        public Iterable<B> iterable;
    }

    public static class ClassWithMap {
        public Map<String, String> map;
    }

    public static class ClassWithMapWithArrays {
        public HashMap<Integer[], List<String>[]> map;
    }

    public static class ClassWithUntypedList {
        public List bList;
    }

    public static class ClassWithSpecificList {
        public LinkedList<B> bList;
    }

    public static class ClassWithSpecificSet {
        public HashSet<B> set;
    }

    public static class ClassWithExtendsList {
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
