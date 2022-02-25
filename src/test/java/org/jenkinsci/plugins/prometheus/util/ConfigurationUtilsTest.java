package org.jenkinsci.plugins.prometheus.util;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;

@RunWith(JUnitParamsRunner.class)
public class ConfigurationUtilsTest {

    @Test
    public void verifyGetNamespaceWhenEnvIsNonEmpty() throws Exception {
        String namespace = "foobar";
        withEnvironmentVariable("PROMETHEUS_NAMESPACE", namespace).execute(() -> {
            String result = ConfigurationUtils.getNamespace();
            assertThat(result).isEqualTo(namespace);
        });
    }

    @Test
    @Parameters({"true", "false"})
    public void verifyGetNamespaceWhenEnvIsNotSetOrEmpty(boolean notSetOrEmpty) throws Exception {
        PrometheusConfiguration config = mock(PrometheusConfiguration.class);
        String namespace = "default-namespace";
        when(config.getDefaultNamespace()).thenReturn(namespace);
        try (MockedStatic<PrometheusConfiguration> configStatic = mockStatic(PrometheusConfiguration.class)) {
            configStatic.when(() -> PrometheusConfiguration.get()).thenReturn(config);
            withEnvironmentVariable("PROMETHEUS_NAMESPACE", notSetOrEmpty ? null : "").execute(() -> {
                String result = ConfigurationUtils.getNamespace();
                assertThat(result).isEqualTo(namespace);
            });
        }
    }

    @Test
    public void verifyGetSubSystem() {
        assertThat(ConfigurationUtils.getSubSystem()).isEqualTo("jenkins");
    }
}
