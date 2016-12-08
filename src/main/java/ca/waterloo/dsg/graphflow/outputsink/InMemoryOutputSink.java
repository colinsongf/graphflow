package ca.waterloo.dsg.graphflow.outputsink;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Stores the output as an in memory data structure in the form of a list of {@code Strings}s.
 */
public class InMemoryOutputSink implements OutputSink {

    private List<String> results = new ArrayList<>();

    /**
     * Adds {@code result} to the list of in-memory outputs.
     *
     * @param result the output {@code String}.
     */
    @Override
    public void append(String result) {
        results.add(result);
    }

    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());
        for (String result : results) {
            stringJoiner.add(result);
        }
        return stringJoiner.toString();
    }

    /**
     * Used for unit testing. It simulates the functionality of the {@code equals()} method
     * without overriding the actual equals() and hashCode() methods.
     *
     * @param o The expected object.
     * @return {@code true} if the current object values match perfectly with the expected object
     * values, {@code false} otherwise.
     */
    public boolean isSameAs(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InMemoryOutputSink that = (InMemoryOutputSink) o;
        return Objects.equals(results, that.results);
    }
}
