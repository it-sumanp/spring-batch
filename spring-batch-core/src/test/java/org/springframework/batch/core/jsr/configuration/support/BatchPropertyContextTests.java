/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.core.jsr.configuration.support;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

/**
 * <p>
 * Test cases around {@link BatchPropertyContext}.
 * </p>
 *
 * @author Chris Schaefer
 */
public class BatchPropertyContextTests {
	private List<BatchPropertyContext.BatchPropertyContextEntry> jobProperties = new ArrayList<BatchPropertyContext.BatchPropertyContextEntry>();
	private List<BatchPropertyContext.BatchPropertyContextEntry> stepProperties = new ArrayList<BatchPropertyContext.BatchPropertyContextEntry>();
	private List<BatchPropertyContext.BatchPropertyContextEntry> artifactProperties = new ArrayList<BatchPropertyContext.BatchPropertyContextEntry>();
	private List<BatchPropertyContext.BatchPropertyContextEntry> stepArtifactProperties = new ArrayList<BatchPropertyContext.BatchPropertyContextEntry>();
	private List<BatchPropertyContext.BatchPropertyContextEntry> partitionProperties = new ArrayList<BatchPropertyContext.BatchPropertyContextEntry>();

	@Before
	public void setUp() {
		BatchPropertyContext batchPropertyContext = new BatchPropertyContext();

		Properties step1Properties = new Properties();
		step1Properties.setProperty("step1PropertyName1", "step1PropertyValue1");
		step1Properties.setProperty("step1PropertyName2", "step1PropertyValue2");
		stepProperties.add(batchPropertyContext.new BatchPropertyContextEntry("step1", step1Properties, BatchArtifact.BatchArtifactType.STEP));

		Properties step2Properties = new Properties();
		step2Properties.setProperty("step2PropertyName1", "step2PropertyValue1");
		step2Properties.setProperty("step2PropertyName2", "step2PropertyValue2");
		stepProperties.add(batchPropertyContext.new BatchPropertyContextEntry("step2", step2Properties, BatchArtifact.BatchArtifactType.STEP));

		Properties jobProperties = new Properties();
		jobProperties.setProperty("jobProperty1", "jobProperty1value");
		jobProperties.setProperty("jobProperty2", "jobProperty2value");
		this.jobProperties.add(batchPropertyContext.new BatchPropertyContextEntry("job1", jobProperties, BatchArtifact.BatchArtifactType.JOB));

		Properties artifactProperties = new Properties();
		artifactProperties.setProperty("deciderProperty1", "deciderProperty1value");
		artifactProperties.setProperty("deciderProperty2", "deciderProperty2value");
		this.artifactProperties.add(batchPropertyContext.new BatchPropertyContextEntry("decider1", artifactProperties, BatchArtifact.BatchArtifactType.ARTIFACT));

		Properties stepArtifactProperties = new Properties();
		stepArtifactProperties.setProperty("readerProperty1", "readerProperty1value");
		stepArtifactProperties.setProperty("readerProperty2", "readerProperty2value");

		BatchPropertyContext.BatchPropertyContextEntry batchPropertyContextEntry =
				batchPropertyContext.new BatchPropertyContextEntry("reader", stepArtifactProperties, BatchArtifact.BatchArtifactType.STEP_ARTIFACT);
		batchPropertyContextEntry.setStepName("step1");

		this.stepArtifactProperties.add(batchPropertyContextEntry);

		Properties partitionProperties = new Properties();
		partitionProperties.setProperty("writerProperty1", "writerProperty1valuePartition0");
		partitionProperties.setProperty("writerProperty2", "writerProperty2valuePartition0");

		BatchPropertyContext.BatchPropertyContextEntry partitionBatchPropertyContextEntry =
				batchPropertyContext.new BatchPropertyContextEntry("writer", partitionProperties, BatchArtifact.BatchArtifactType.STEP_ARTIFACT);
		partitionBatchPropertyContextEntry.setStepName("step2:partition0");

		this.partitionProperties.add(partitionBatchPropertyContextEntry);

		Properties partitionStepProperties = new Properties();
		partitionStepProperties.setProperty("writerProperty1Step", "writerProperty1");
		partitionStepProperties.setProperty("writerProperty2Step", "writerProperty2");

		BatchPropertyContext.BatchPropertyContextEntry partitionStepBatchPropertyContextEntry =
				batchPropertyContext.new BatchPropertyContextEntry("writer", partitionStepProperties, BatchArtifact.BatchArtifactType.STEP_ARTIFACT);
		partitionStepBatchPropertyContextEntry.setStepName("step2");

		this.partitionProperties.add(partitionStepBatchPropertyContextEntry);
	}

