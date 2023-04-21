package org.jenkinsci.plugins.prometheus.collectors.testutils;


import hudson.model.Job;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class MockedJobCollectorTest extends CollectorTest {

    @Mock
    protected Job mock;

    @Test
    public abstract void testCollectResult();


}
