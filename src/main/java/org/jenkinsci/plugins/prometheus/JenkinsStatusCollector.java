package org.jenkinsci.plugins.prometheus;

import hudson.model.Computer;
import io.prometheus.client.Collector;
import io.prometheus.client.Gauge;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;

import java.util.ArrayList;
import java.util.List;

public class JenkinsStatusCollector extends Collector {

    @Override
    public List<MetricFamilySamples> collect() {
        String subsystem = "jenkins";
        String namespace = ConfigurationUtils.getNamespace();
        List<MetricFamilySamples> samples = new ArrayList<>();
        Gauge jenkinsUp = Gauge.build().
                name("up").
                labelNames().
                subsystem(subsystem).
                namespace(namespace).
                help("Is Jenkins ready to receive requests").
                create();
        Jenkins jenkins = Jenkins.getInstance();
        jenkinsUp.set(jenkins.getInitLevel() == hudson.init.InitMilestone.COMPLETED ?
                1 : 0);
        samples.addAll(jenkinsUp.collect());
        Gauge jenkinsUptime = Gauge.build().
                name("uptime").
                labelNames().
                subsystem(subsystem).
                namespace(namespace).
                help("Time since Jenkins machine was initialized").
                create();
        Computer computer = jenkins.toComputer();
        if (computer != null) {
            long upTime = computer.getConnectTime();
            jenkinsUptime.set(System.currentTimeMillis() - upTime);
            samples.addAll(jenkinsUptime.collect());
        }
        return samples;
    }
}