	@Test
	public void testStepLevelProperties() {
		BatchPropertyContext batchPropertyContext = new BatchPropertyContext();
		batchPropertyContext.setJobPropertiesContextEntry(jobProperties);
		batchPropertyContext.setStepPropertiesContextEntry(stepProperties);

		Properties step1Properties = batchPropertyContext.getStepProperties("step1");
		assertEquals(2, step1Properties.size());
		assertEquals("step1PropertyValue1", step1Properties.getProperty("step1PropertyName1"));
		assertEquals("step1PropertyValue2", step1Properties.getProperty("step1PropertyName2"));

		Properties step2Properties = batchPropertyContext.getStepProperties("step2");
		assertEquals(2, step2Properties.size());
		assertEquals("step2PropertyValue1", step2Properties.getProperty("step2PropertyName1"));
		assertEquals("step2PropertyValue2", step2Properties.getProperty("step2PropertyName2"));
	}

	@Test
	public void testJobLevelProperties() {
		BatchPropertyContext batchPropertyContext = new BatchPropertyContext();
		batchPropertyContext.setJobPropertiesContextEntry(jobProperties);

		Properties jobProperties = batchPropertyContext.getJobProperties();
		assertEquals(2, jobProperties.size());
		assertEquals("jobProperty1value", jobProperties.getProperty("jobProperty1"));
		assertEquals("jobProperty2value", jobProperties.getProperty("jobProperty2"));
	}

	@Test
	public void testAddPropertiesToExistingStep() {
		BatchPropertyContext batchPropertyContext = new BatchPropertyContext();
		batchPropertyContext.setJobPropertiesContextEntry(jobProperties);
		batchPropertyContext.setStepPropertiesContextEntry(stepProperties);

		Properties step1 = batchPropertyContext.getStepProperties("step1");
		assertEquals(2, step1.size());
		assertEquals("step1PropertyValue1", step1.getProperty("step1PropertyName1"));
		assertEquals("step1PropertyValue2", step1.getProperty("step1PropertyName2"));

		Properties step1properties = new Properties();
		step1properties.setProperty("newStep1PropertyName", "newStep1PropertyValue");

		batchPropertyContext.setStepPropertiesContextEntry(
				Collections.singletonList(batchPropertyContext.new BatchPropertyContextEntry("step1", step1properties, BatchArtifact.BatchArtifactType.STEP)));

		Properties step1updated = batchPropertyContext.getStepProperties("step1");
		assertEquals(3, step1updated.size());
		assertEquals("step1PropertyValue1", step1updated.getProperty("step1PropertyName1"));
		assertEquals("step1PropertyValue2", step1updated.getProperty("step1PropertyName2"));
		assertEquals("newStep1PropertyValue", step1updated.getProperty("newStep1PropertyName"));
	}

	@Test
	public void testNonStepLevelArtifactProperties() {
		BatchPropertyContext batchPropertyContext = new BatchPropertyContext();
		batchPropertyContext.setJobPropertiesContextEntry(jobProperties);
		batchPropertyContext.setArtifactPropertiesContextEntry(artifactProperties);
		batchPropertyContext.setStepPropertiesContextEntry(stepProperties);

		Properties artifactProperties = batchPropertyContext.getArtifactProperties("decider1");
		assertEquals(4, artifactProperties.size());
		assertEquals("deciderProperty1value", artifactProperties.getProperty("deciderProperty1"));
		assertEquals("deciderProperty2value", artifactProperties.getProperty("deciderProperty2"));
		assertEquals("jobProperty1value", artifactProperties.getProperty("jobProperty1"));
		assertEquals("jobProperty2value", artifactProperties.getProperty("jobProperty2"));
	}

