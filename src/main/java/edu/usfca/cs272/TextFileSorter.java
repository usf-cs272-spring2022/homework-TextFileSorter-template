package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;

/**
 * A simple class for sorting text files.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2022
 */
public class TextFileSorter {
	/**
	 * A simple comparable class for storing and comparing text files. Should
	 * implement the {@link Comparable} interface to allow for comparison between
	 * {@link TextFile} objects by their {@link TextFile#path} paths.
	 *
	 * @see Comparable
	 * @see Path#compareTo(Path)
	 * @see TextFile#path
	 */
	// TODO Update declaration to implement interface
	public static class TextFile {
		/** The normalized text file path. */
		private final Path path;

		/** The text file name. */
		private final String name;

		/** The text file size. */
		private final long size;

		/** The last modified date of the file. */
		private final FileTime date;

		/**
		 * Initializes a text file.
		 *
		 * @param path the text file source
		 * @throws IOException if I/O error occurs
		 */
		public TextFile(Path path) throws IOException {
			this.path = path.normalize();
			this.name = path.getFileName().toString();
			this.size = Files.isRegularFile(path) ? Files.size(path) : -1;
			this.date = Files.isRegularFile(path) ? Files.getLastModifiedTime(path) : FileTime.fromMillis(0);
		}

		@Override
		public String toString() {
			String output = this.path.getNameCount() < 1 ? this.name : path.toString();
			return String.format("%s (%s bytes, modified %s)", output, size, date);
		}

		/**
		 * Compares {@link TextFile} objects by their {@link Path} source.
		 *
		 * @param other the other text file
		 * @return a negative integer, zero, or a positive integer as this text file is less than, equal to, or greater than the other text file
		 *
		 * @see Path#compareTo(Path)
		 * @see Comparable#compareTo(Object)
		 */
		public int compareTo(TextFile other) {
			// TODO Update to compare by source
			throw new UnsupportedOperationException("Not yet implemented.");
		}
	}

	/*
	 * TODO For the following, define the comparators based on the approach stated
	 * in the Javadoc comments. Add members, classes, and methods as needed.
	 */

	/**
	 * A comparator that compares text files by their last modified date, defined
	 * using a static nested class.
	 *
	 * @see TextFile#date
	 * @see FileTime#compareTo(FileTime)
	 */
	public static final Comparator<TextFile> DATE_COMPARATOR = null;

	/**
	 * A comparator that compares text files in case insensitive order by their
	 * name, defined using a non-static inner class.
	 *
	 * @see TextFile#name
	 * @see String#CASE_INSENSITIVE_ORDER
	 */
	public static final Comparator<TextFile> NAME_COMPARATOR = null;

	/**
	 * A comparator that compares text files by their size with the largest sizes
	 * first (descending order), defined using a lambda expression.
	 *
	 * @see TextFile#size
	 * @see Long#compare(long, long)
	 */
	public static final Comparator<TextFile> SIZE_COMPARATOR = null;

	/**
	 * Returns a comparator created using an anonymous inner class that compares
	 * text files by the {@link #SIZE_COMPARATOR} if the sizes are not equal. If
	 * the sizes are equal, then compares using a {@link #NAME_COMPARATOR}
	 * instead. If the names are equal, then compares by the {@link TextFile}
	 * natural sort order (by its {@link Path} source).
	 *
	 * @see #SIZE_COMPARATOR
	 * @see #NAME_COMPARATOR
	 * @see TextFile
	 *
	 * @return a comparator created using an anonymous inner class
	 */
	public static Comparator<TextFile> getNested() {
		return new Comparator<TextFile>() {
			@Override
			public int compare(TextFile first, TextFile second) {
				throw new UnsupportedOperationException("Not yet implemented.");
			}
		};
	};

	// TODO Add members, methods, or classes as needed.
}
