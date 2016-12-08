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
     * @param a One of the objects.
     * @param b The other object.
     * @return {@code true} if the {@code a} object values are the same as the
     * {@code b} object values, {@code false} otherwise.
     */
    @ExistsForTesting
    public static boolean isSameAs(InMemoryOutputSink a, InMemoryOutputSink b) {
        if (a == b) {
            return true;
        }
        if (null == a || null == b) {
            return false;
        }
        return Objects.equals(a.results, b.results);
    }
}
