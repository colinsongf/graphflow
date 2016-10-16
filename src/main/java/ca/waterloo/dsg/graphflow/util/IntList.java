package ca.waterloo.dsg.graphflow.util;

/**
 * Interface for a list of integers.
 */
public interface IntList {

  /**
   * Inserts the given int to the end of the array and returns the index.
   * @param i
   * @return boolean
   */
  boolean add(int i);

  /**
   * Gets the element at the given index.
   * @param index
   * @throws ArrayIndexOutOfBoundsException
   * @return
   */
  int get(int index) throws ArrayIndexOutOfBoundsException;

  /**
   * Appends the given array to the list and returns index of first entry
   * @param i
   * @return boolean
   */
  boolean addAll(int[] i);

  /**
   *
   * @param index
   * @return
   * @throws ArrayIndexOutOfBoundsException
   */
  int remove(int index) throws ArrayIndexOutOfBoundsException;

  /**
   * Returns the current size of the list
   * @return int
   */
  int size();

  /**
   * Returns the array underlying the list
   * @return int[]
   */
  int[] toArray();

}
