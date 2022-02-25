package org.jenkinsci.plugins.prometheus.util;

import hudson.ExtensionList;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.junit.runner.RunWith;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class ConfigurationUtilsTest {

    @Rule
    public final JenkinsRule jenkins = new JenkinsRule();

    @Test
    @Parameters({"true", "false"})
    public void verifyGetCollectDiskUsage(boolean value) {
        // given
        List<PrometheusConfiguration> extensions = ExtensionList.lookup(PrometheusConfiguration.class);
        PrometheusConfiguration configuration = extensions.get(0);
        configuration.setCollectDiskUsage(value);

        // when
        boolean result = ConfigurationUtils.getCollectDiskUsage();

        // then
        assertThat(result).isEqualTo(value);
    }
}
