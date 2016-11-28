package ca.waterloo.dsg.graphflow.outputsink;

import ca.waterloo.dsg.graphflow.util.ExistsForTesting;

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
     * Used during unit testing to check the equality of objects. This is used instead of
     * overriding the standard {@code equals()} and {@code hashCode()} methods.
     *
     * @param a The actual object.
     * @param b The expected object.
     * @return {@code true} if the {@code actual} object values are the same as the
     * {@code expected} object values, {@code false} otherwise.
     */
    @ExistsForTesting
    public static boolean isSameAs(InMemoryOutputSink a, InMemoryOutputSink b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return Objects.equals(a.results, b.results);
    }
}
