package org.jenkinsci.plugins.prometheus;

import hudson.model.AdministrativeMonitor;
import io.prometheus.client.Collector;
import io.prometheus.client.Gauge;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;

import java.util.ArrayList;
import java.util.List;

public class JenkinsStatusCollector extends Collector {

    private Gauge jenkinsUp;
    private Gauge jenkinsUptime;

    @Override
    public List<MetricFamilySamples> collect() {
        final String subsystem = "jenkins";
        final String namespace = ConfigurationUtils.getNamespace();
        final List<MetricFamilySamples> samples = new ArrayList<>();

        long upTime = Jenkins.getInstance().toComputer().getConnectTime();
        System.out.println(upTime);
        jenkinsUp = Gauge.build().
                name("up").
                subsystem(subsystem).namespace(namespace).
                help("Is Jenkins ready to receive requests").
                create();
        jenkinsUp.set(Jenkins.getInstance().getInitLevel() == hudson.init.InitMilestone.COMPLETED ?
                1 : 0);
        samples.addAll(jenkinsUp.collect());
        jenkinsUptime = Gauge.build().
                name("uptime").
                subsystem(subsystem).namespace(namespace).
                help("Time since Jenkins machine was initialized").
                create();
        jenkinsUptime.set(System.currentTimeMillis() - upTime);
        samples.addAll(jenkinsUptime.collect());
        return samples;
    }
}
