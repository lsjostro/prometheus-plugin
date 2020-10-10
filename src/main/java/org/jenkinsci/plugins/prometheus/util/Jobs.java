package org.jenkinsci.plugins.prometheus.util;

import hudson.model.Job;
import jenkins.model.Jenkins;

import java.util.List;
import java.util.function.Consumer;

public class Jobs {
    public static void forEachJob(Consumer<Job> consumer) {
        List<Job> jobs = Jenkins.getInstance().getAllItems(Job.class);
        if (jobs != null) {
            for (Job item : jobs) {
                consumer.accept(item);
            }
        }
    }
}
