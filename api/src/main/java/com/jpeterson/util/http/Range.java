/*
 * Copyright 2007 Jesse Peterson
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jpeterson.util.http;

/**
 * Specifies a byte range.
 * 
 * @author Jesse Peterson
 */
public class Range implements Rangeable {
	private long start = 0;

	private long end = OPEN_ENDED;

	/**
	 * Used to signify a range that goes to the relative end.
	 */
	public static final long OPEN_ENDED = Long.MAX_VALUE;

	/**
	 * Create a <code>Range</code> that specifies the "whole" range, from 0 to
	 * the relative <code>END</code>.
	 * 
	 * @see #resolve(long)
	 */
	public Range() {
		this(0, OPEN_ENDED);
	}

	/**
	 * Create a <code>Range</code> with the specified <code>start</code> and
	 * <code>end</code>.
	 * 
	 * @param start
	 *            The start position of the range, inclusive.
	 * @param end
	 *            The end position of the range, inclusive.
	 */
	public Range(long start, long end) {
		setStart(start);
		setEnd(end);
	}

	/**
	 * Create a <code>Range</code> with the specified <code>start</code> and
	 * relative <code>END</code>. This constructor can also be used for a
	 * suffix range, where the range is relative from the end of the absolute
	 * length. A suffix range is specified by a negative start.
	 * 
	 * @param start
	 *            The start position of the range, inclusive. If negative, this
	 *            specifies a relative range starting from the end of the
	 *            absolute length. For instance, a start of "-5" is the last 5
	 *            bytes. With a total of 10 bytes, this is the range from 5-9
	 *            when resolved
	 * @see #resolve(long)
	 */
	public Range(long start) {
		this(start, OPEN_ENDED);
	}

	/**
	 * If the range is relative, it is resolved to an absolute range based on
	 * the absolute length. This also "squeezes" the range to ensure that it is
	 * contained by the <code>absoluteLength</code>.
	 * 
	 * @param absoluteLength
	 *            The absolute length that this range is a subset of.
	 * @throws IllegalStateException
	 *             Thrown if the range start is larger than the absolute length
	 */
	public void resolve(long absoluteLength) {
		if (start < 0) {
			// relative range
			start = absoluteLength + start;
			if (start < 0) {
				start = 0;
			}
			end = absoluteLength - 1;
		} else {
			if (start >= absoluteLength) {
				throw new IllegalStateException(
						"Range outside of absolute length");
			}

			if (end == OPEN_ENDED) {
				// relative end
				end = absoluteLength - 1;
			}

			// squeeze end, if necessary
			if (end >= absoluteLength) {
				end = absoluteLength - 1;
			}
		}
	}

	/**
	 * Get the length of the range. This is the "size" of the range, from
	 * <code>start</code> to <code>end</code>.
	 * 
	 * @return The length of the range.
	 */
	public long getLength() {
		if ((start < 0) || (end == OPEN_ENDED)) {
			// relative, needs to be resolved
			return -1;
		}
		return end - start + 1;
	}

	/**
	 * Determine if an index value is included within the range.
	 * 
	 * @param index
	 *            The index value to test.
	 * @return <code>True</code> if the index value is included within the
	 *         start and end of the range, inclusive. <code>False</code>
	 *         otherwise.
	 */
	public boolean includes(long index) {
		if ((index >= start) && (index <= end)) {
			return true;
		}

		return false;
	}

	/**
	 * Determine if this range can merge with the provided range. A range is
	 * mergeable if the start of the second range is between the start and end
	 * of the first range or the end of the second range is between the start
	 * and end of the first range or start of one is one less than the end of
	 * the other.
	 * 
	 * @param range
	 *            The range to test against this range.
	 * @return <code>True</code> if the ranges intersect, <code>false</code>
	 *         otherwise.
	 */
	public boolean combinable(Range range) {
		long point;

		point = range.getStart();

		if ((point >= start) && (point <= end)) {
			return true;
		}

		point = range.getEnd();

		if ((point >= start) && (point <= end)) {
			return true;
		}

		if (((end + 1) == range.getStart()) || (range.getEnd() + 1 == start)) {
			return true;
		}

		return false;
	}

	/**
	 * Merge another intersecting range with this range.
	 * 
	 * @param range
	 *            The other range to merge with.
	 * @return The merged range.
	 * @throws IllegalArgumentException
	 *             Thrown if the provided range doesn't intersect.
	 * @see #combinable(Range)
	 */
	public Range combine(Range range) throws IllegalArgumentException {
		if (!range.combinable(this)) {
			throw new IllegalArgumentException(
					"Ranges do not intersect. Can not merge.");
		}

		if (range.getStart() < start) {
			start = range.getStart();
		}

		if (range.getEnd() > end) {
			end = range.getEnd();
		}

		return this;
	}

	/**
	 * Get the start position of the range.
	 * 
	 * @return The start position of the range, inclusive.
	 */
	public long getStart() {
		return start;
	}

	/**
	 * Set the start position of the range.
	 * 
	 * @param start
	 *            The start position of the range, inclusive.
	 */
	public void setStart(long start) {
		this.start = start;
	}

	/**
	 * Get the end position of the range.
	 * 
	 * @return The end position of the range, inclusive.
	 */
	public long getEnd() {
		return end;
	}

	/**
	 * Set the end position of the range.
	 * 
	 * @param end
	 *            The end position of the range, inclusive.
	 */
	public void setEnd(long end) {
		this.end = end;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * 
	 * @param obj
	 *            the reference object with which to compare.
	 * @return <code>true</code> if this object is the same as the
	 *         <code>obj</code> argument; <code>false</code> otherwise.
	 */
	public boolean equals(Object obj) {
		Range r;

		if (!(obj instanceof Range)) {
			return false;
		}

		r = (Range) obj;

		if (r.getStart() == start) {
			if (r.getEnd() == end) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns a hash code value for the object.
	 * 
	 * @return a hash code value for this object.
	 */
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * Returns a string representation of the object.
	 * 
	 * @return A string representation of the object in JSON format.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("{");
		buffer.append("\"start\":");
		buffer.append(start);
		buffer.append(",\"end\":");
		buffer.append(end);
		buffer.append("}");

		return buffer.toString();
	}
}
