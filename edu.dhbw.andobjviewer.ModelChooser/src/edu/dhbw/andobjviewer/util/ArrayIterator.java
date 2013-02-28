package edu.dhbw.andobjviewer.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayIterator implements Iterator<Object> {
	private Object array[];
	private int pos = 0;

	public ArrayIterator(Object anArray[]) {
		array = anArray;
	}
	public ArrayIterator() {
		
	}

	public boolean hasNext() {
		return pos < array.length;
	}

	public Object next() throws NoSuchElementException {
		if (hasNext())
			return array[pos++];
		else
			throw new NoSuchElementException();
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
	public static boolean isAnynull(Object arrays[]){
		
		int temppos=0;
		
		boolean flag=false;
		for(;temppos<arrays.length;temppos++)
		{
			if(arrays[temppos]==null)
				flag=true;
		}
		
		
		return flag;
	}
	
	
}