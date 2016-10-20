package ca.waterloo.dsg.graphflow.util;

import java.util.Arrays;

/**
 * A list of int primitives represented by an array.
 */
public class IntArrayList implements IntList {

  public static final int INITIAL_CAPACITY = 2;
  private int[] data;
  private int size = 0;

  public IntArrayList() {
    data = new int[INITIAL_CAPACITY];
  }

  public IntArrayList(int capacity) {
    data = new int[capacity];
  }

  @Override
  public boolean add(int i) {
    ensure_capacity(size+1);
    data[size++] = i;
    this.sort();
    return true;
  }

  @Override
  public int get(int index) throws ArrayIndexOutOfBoundsException {
    return data[index];
  }

  @Override
  public boolean addAll(int[] i) {
    int numnew = i.length;
    ensure_capacity(size+numnew);
    System.arraycopy(i, 0, data, size, numnew);
    size += numnew;
    this.sort();
    return true;
  }

  @Override
  public int remove(int index) throws ArrayIndexOutOfBoundsException {
    int numMoved = size - index - 1;
    int valueToBeRemoved = data[index];
      if (numMoved > 0)
        System.arraycopy(data, index+1, data, index, numMoved);
    data[--size] = 0;
    this.sort();
    return valueToBeRemoved;
  }

  @Override
  public int size() { return size; }

  @Override
  public int[] toArray() {
    return Arrays.copyOfRange(data, 0, size);
  }

  /**
   * Sorts the underlying array inplace.
   */
  public void sort() {
    for(int i = 1; i < size; i++) {
      int temp = data[i];
      int j = i;

      while((j > 0) && (temp < data[j-1])) {
        data[j] = data[j-1];
        j--;
      }
      data[j] = temp;
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[");
    for(int i=0; i < size - 1; i++) {
      builder.append(data[i] + ", ");
    }
    if(size > 0) {
      builder.append(data[size-1]);
    }
    builder.append("]");
    return builder.toString();
  }
  /**
   * Checks if current capacity exceeds size and increases capacity if it doesn't.
   *
   * @param minCapacity
   */
  private void ensure_capacity(int minCapacity) {
    int oldCapacity = data.length;

    if (minCapacity > oldCapacity) {
      int newCapacity = (oldCapacity * 3) / 2 + 1;
      if (newCapacity < minCapacity)
        newCapacity = minCapacity;
      data = Arrays.copyOf(data, newCapacity);
    }
  }

  /**
   * Searches for the given value in the array and returns the index.
   *  Return value negative if value not found.
   * @param value
   * @return
   */
  public int search(int value) {
    int lowindex = 0, highIndex = this.size-1, result = -1;

    while(lowindex <= highIndex) {
      int mid = (lowindex + highIndex)/2;
      if(data[mid] == value) {
        result = mid;
        break;
      } else if(data[mid] < value) {
        lowindex = mid+1;
      } else {
        highIndex = mid-1;
      }
    }
    return result;
  }

  /**
   * Returns the intersection of sorted IntArrayLists this and newList as a new IntArrayList.
   * @param newList
   * @return
   */
  public IntArrayList getIntersection(IntArrayList newList) {
    IntArrayList shorter, longer, intersection;
    if (this.size() > newList.size()) {
      shorter = newList;
      longer = this;
    } else {
      shorter = this;
      longer = newList;
    }

    intersection = new IntArrayList(shorter.size());
    for(int i=0; i < shorter.size(); i++) {
      int resultIndex = longer.search(shorter.get(i));

      if(resultIndex >= 0) {
        intersection.add(shorter.get(i));
      }
    }
    return intersection;
  }
}
