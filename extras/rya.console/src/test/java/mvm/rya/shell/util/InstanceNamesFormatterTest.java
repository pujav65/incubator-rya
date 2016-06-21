package mvm.rya.shell.util;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.beust.jcommander.internal.Lists;

/**
 * Tests an instance of {@link InstanceNamesFormatter}.
 */
public class InstanceNamesFormatterTest {

    @Test
    public void format_withConnectedName() {
        final List<String> instanceNames = Lists.newArrayList("a", "b", "c", "d");

        final String formatted = new InstanceNamesFormatter().format(instanceNames, "c");

        final String expected =
                "Rya instance names:\n" +
                "   a\n" +
                "   b\n" +
                " * c\n" +
                "   d\n";

        assertEquals(expected, formatted);
    }

    @Test
    public void format_connectedNameNotInList() {
        final List<String> instanceNames = Lists.newArrayList("a", "b", "c", "d");

        final String formatted = new InstanceNamesFormatter().format(instanceNames, "not_in_list");

        final String expected =
                "Rya instance names:\n" +
                "   a\n" +
                "   b\n" +
                "   c\n" +
                "   d\n";

        assertEquals(expected, formatted);
    }

    @Test
    public void format() {
        final List<String> instanceNames = Lists.newArrayList("a", "b", "c", "d");

        final String formatted = new InstanceNamesFormatter().format(instanceNames);

        final String expected =
                "Rya instance names:\n" +
                "   a\n" +
                "   b\n" +
                "   c\n" +
                "   d\n";

        assertEquals(expected, formatted);
    }
}