
/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.dataflow.sdk.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.cloud.dataflow.sdk.io.BoundedSource;
import com.google.cloud.dataflow.sdk.io.BoundedSource.BoundedReader;
import com.google.cloud.dataflow.sdk.io.CountingSource;
import com.google.cloud.dataflow.sdk.options.PipelineOptions;
import com.google.cloud.dataflow.sdk.options.PipelineOptionsFactory;
import com.google.common.collect.Sets;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.Set;

/**
 * Tests for {@link SourceTestUtils}.
 */
@RunWith(JUnit4.class)
public class SourceTestUtilsTest {

  @Test
  public void testToUnsplittableSource() throws Exception {
    PipelineOptions options = PipelineOptionsFactory.create();
    BoundedSource<Long> baseSource = CountingSource.upTo(100);
    BoundedSource<Long> unsplittableSource = SourceTestUtils.toUnsplittableSource(baseSource);
    List<?> splits = unsplittableSource.splitIntoBundles(1, options);
    assertEquals(splits.size(), 1);
    assertEquals(splits.get(0), unsplittableSource);

    BoundedReader<Long> unsplittableReader = unsplittableSource.createReader(options);
    assertEquals(0, unsplittableReader.getFractionConsumed(), 1e-15);

    Set<Long> expected = Sets.newHashSet(SourceTestUtils.readFromSource(baseSource, options));
    Set<Long> actual = Sets.newHashSet();
    actual.addAll(SourceTestUtils.readNItemsFromUnstartedReader(unsplittableReader, 40));
    assertNull(unsplittableReader.splitAtFraction(0.5));
    actual.addAll(SourceTestUtils.readRemainingFromReader(unsplittableReader, true /* started */));
    assertEquals(1, unsplittableReader.getFractionConsumed(), 1e-15);

    assertEquals(100, actual.size());
    assertEquals(Sets.newHashSet(expected), Sets.newHashSet(actual));
  }
}
