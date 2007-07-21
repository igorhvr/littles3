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
 * Process an HTTP <code>Range</code> header value.
 * 
 * @author Jesse Peterson
 */
public class RangeFactory {
	/**
	 * Process a <code>Range</code> header, creating a <code>RangeSet</code>
	 * covering the range value.
	 * 
	 * @param value
	 *            The <code>Range</code> header value. Like "bytes=0-500".
	 * @return The <code>RangeSet</code> based on the <code>value</code>.
	 * @throws IllegalArgumentException
	 *             Unable to process the <code>value</code>.
	 */
	public static RangeSet processRangeHeader(String value)
			throws IllegalArgumentException {
		RangeSet rangeSet = new RangeSet();
		Range range;

		if (value.startsWith("bytes=")) {
			String rangeValues[] = value.substring("bytes=".length())
					.split(",");

			for (int i = 0; i < rangeValues.length; i++) {
				if (rangeValues[i].startsWith("-")) {
					// negative range
					// TODO implement
					throw new UnsupportedOperationException(
							"Suffix byte ranges not yet implemented: " + value);
				} else {
					int dash = rangeValues[i].indexOf('-');
					if (rangeValues[i].endsWith("-")) {
						// no end
						long start = Long.parseLong(rangeValues[i].substring(0,
								dash));
						range = new Range();
						range.setStart(start);
						rangeSet.add(range);
					} else {
						long start = Long.parseLong(rangeValues[i].substring(0,
								dash));
						long end = Long.parseLong(rangeValues[i]
								.substring(dash + 1));
						rangeSet.add(new Range(start, end));
					}
				}
			}
		} else {
			throw new IllegalArgumentException(
					"Range value does not start with 'bytes=': " + value);
		}

		return rangeSet;
	}
}