	@Test
	public void testStepLevelArtifactProperties() {
		BatchPropertyContext batchPropertyContext = new BatchPropertyContext();
		batchPropertyContext.setJobPropertiesContextEntry(jobProperties);
		batchPropertyContext.setArtifactPropertiesContextEntry(artifactProperties);
		batchPropertyContext.setStepPropertiesContextEntry(stepProperties);
		batchPropertyContext.setStepArtifactPropertiesContextEntry(stepArtifactProperties);

		Properties artifactProperties = batchPropertyContext.getStepArtifactProperties("step1", "reader");
		assertEquals(6, artifactProperties.size());
		assertEquals("readerProperty1value", artifactProperties.getProperty("readerProperty1"));
		assertEquals("readerProperty2value", artifactProperties.getProperty("readerProperty2"));
		assertEquals("step1PropertyValue1", artifactProperties.getProperty("step1PropertyName1"));
		assertEquals("step1PropertyValue2", artifactProperties.getProperty("step1PropertyName2"));
		assertEquals("jobProperty1value", artifactProperties.getProperty("jobProperty1"));
		assertEquals("jobProperty2value", artifactProperties.getProperty("jobProperty2"));
	}

	@Test
	public void testArtifactNonOverridingJobProperties() {
		BatchPropertyContext batchPropertyContext = new BatchPropertyContext();
		batchPropertyContext.setJobPropertiesContextEntry(jobProperties);
		batchPropertyContext.setArtifactPropertiesContextEntry(artifactProperties);

		Properties jobProperties = new Properties();
		jobProperties.setProperty("deciderProperty1", "decider1PropertyOverride");

		batchPropertyContext.setJobPropertiesContextEntry(
				Collections.singletonList(batchPropertyContext.new BatchPropertyContextEntry("job1", jobProperties, BatchArtifact.BatchArtifactType.JOB)));

		Properties step1 = batchPropertyContext.getArtifactProperties("decider1");
		assertEquals(4, step1.size());
		assertEquals("deciderProperty1value", step1.getProperty("deciderProperty1"));
		assertEquals("deciderProperty2value", step1.getProperty("deciderProperty2"));
		assertEquals("jobProperty1value", step1.getProperty("jobProperty1"));
		assertEquals("jobProperty2value", step1.getProperty("jobProperty2"));

		Properties job = batchPropertyContext.getJobProperties();
		assertEquals(3, job.size());
		assertEquals("decider1PropertyOverride", job.getProperty("deciderProperty1"));
		assertEquals("jobProperty1value", job.getProperty("jobProperty1"));
		assertEquals("jobProperty2value", job.getProperty("jobProperty2"));
	}

	@Test
	public void testPartitionProperties() {
		BatchPropertyContext batchPropertyContext = new BatchPropertyContext();
		batchPropertyContext.setJobPropertiesContextEntry(jobProperties);
		batchPropertyContext.setArtifactPropertiesContextEntry(artifactProperties);
		batchPropertyContext.setStepPropertiesContextEntry(stepProperties);
		batchPropertyContext.setStepArtifactPropertiesContextEntry(stepArtifactProperties);
		batchPropertyContext.setStepArtifactPropertiesContextEntry(partitionProperties);

		Properties artifactProperties = batchPropertyContext.getStepArtifactProperties("step2:partition0", "writer");
		assertEquals(8, artifactProperties.size());
		assertEquals("writerProperty1", artifactProperties.getProperty("writerProperty1Step"));
		assertEquals("writerProperty2", artifactProperties.getProperty("writerProperty2Step"));
		assertEquals("writerProperty1valuePartition0", artifactProperties.getProperty("writerProperty1"));
		assertEquals("writerProperty2valuePartition0", artifactProperties.getProperty("writerProperty2"));
		assertEquals("step2PropertyValue1", artifactProperties.getProperty("step2PropertyName1"));
		assertEquals("step2PropertyValue2", artifactProperties.getProperty("step2PropertyName2"));
		assertEquals("jobProperty1value", artifactProperties.getProperty("jobProperty1"));
		assertEquals("jobProperty2value", artifactProperties.getProperty("jobProperty2"));
	}
}
