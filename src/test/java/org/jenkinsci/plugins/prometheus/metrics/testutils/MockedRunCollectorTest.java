package org.jenkinsci.plugins.prometheus.metrics.testutils;

import hudson.model.Run;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class MockedRunCollectorTest extends CollectorTest {

    @Mock
    protected Run mock;


}
