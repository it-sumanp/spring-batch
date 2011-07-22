/*
 * Copyright 2006-2010 the original author or authors.
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
package org.springframework.batch.core.configuration.support;

import org.junit.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.StepRegistry;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.step.NoSuchStepException;
import org.springframework.batch.core.step.StepLocator;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Dave Syer
 */
public class DefaultJobLoaderTests {

    /**
     * The name of the job as defined in the test context used in this test.
     */
    private static final String TEST_JOB_NAME = "test-job";

    /**
     * The name of the step as defined in the test context used in this test.
     */
    private static final String TEST_STEP_NAME = "test-step";

    private JobRegistry jobRegistry = new MapJobRegistry();
    private StepRegistry stepRegistry = new MapStepRegistry();

    private DefaultJobLoader jobLoader = new DefaultJobLoader(jobRegistry, stepRegistry);

    @Test
    public void testLoadWithExplicitName() throws Exception {
        ClassPathXmlApplicationContextFactory factory = new ClassPathXmlApplicationContextFactory(
                new ByteArrayResource(JOB_XML.getBytes()));
        jobLoader.load(factory);
        assertEquals(1, jobRegistry.getJobNames().size());
        jobLoader.reload(factory);
        assertEquals(1, jobRegistry.getJobNames().size());
    }

    @Test
    public void testRegistryUpdated() throws Exception {
        ClassPathXmlApplicationContextFactory factory = new ClassPathXmlApplicationContextFactory(
                new ClassPathResource("trivial-context.xml", getClass()));
        jobLoader.load(factory);
        assertEquals(1, jobRegistry.getJobNames().size());
        assertStepExist(TEST_JOB_NAME, TEST_STEP_NAME);
    }

    @Test
    public void testMultipleJobsInTheSameContext() throws Exception {
        ClassPathXmlApplicationContextFactory factory = new ClassPathXmlApplicationContextFactory(
                new ClassPathResource("job-context-with-steps.xml", getClass()));
        jobLoader.load(factory);
        assertEquals(2, jobRegistry.getJobNames().size());
        assertStepExist("job1", "step11");
        assertStepExist("job1", "step12");
        assertStepExist("job2", "step21");
        assertStepExist("job2", "step22");

    }

    @Test
    public void testReload() throws Exception {
        ClassPathXmlApplicationContextFactory factory = new ClassPathXmlApplicationContextFactory(
                new ClassPathResource("trivial-context.xml", getClass()));
        jobLoader.load(factory);
        assertEquals(1, jobRegistry.getJobNames().size());
        assertStepExist(TEST_JOB_NAME, TEST_STEP_NAME);
        jobLoader.reload(factory);
        assertEquals(1, jobRegistry.getJobNames().size());
        assertStepExist(TEST_JOB_NAME, TEST_STEP_NAME);
    }

    @Test
    public void testReloadWithAutoRegister() throws Exception {
        ClassPathXmlApplicationContextFactory factory = new ClassPathXmlApplicationContextFactory(
                new ClassPathResource("trivial-context-autoregister.xml", getClass()));
        jobLoader.load(factory);
        assertEquals(1, jobRegistry.getJobNames().size());
        assertStepExist(TEST_JOB_NAME, TEST_STEP_NAME);
        jobLoader.reload(factory);
        assertEquals(1, jobRegistry.getJobNames().size());
        assertStepExist(TEST_JOB_NAME, TEST_STEP_NAME);
    }

    protected void assertStepExist(String jobName, String stepName) {
        try {
            stepRegistry.getStep(jobName, stepName);
        } catch (NoSuchJobException e) {
            fail("Job with name [" + jobName + "] should have been found.");
        } catch (NoSuchStepException e) {
            fail("Step with name [" + stepName + "] for job [" + jobName + "] should have been found.");
        }
    }

    private static final String JOB_XML = String
            .format(
                    "<beans xmlns='http://www.springframework.org/schema/beans' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
                            + "xsi:schemaLocation='http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd'><bean class='%s$StubJob'/></beans>",
                    DefaultJobLoaderTests.class.getName());

    public static class StubJob implements Job, StepLocator {

        public void execute(JobExecution execution) {
        }

        public JobParametersIncrementer getJobParametersIncrementer() {
            return null;
        }

        public String getName() {
            return "job";
        }

        public boolean isRestartable() {
            return false;
        }

        public JobParametersValidator getJobParametersValidator() {
            return null;
        }

        public Collection<String> getStepNames() {
            return Collections.emptyList();
        }

        public Step getStep(String stepName) throws NoSuchStepException {
            throw new NoSuchStepException("Step [" + stepName + "] does not exist");
        }
    }

}
