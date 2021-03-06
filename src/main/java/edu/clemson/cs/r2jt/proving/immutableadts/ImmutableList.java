package edu.clemson.cs.r2jt.proving.immutableadts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import edu.clemson.cs.r2jt.proving.ArrayIterator;

public class ImmutableList<E> extends AbstractImmutableList<E> {

    private final E[] myElements;
    private final int myElementsLength;

    @SuppressWarnings("unchecked")
    public ImmutableList(Iterable<E> i) {
        List<E> tempList = new ArrayList<E>();

        for (E e : i) {
            tempList.add(e);
        }

        myElements = (E[]) tempList.toArray();
        myElementsLength = myElements.length;
    }

    public ImmutableList(E[] i) {
        myElementsLength = i.length;
        myElements = Arrays.copyOf(i, myElementsLength);
    }

    public ImmutableList(E[] i, int length) {
        myElementsLength = length;
        myElements = Arrays.copyOf(i, length);
    }

    @Override
    public E get(int index) {
        return myElements[index];
    }

    @Override
    public SimpleImmutableList<E> head(int length) {
        return new ImmutableListSubview<E>(this, 0, length);
    }

    @Override
    public Iterator<E> iterator() {
        return new ArrayIterator<E>(myElements);
    }

    public Iterator<E> subsequenceIterator(int start, int length) {
        return new ArrayIterator<E>(myElements, start, length);
    }

    @Override
    public int size() {
        return myElementsLength;
    }

    @Override
    public SimpleImmutableList<E> tail(int startIndex) {
        return new ImmutableListSubview<E>(this, startIndex, myElementsLength
                - startIndex);
    }
}
