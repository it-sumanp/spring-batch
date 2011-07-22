/*
 * Copyright 2006-2007 the original author or authors.
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.StepRegistry;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.util.Assert;

/**
 * Simple map-based implementation of {@link JobRegistry}. Access to the map is
 * synchronized, guarded by an internal lock.
 *
 * @author Dave Syer
 *
 */
public class MapJobRegistry implements JobRegistry, StepRegistry {

	private final Map<String, JobFactory> map = new HashMap<String, JobFactory>();
    private final MapStepRegistry stepRegistry = new MapStepRegistry();

	public void register(JobFactory jobFactory) throws DuplicateJobException {
		Assert.notNull(jobFactory);
		String name = jobFactory.getJobName();
		Assert.notNull(name, "Job configuration must have a name.");
		synchronized (map) {
			if (map.containsKey(name)) {
				throw new DuplicateJobException("A job configuration with this name [" + name
						+ "] was already registered");
			}
			map.put(name, jobFactory);
		}
	}

	public void unregister(String name) {
		Assert.notNull(name, "Job configuration must have a name.");
		synchronized (map) {
			map.remove(name);
		}

	}

	public Job getJob(String name) throws NoSuchJobException {
		synchronized (map) {
			if (!map.containsKey(name)) {
				throw new NoSuchJobException("No job configuration with the name [" + name + "] was registered");
			}
			return map.get(name).createJob();
		}
	}

	public Collection<String> getJobNames() {
		synchronized (map) {
			return Collections.unmodifiableCollection(new HashSet<String>(map.keySet()));
		}
	}

    public void register(String jobName, Collection<Step> steps) {
        stepRegistry.register(jobName, steps);
    }

    public void unregisterStepsFromJob(String jobName) {
        stepRegistry.unregisterStepsFromJob(jobName);
    }

    public Step getStep(String jobName, String stepName) throws NoSuchJobException {
        return stepRegistry.getStep(jobName, stepName);
    }

}
