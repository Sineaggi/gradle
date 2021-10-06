/*
 * Copyright 2021 the original author or authors.
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

package org.gradle.internal.resources;

import org.gradle.util.Path;

public class ProjectLockRegistry extends AbstractResourceLockRegistry<Path, ProjectLock> {
    private final boolean parallelEnabled;
    private final AllProjectsLock allProjectsLock;
    private int lockedProjects;

    public ProjectLockRegistry(ResourceLockCoordinationService coordinationService, boolean parallelEnabled) {
        super(coordinationService);
        this.parallelEnabled = parallelEnabled;
        // TODO - register this so that #hasOpenLocks() can see it
        allProjectsLock = new AllProjectsLock("All projects", coordinationService, this);
    }

    @Override
    public void lockAcquired(ResourceLock resourceLock) {
        super.lockAcquired(resourceLock);
        lockedProjects++;
    }

    @Override
    public void lockReleased(ResourceLock resourceLock) {
        super.lockReleased(resourceLock);
        lockedProjects--;
    }

    int getLockedProjects() {
        return lockedProjects;
    }

    public boolean getAllowsParallelExecution() {
        return parallelEnabled;
    }

    public ResourceLock getAllProjectsLock() {
        return allProjectsLock;
    }

    public ResourceLock getResourceLock(Path buildIdentityPath, Path projectIdentityPath) {
        return getResourceLock(parallelEnabled ? projectIdentityPath : buildIdentityPath);
    }

    ResourceLock getResourceLock(final Path lockPath) {
        return getOrRegisterResourceLock(lockPath, new ResourceLockProducer<Path, ProjectLock>() {
            @Override
            public ProjectLock create(Path projectPath, ResourceLockCoordinationService coordinationService, ResourceLockContainer owner) {
                return new ProjectLock(lockPath.getPath(), coordinationService, owner, allProjectsLock);
            }
        });
    }
}
