package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Executor;
import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.testutils.MockedRunCollectorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.Mockito.*;


public class BuildLikelyStuckGaugeTest extends MockedRunCollectorTest {


    @Test
    public void testNothingCalculatedWhenRunIsNull() {
        BuildLikelyStuckGauge sut = new BuildLikelyStuckGauge(getLabelNames(), getNamespace(), getSubSystem(), "");

        sut.calculateMetric(null, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(0, collect.get(0).samples.size());

    }

    @Test
    public void testNothingCalculatedWhenJobIsNotBuilding() {
        Mockito.when(mock.isBuilding()).thenReturn(false);

        BuildLikelyStuckGauge sut = new BuildLikelyStuckGauge(getLabelNames(), getNamespace(), getSubSystem(), "");

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(0, collect.get(0).samples.size());

    }

    @Test
    public void testNothingCalculatedWhenNoExecutorFound() {
        Mockito.when(mock.isBuilding()).thenReturn(true);
        Mockito.when(mock.getExecutor()).thenReturn(null);

        BuildLikelyStuckGauge sut = new BuildLikelyStuckGauge(getLabelNames(), getNamespace(), getSubSystem(), "");

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(0, collect.get(0).samples.size());
    }

    @Test
    public void testBuildIsLikelyStuck() {
        Mockito.when(mock.isBuilding()).thenReturn(true);
        Executor mockedExecutor = mock(Executor.class);
        when(mockedExecutor.isLikelyStuck()).thenReturn(true);
        Mockito.when(mock.getExecutor()).thenReturn(mockedExecutor);

        BuildLikelyStuckGauge sut = new BuildLikelyStuckGauge(getLabelNames(), getNamespace(), getSubSystem(), "");

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(1, collect.get(0).samples.size());
        Assertions.assertEquals(1.0, collect.get(0).samples.get(0).value);
    }

    @Test
    public void testBuildIsNotLikelyStuck() {
        Mockito.when(mock.isBuilding()).thenReturn(true);
        Executor mockedExecutor = mock(Executor.class);
        when(mockedExecutor.isLikelyStuck()).thenReturn(false);
        Mockito.when(mock.getExecutor()).thenReturn(mockedExecutor);

        BuildLikelyStuckGauge sut = new BuildLikelyStuckGauge(getLabelNames(), getNamespace(), getSubSystem(), "");

        sut.calculateMetric(mock, getLabelValues());

        List<Collector.MetricFamilySamples> collect = sut.collect();

        Assertions.assertEquals(1, collect.size());
        Assertions.assertEquals(1, collect.get(0).samples.size());
        Assertions.assertEquals(0.0, collect.get(0).samples.get(0).value);
    }
}