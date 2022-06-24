package org.jenkinsci.plugins.prometheus;

import hudson.model.Computer;
import hudson.model.Node;
import io.prometheus.client.Collector;
import io.prometheus.client.Gauge;
import io.prometheus.client.Info;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;

import java.util.ArrayList;
import java.util.List;

public class JenkinsStatusCollector extends Collector {
    protected String subsystem;
    protected String namespace;
    protected Jenkins jenkins;

    @Override
    public List<MetricFamilySamples> collect() {
        subsystem = ConfigurationUtils.getSubSystem();
        namespace = ConfigurationUtils.getNamespace();
        List<MetricFamilySamples> samples = new ArrayList<>();
        
        jenkins = Jenkins.get();
        Info jenkinsVersionInfo = Info.build()
                .name("version")
                .help("Jenkins Application Version")
                .subsystem(subsystem)
                .namespace(namespace)
                .create();
        jenkinsVersionInfo.info("version", Jenkins.VERSION);
        samples.addAll(jenkinsVersionInfo.collect());

        Gauge jenkinsUp = Gauge.build()
                .name("up")
                .labelNames()
                .subsystem(subsystem)
                .namespace(namespace)
                .help("Is Jenkins ready to receive requests")
                .create();
        jenkinsUp.set(jenkins.getInitLevel() == hudson.init.InitMilestone.COMPLETED ? 1 : 0);
        samples.addAll(jenkinsUp.collect());

        Gauge jenkinsUptime = Gauge.build()
                .name("uptime")
                .labelNames()
                .subsystem(subsystem)
                .namespace(namespace)
                .help("Time since Jenkins machine was initialized")
                .create();
        Computer computer = jenkins.toComputer();
        if (computer != null) {
            long upTime = computer.getConnectTime();
            jenkinsUptime.set(System.currentTimeMillis() - upTime);
            samples.addAll(jenkinsUptime.collect());
        }

        if (!PrometheusConfiguration.get().isCollectNodeStatus()) {
            return samples;
        }

        samples.addAll(collectNodeStatus());

        return samples;
    }

    protected List<MetricFamilySamples> collectNodeStatus() {
        Gauge jenkinsNodes = Gauge.build().
                name("nodes_online").
                subsystem(subsystem).
                namespace(namespace).
                help("Jenkins nodes online status").
                labelNames("node").
                create();

        for (Node node : jenkins.getNodes()) {
            //Check whether the node is online or offline
            Computer comp = node.toComputer();
            if (comp != null) {
                if (comp.isOnline()) { // https://javadoc.jenkins.io/hudson/model/Computer.html
                    jenkinsNodes.labels(node.getNodeName()).set(1);
                } else {
                    jenkinsNodes.labels(node.getNodeName()).set(0);
                }
            }
        }

        return jenkinsNodes.collect();
    }
}
