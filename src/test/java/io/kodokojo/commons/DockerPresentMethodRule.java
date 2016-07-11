package io.kodokojo.commons;

/*
 * #%L
 * commons-tests
 * %%
 * Copyright (C) 2016 Kodo-kojo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.kodokojo.commons.utils.DockerTestSupport;
import org.junit.Assume;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class DockerPresentMethodRule implements MethodRule {

    private final boolean stopContainers;

    private final DockerTestSupport dockerTestSupport = new DockerTestSupport();

    public DockerPresentMethodRule(boolean stopContainers) {
        this.stopContainers = stopContainers;
    }

    public DockerPresentMethodRule() {
        this(true);
    }

    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        DockerIsRequire dockerIsRequire = method.getAnnotation(DockerIsRequire.class);
        if (dockerIsRequire == null) {
            return base;
        }
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Assume.assumeTrue("Docker client isn't available", DockerPresentMethodRule.this.dockerTestSupport.isDockerIsPresent());
                if (stopContainers) {
                    dockerTestSupport.stopAndRemoveContainer();
                }
                try {
                    base.evaluate();
                } finally {
                    if (stopContainers) {
                        dockerTestSupport.stopAndRemoveContainer();
                    }
                }

            }

        };
    }

    public DockerTestSupport getDockerTestSupport() {
        return dockerTestSupport;
    }
}
