package org.jenkinsci.plugins.prometheus.util;

import hudson.model.Item;
import hudson.model.Job;
import jenkins.model.Jenkins;

import java.util.Collection;
import java.util.List;

public class Jobs {
    public static void forEachJob(Callback<Job> callback) {
        List<Item> items = Jenkins.getInstance().getAllItems();
        if (items != null) {
            for (Item item : items) {
                Collection<? extends Job> jobs = item.getAllJobs();
                if (jobs != null) {
                    for (Job job : jobs) {
                        if (job != null) {
                            callback.invoke(job);
                        }
                    }
                }
            }
        }
    }
}
