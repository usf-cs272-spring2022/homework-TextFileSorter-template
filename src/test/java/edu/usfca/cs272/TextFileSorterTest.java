package edu.usfca.cs272;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import edu.usfca.cs272.TextFileSorter.TextFile;

/**
 * Tests for the {@link TextFileSorter} class.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2022
 */
@TestMethodOrder(MethodName.class)
public class TextFileSorterTest {
	/** Location of test files. */
	public static final Path BASE = Path.of("src", "test", "resources");

	/**
	 * Tests a specific comparator.
	 *
	 * @see TextFileSorter.TextFile
	 */
	@Nested
	public class A_PathTests extends ComparatorTests {
		@Override
		@BeforeEach
		public void setup() {
			// can't use Comparator.naturalOrder() here because not guaranteed interface implemented correctly
			Comparator<TextFileSorter.TextFile> temp = new Comparator<TextFileSorter.TextFile>() {
				@Override
				public int compare(TextFileSorter.TextFile first, TextFileSorter.TextFile second) {
					try {
						// See a warning? Then your TextFile does not implement the interface correctly!
						Comparable<TextFileSorter.TextFile> casted1 = (Comparable<TextFile>) first;
						return casted1.compareTo(second);
					}
					catch (ClassCastException e) {
						// fail test because unable to cast
						fail("\nTextFile does not implement the necessary interface.");
					}

					return 0;
				}
			};

			test = temp;
			small = Path.of("a", "z.txt");
			large = Path.of("z", "a.txt");
			equal = Path.of(".", "z", "a.txt");
		}
	}

	/**
	 * Tests a specific comparator.
	 *
	 * @see TextFileSorter#DATE_COMPARATOR
	 */
	@Nested
	public class B_DateTests extends ComparatorTests {
		@Override
		@BeforeEach
		public void setup() throws IOException {
			test = TextFileSorter.DATE_COMPARATOR;
			small = BASE.resolve("a_large_size.txt");
			large = BASE.resolve("z_small_size.txt");
			equal = BASE.resolve("a_equal_size.txt");

			Instant now = Instant.now();
			Instant past = now.minus(Duration.ofHours(1));

			Files.setLastModifiedTime(small, FileTime.from(past));
			Files.setLastModifiedTime(large, FileTime.from(now));
			Files.setLastModifiedTime(equal, FileTime.from(now));
		}
	}

	/**
	 * Tests a specific comparator.
	 *
	 * @see TextFileSorter#NAME_COMPARATOR
	 */
	@Nested
	public class C_NameTests extends ComparatorTests {
		@Override
		@BeforeEach
		public void setup() {
			test = TextFileSorter.NAME_COMPARATOR;
			small = Path.of("z", "a.txt");
			large = Path.of("a", "z.txt");
			equal = Path.of("Z.TXT");
		}
	}

	/**
	 * Tests a specific comparator.
	 *
	 * @see TextFileSorter#SIZE_COMPARATOR
	 */
	@Nested
	public class D_SizeTests extends ComparatorTests {
		@Override
		@BeforeEach
		public void setup() {
			test = TextFileSorter.SIZE_COMPARATOR; 	// descending sort order
			small = BASE.resolve("a_large_size.txt");
			large = BASE.resolve("z_small_size.txt");
			equal = BASE.resolve("a_equal_size.txt");
		}
	}

	/**
	 * Tests a specific comparator.
	 *
	 * @see TextFileSorter#DATE_COMPARATOR
	 */
	@Nested
	public class E_NestedTests extends ComparatorTests {
		@Override
		@BeforeEach
		public void setup() {
			test = TextFileSorter.getNested();

			// handles case where sizes are different
			small = BASE.resolve("a_large_size.txt");
			large = BASE.resolve("z_small_size.txt");

			// handles case where size, name, and path are same
			equal = BASE.resolve(".").resolve("z_small_size.txt");
		}

		/**
		 * Tests that this comparator was implemented properly.
		 * @throws IOException if I/O error occurs
		 */
		@Test
		@Order(4)
		public void testSameSizeUniqueName() throws IOException {
			// paths don't exist, so sizes are all -1
			small = Path.of("z", "a.txt");
			large = Path.of("a", "z.txt");

			TextFileSorter.TextFile one = new TextFileSorter.TextFile(large);
			TextFileSorter.TextFile two = new TextFileSorter.TextFile(small);
			String debug = String.format("%s > %s", one, two);
			assertTrue(test.compare(one, two) > 0, debug);
		}

		/**
		 * Tests that this comparator was implemented properly.
		 * @throws IOException if I/O error occurs
		 */
		@Test
		@Order(5)
		public void testSameSizeSameName() throws IOException {
			// paths don't exist, so sizes are all -1
			small = Path.of("a", "a.txt");
			large = Path.of("z", "a.txt");

			TextFileSorter.TextFile one = new TextFileSorter.TextFile(large);
			TextFileSorter.TextFile two = new TextFileSorter.TextFile(small);
			String debug = String.format("%s > %s", one, two);
			assertTrue(test.compare(one, two) > 0, debug);
		}
	}

	/**
	 * Tests approaches used.
	 *
	 * @see TextFileSorter
	 */
	@Nested
	public class F_ApproachTests {
		/**
		 * Tests approach.
		 */
		@Test
		@Order(1)
		public void testTextFileInterface() {
			Type[] types = TextFileSorter.TextFile.class.getGenericInterfaces();

			if (types.length == 1) {
				String name = types[0].getTypeName();

				Supplier<String> error1 = () -> String.format("%nThe Comparable interface not properly implemented. %nFound: %s%n", name);
				Supplier<String> error2 = () -> String.format("%nThe interface generic type not properly specified. %nFound: %s%n", name);

				assertAll(
						() -> assertTrue(name.contains("Comparable"), error1),
						() -> assertTrue(name.contains("TextFile"), error2)
				);
			}
			else {
				fail("\nTextFile does not implement the necessary interface.");
			}
		}

