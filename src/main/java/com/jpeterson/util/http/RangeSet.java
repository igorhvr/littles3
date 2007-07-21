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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

public class RangeSet extends AbstractSet implements Rangeable {

	private ArrayList ranges = new ArrayList();

	public RangeSet() {
		super();
	}

	public RangeSet(Collection collection) {
		this();

		for (Iterator iter = collection.iterator(); iter.hasNext();) {
			add(iter.next());
		}
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
		Range range;
		boolean error = true;

		for (Iterator iter = ranges.iterator(); iter.hasNext();) {
			range = (Range) iter.next();
			range.resolve(absoluteLength);

			if (range.getStart() >= absoluteLength) {
				// outside of absolute length
			} else if (range.getEnd() >= absoluteLength) {
				// squeeze to absolute length
				range.setEnd(absoluteLength - 1);
				error = false;
			} else {
				error = false;
			}
		}

		if (error) {
			throw new IllegalStateException("Range outside of absolute length");
		}

		// make sure resolved ranges are ordered
		RangeSet tempRangeSet = new RangeSet(this);
		ranges = new ArrayList();

		for (Iterator iter = tempRangeSet.iterator(); iter.hasNext();) {
			range = (Range) iter.next();
			if (range.getStart() >= absoluteLength) {
				// outside of absolute, throw it out
			} else {
				ranges.add(range);
			}
		}
	}

	public long getLength() {
		long length = 0;

		for (Iterator iter = ranges.iterator(); iter.hasNext();) {
			Range range = (Range) iter.next();
			length += range.getLength();
		}

		return length;
	}

	public boolean includes(long index) {
		for (Iterator iter = ranges.iterator(); iter.hasNext();) {
			Range range = (Range) iter.next();

			if (range.includes(index)) {
				return true;
			}
		}

		return false;
	}

	public boolean add(Object o) {
		Range providedRange = (Range) o;
		Range r, testRange;

		// merge intersecting ranges

		testRange = providedRange;
		for (ListIterator iter = ranges.listIterator(); iter.hasNext()
				&& (testRange != null);) {
			r = (Range) iter.next();
			if (r.combinable(testRange)) {
				iter.remove();
				r.combine(testRange);
				testRange = r;
			} else {
				if (testRange.getStart() < r.getStart()) {
					iter.remove();
					iter.add(testRange);
					testRange = r;
				}
			}
		}

		if (testRange != null) {
			ranges.add(testRange);
		}

		return true;
	}

	@Override
	public Iterator iterator() {
		return ranges.iterator();
	}

	@Override
	public int size() {
		return ranges.size();
	}

}