		/**
		 * Tests approach.
		 */
		@Test
		@Order(2)
		public void testTextFileType() {
			Class<?> test = TextFileSorter.TextFile.class;
			boolean expectAnonymous = false;
			boolean expectMember = true;
			boolean expectStatic = true;
			boolean expectLambda = false;
			testClassDefinition(test, expectAnonymous, expectMember, expectStatic, expectLambda);
		}

		/**
		 * Tests approach.
		 */
		@Test
		@Order(3)
		public void testDateComparatorType() {
			Class<?> test = TextFileSorter.DATE_COMPARATOR.getClass();
			boolean expectAnonymous = false;
			boolean expectMember = true;
			boolean expectStatic = true;
			boolean expectLambda = false;
			testClassDefinition(test, expectAnonymous, expectMember, expectStatic, expectLambda);
		}

		/**
		 * Tests approach.
		 */
		@Test
		@Order(4)
		public void testNameComparatorType() {
			Class<?> test = TextFileSorter.NAME_COMPARATOR.getClass();
			boolean expectAnonymous = false;
			boolean expectMember = true;
			boolean expectStatic = false;
			boolean expectLambda = false;
			testClassDefinition(test, expectAnonymous, expectMember, expectStatic, expectLambda);
		}

		/**
		 * Tests approach.
		 */
		@Test
		@Order(5)
		public void testSizeComparatorType() {
			Class<?> test = TextFileSorter.SIZE_COMPARATOR.getClass();
			boolean expectAnonymous = false;
			boolean expectMember = false;
			boolean expectStatic = false;
			boolean expectLambda = true;
			testClassDefinition(test, expectAnonymous, expectMember, expectStatic, expectLambda);
		}

		/**
		 * Tests approach.
		 */
		@Test
		@Order(6)
		public void testNestedComparatorType() {
			Class<?> test = TextFileSorter.getNested().getClass();
			boolean expectAnonymous = true;
			boolean expectMember = false;
			boolean expectStatic = false;
			boolean expectLambda = false;
			testClassDefinition(test, expectAnonymous, expectMember, expectStatic, expectLambda);
		}
	}

	/**
	 * Tests if the class is defined as expected.
	 *
	 * @param test the class to test
	 * @param expectAnonymous whether the class should be anonymous
	 * @param expectMember whether the class should be an inner/member class
	 * @param expectStatic whether the class should be a static class
	 * @param expectLambda whether the class should be a lambda expression
	 */
	public static void testClassDefinition(Class<?> test, boolean expectAnonymous,
			boolean expectMember, boolean expectStatic, boolean expectLambda) {
		boolean actualAnonymous = test.isAnonymousClass();
		boolean actualMember = test.isMemberClass();
		boolean actualStatic = Modifier.isStatic(test.getModifiers());
		boolean actualLambda = test.isSynthetic() && test.getTypeName().contains("$$Lambda$");

		assertAll(
				() -> assertEquals(expectAnonymous, actualAnonymous, "is anonymous?"),
				() -> assertEquals(expectMember, actualMember, "is inner class?"),
				() -> assertEquals(expectStatic, actualStatic, "is static class?"),
				() -> assertEquals(expectLambda, actualLambda, "is lambda expression?")
		);
	}

	/**
	 * Tests a specific comparator.
	 */
	@TestMethodOrder(OrderAnnotation.class)
	public abstract class ComparatorTests {
		/** The comparator being tested. */
		public Comparator<TextFileSorter.TextFile> test;

		/** A Path expected to be smaller than the small Path object. */
		public Path small;

		/** A Path expected to be larger than the small Path object. */
		public Path large;

		/** A Path expected to be equal to the large Path object. */
		public Path equal;

		/**
		 * Sets the comparator being tested.
		 * @throws IOException if an I/O error occurs
		 */
		@BeforeEach
		public abstract void setup() throws IOException;

		/**
		 * Tests that this comparator was implemented properly.
		 * @throws IOException if I/O error occurs
		 */
		@Test
		@Order(1)
		public void testPositive() throws IOException {
			TextFileSorter.TextFile one = new TextFileSorter.TextFile(large);
			TextFileSorter.TextFile two = new TextFileSorter.TextFile(small);
			String debug = String.format("%s > %s", one, two);
			assertTrue(test.compare(one, two) > 0, debug);
		}

		/**
		 * Tests that this comparator was implemented properly.
		 * @throws IOException if I/O error occurs
		 */
		@Test
		@Order(2)
		public void testNegative() throws IOException {
			TextFileSorter.TextFile one = new TextFileSorter.TextFile(small);
			TextFileSorter.TextFile two = new TextFileSorter.TextFile(large);
			String debug = String.format("%s < %s", one, two);
			assertTrue(test.compare(one, two) < 0, debug);
		}

		/**
		 * Tests that this comparator was implemented properly.
		 * @throws IOException if I/O error occurs
		 */
		@Test
		@Order(3)
		public void testEqual() throws IOException {
			TextFileSorter.TextFile one = new TextFileSorter.TextFile(large);
			TextFileSorter.TextFile two = new TextFileSorter.TextFile(equal);
			String debug = String.format("%s == %s", one, two);
			assertTrue(test.compare(one, two) == 0, debug);
		}
	}
}
