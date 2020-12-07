package org.jenkinsci.plugins.prometheus;

import com.cloudbees.workflow.rest.external.StageNodeExt;
import com.cloudbees.workflow.rest.external.StatusExt;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import io.prometheus.client.Collector;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;
import org.jenkinsci.plugins.prometheus.util.Jobs;
import org.jenkinsci.plugins.prometheus.util.Runs;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static org.jenkinsci.plugins.prometheus.util.FlowNodes.getSortedStageNodes;

public class JobCollector extends Collector {

    private static final Logger logger = LoggerFactory.getLogger(JobCollector.class);

    private Summary summary;
    private Counter jobSuccessCount;
    private Counter jobFailedCount;
    private Gauge jobLastBuildResultOrdinal;
    private Gauge jobLastBuildResult;
    private Gauge jobLastBuildStartMillis;
    private Gauge jobLastBuildDuration;
    private Gauge jobHealthScore;
    private Gauge jobLastBuildTestsTotal;
    private Gauge jobLastBuildTestsSkipped;
    private Gauge jobLastBuildTestsFailing;
    private Gauge jobBuildResultOrdinal;
    private Gauge jobBuildResult;
    private Gauge jobBuildStartMillis;
    private Gauge jobBuildDuration;
    private Gauge jobBuildTestsTotal;
    private Gauge jobBuildTestsSkipped;
    private Gauge jobBuildTestsFailing;
    private Summary stageSummary;

    public JobCollector() {
    }

    @Override
    public List<MetricFamilySamples> collect() {
        logger.debug("Collecting metrics for prometheus");

        String namespace = ConfigurationUtils.getNamespace();
        List<MetricFamilySamples> samples = new ArrayList<>();
        String fullname = "builds";
        String subsystem = ConfigurationUtils.getSubSystem();
        String jobAttribute = PrometheusConfiguration.get().getJobAttributeName();
        String[] labelBaseNameArray = {jobAttribute, "repo"};
        String[] labelBuildNameArray = Arrays.copyOf(labelBaseNameArray, labelBaseNameArray.length + 3);
        labelBuildNameArray[labelBaseNameArray.length] = "number";
        labelBuildNameArray[labelBaseNameArray.length + 1] = "parameters";
        labelBuildNameArray[labelBaseNameArray.length + 2] = "status";
        String[] labelStageNameArray = Arrays.copyOf(labelBaseNameArray, labelBaseNameArray.length + 1);
        labelStageNameArray[labelBaseNameArray.length] = "stage";
        boolean processDisabledJobs = PrometheusConfiguration.get().isProcessingDisabledBuilds();
        boolean ignoreBuildMetrics =
                !PrometheusConfiguration.get().isCountAbortedBuilds() &&
                        !PrometheusConfiguration.get().isCountFailedBuilds() &&
                        !PrometheusConfiguration.get().isCountNotBuiltBuilds() &&
                        !PrometheusConfiguration.get().isCountSuccessfulBuilds() &&
                        !PrometheusConfiguration.get().isCountUnstableBuilds();

        if (ignoreBuildMetrics) {
            return samples;
        }

        summary = Summary.build()
                .name(fullname + "_duration_milliseconds_summary")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelBaseNameArray)
                .help("Summary of Jenkins build times in milliseconds by Job")
                .create();

        jobSuccessCount = Counter.build()
                .name(fullname + "_success_build_count")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelBaseNameArray)
                .help("Successful build count")
                .create();

        jobFailedCount = Counter.build()
                .name(fullname + "_failed_build_count")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelBaseNameArray)
                .help("Failed build count")
                .create();

        jobLastBuildResultOrdinal = Gauge.build()
                .name(fullname + "_last_build_result_ordinal")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelBaseNameArray)
                .help("Build status of a job.")
                .create();

        jobLastBuildResult = Gauge.build()
                .name(fullname + "_last_build_result")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelBaseNameArray)
                .help("Build status of a job as a boolean (0 or 1)")
                .create();

        jobLastBuildDuration = Gauge.build()
                .name(fullname + "_last_build_duration_milliseconds")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelBaseNameArray)
                .help("Build times in milliseconds of last build")
                .create();

        jobLastBuildStartMillis = Gauge.build()
                .name(fullname + "_last_build_start_time_milliseconds")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelBaseNameArray)
                .help("Last build start timestamp in milliseconds")
                .create();

        jobHealthScore = Gauge.build()
                .name(fullname + "_health_score")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelBaseNameArray)
                .help("Health score of a job")
                .create();

        jobLastBuildTestsTotal = Gauge.build()
                .name(fullname + "_last_build_tests_total")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelBaseNameArray)
                .help("Number of total tests during the last build")
                .create();

        jobLastBuildTestsSkipped = Gauge.build()
                .name(fullname + "_last_build_tests_skipped")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelBaseNameArray)
                .help("Number of skipped tests during the last build")
                .create();

        jobLastBuildTestsFailing = Gauge.build()
                .name(fullname + "_last_build_tests_failing")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelBaseNameArray)
                .help("Number of failing tests during the last build")
                .create();

        jobBuildResultOrdinal = Gauge.build()
                .name(fullname + "_build_result_ordinal")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelBuildNameArray)
                .help("Build status of a job.")
                .create();

        jobBuildResult = Gauge.build()
                .name(fullname + "_build_result")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelBuildNameArray)
                .help("Build status of a job as a boolean (0 or 1)")
                .create();

        jobBuildDuration = Gauge.build()
                .name(fullname + "_build_duration_milliseconds")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelBuildNameArray)
                .help("Build times in milliseconds of a build")
                .create();

        jobBuildStartMillis = Gauge.build()
                .name(fullname + "_build_start_time_milliseconds")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelBuildNameArray)
                .help("Build start timestamp in milliseconds")
                .create();

        jobBuildTestsTotal = Gauge.build()
                .name(fullname + "_build_tests_total")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelBuildNameArray)
                .help("Number of total tests during a build")
                .create();

        jobBuildTestsSkipped = Gauge.build()
                .name(fullname + "_build_tests_skipped")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelBuildNameArray)
                .help("Number of skipped tests during a build")
                .create();

        jobBuildTestsFailing = Gauge.build()
                .name(fullname + "_build_tests_failing")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelBuildNameArray)
                .help("Number of failing tests during a build")
                .create();

        stageSummary = Summary.build().name(fullname + "_stage_duration_milliseconds_summary")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelStageNameArray)
                .help("Summary of Jenkins build times by Job and Stage")
                .create();

        Jobs.forEachJob(job -> {
            if (!job.isBuildable() && processDisabledJobs) {
                logger.debug("job [{}] is disabled", job.getFullName());
                return;
            }
            logger.debug("Collecting metrics for job [{}]", job.getFullName());
            appendJobMetrics(job);
        });

        addSamples(samples, summary.collect(), "Adding [{}] samples from summary");
        addSamples(samples, jobSuccessCount.collect(), "Adding [{}] samples from counter");
        addSamples(samples, jobFailedCount.collect(), "Adding [{}] samples from counter");
        addSamples(samples, jobHealthScore.collect(), "Adding [{}] samples from gauge");
        addSamples(samples, jobLastBuildResultOrdinal.collect(), "Adding [{}] samples from gauge");
        addSamples(samples, jobLastBuildResult.collect(), "Adding [{}] samples from gauge");
        addSamples(samples, jobLastBuildDuration.collect(), "Adding [{}] samples from gauge");
        addSamples(samples, jobLastBuildStartMillis.collect(), "Adding [{}] samples from gauge");
        addSamples(samples, jobLastBuildTestsTotal.collect(), "Adding [{}] samples from gauge");
        addSamples(samples, jobLastBuildTestsSkipped.collect(), "Adding [{}] samples from gauge");
        addSamples(samples, jobLastBuildTestsFailing.collect(), "Adding [{}] samples from gauge");
        addSamples(samples, jobBuildResultOrdinal.collect(), "Adding [{}] samples from gauge");
        addSamples(samples, jobBuildResult.collect(), "Adding [{}] samples from gauge");
        addSamples(samples, jobBuildDuration.collect(), "Adding [{}] samples from gauge");
        addSamples(samples, jobBuildStartMillis.collect(), "Adding [{}] samples from gauge");
        addSamples(samples, jobBuildTestsTotal.collect(), "Adding [{}] samples from gauge");
        addSamples(samples, jobBuildTestsSkipped.collect(), "Adding [{}] samples from gauge");
        addSamples(samples, jobBuildTestsFailing.collect(), "Adding [{}] samples from gauge");
        addSamples(samples, stageSummary.collect(), "Adding [{}] samples from summary");

        return samples;
    }

    private void addSamples(List<MetricFamilySamples> allSamples, List<MetricFamilySamples> newSamples, String logMessage) {
        int sampleCount = newSamples.get(0).samples.size();
        if (sampleCount > 0) {
            logger.debug(logMessage, sampleCount);
            allSamples.addAll(newSamples);
        }
    }

    protected void appendJobMetrics(Job job) {
        // Add this to the repo as well so I can group by Github Repository
        String repoName = StringUtils.substringBetween(job.getFullName(), "/");
        if (repoName == null) {
            repoName = "NA";
        }
        String[] labelValueArray = {job.getFullName(), repoName};

        Run run = job.getLastBuild();
        // Never built
        if (null == run) {
            logger.debug("job [{}] never built", job.getFullName());
            return;
        }

        /*
         * _last_build_result _last_build_result_ordinal
         *
         * SUCCESS   0 true  - The build had no errors.
         * UNSTABLE  1 true  - The build had some errors but they were not fatal. For example, some tests failed.
         * FAILURE   2 false - The build had a fatal error.
         * NOT_BUILT 3 false - The module was not built.
         * ABORTED   4 false - The build was manually aborted.
         */
        int ordinal = -1; // running
        // Job is running
        Result runResult = run.getResult();
        if (null != runResult) {
            ordinal = runResult.ordinal;
        }
        long millis = run.getStartTimeInMillis();
        long duration = run.getDuration();
        int score = job.getBuildHealth().getScore();

        jobLastBuildStartMillis.labels(labelValueArray).set(millis);
        if (!run.isBuilding()) {
            jobLastBuildResultOrdinal.labels(labelValueArray).set(ordinal);
            jobLastBuildResult.labels(labelValueArray).set(ordinal < 2 ? 1 : 0);
            jobLastBuildDuration.labels(labelValueArray).set(duration);
            jobHealthScore.labels(labelValueArray).set(score);
        }

        if (PrometheusConfiguration.get().isFetchTestResults() && hasTestResults(run) && !run.isBuilding()) {
            int testsTotal = run.getAction(AbstractTestResultAction.class).getTotalCount();
            int testsFail = run.getAction(AbstractTestResultAction.class).getFailCount();
            int testsSkipped = run.getAction(AbstractTestResultAction.class).getSkipCount();

            jobLastBuildTestsTotal.labels(labelValueArray).set(testsTotal);
            jobLastBuildTestsSkipped.labels(labelValueArray).set(testsSkipped);
            jobLastBuildTestsFailing.labels(labelValueArray).set(testsFail);
        }

        while (run != null) {
            logger.debug("getting metrics for run [{}] from job [{}]", run.getNumber(), job.getName());
            if (Runs.includeBuildInMetrics(run)) {
                logger.debug("getting build info for run [{}] from job [{}]", run.getNumber(), job.getName());
                String params = Runs.getBuildParameters(run).entrySet().stream().map(e -> "" + e.getKey() + "=" + String.valueOf(e.getValue())).collect(Collectors.joining(";"));
                String resultString = "UNDEFINED";
                runResult = run.getResult();
                if (runResult != null) {
                    resultString = runResult.toString();
                }
                String[] BuildLabelValueArray = {job.getFullName(), repoName, String.valueOf(run.getNumber()), params, run.isBuilding() ? "RUNNING" : resultString};
                duration = run.getDuration();
                millis = run.getStartTimeInMillis();
                runResult = run.getResult();
                if (null != runResult) {
                    ordinal = runResult.ordinal;
                }

                jobBuildStartMillis.labels(BuildLabelValueArray).set(millis);
                if (run.isBuilding()) {
                    // Only set duration and result metrics for builds that are not running
                    continue;
                }
                jobBuildResultOrdinal.labels(BuildLabelValueArray).set(ordinal);
                jobBuildResult.labels(BuildLabelValueArray).set(ordinal < 2 ? 1 : 0);
                jobBuildDuration.labels(BuildLabelValueArray).set(duration);

                if (PrometheusConfiguration.get().isFetchTestResults() && hasTestResults(run) && !run.isBuilding()) {
                    int testsTotal = run.getAction(AbstractTestResultAction.class).getTotalCount();
                    int testsFail = run.getAction(AbstractTestResultAction.class).getFailCount();
                    int testsSkipped = run.getAction(AbstractTestResultAction.class).getSkipCount();

                    jobBuildTestsTotal.labels(BuildLabelValueArray).set(testsTotal);
                    jobBuildTestsSkipped.labels(BuildLabelValueArray).set(testsSkipped);
                    jobBuildTestsFailing.labels(BuildLabelValueArray).set(testsFail);
                }

                summary.labels(labelValueArray).observe(duration);
                if (runResult != null) {
                    if (runResult.ordinal == 0 || runResult.ordinal == 1) {
                        jobSuccessCount.labels(labelValueArray).inc();
                    } else if (runResult.ordinal > 1) {
                        jobFailedCount.labels(labelValueArray).inc();
                    }
                }
                if (run instanceof WorkflowRun) {
                    logger.debug("run [{}] from job [{}] is of type workflowRun", run.getNumber(), job.getName());
                    WorkflowRun workflowRun = (WorkflowRun) run;
                    if (workflowRun.getExecution() == null) {
                        run = run.getPreviousBuild();
                        continue;
                    }
                    try {
                        logger.debug("getting the sorted stage nodes for run[{}] from job [{}]", run.getNumber(), job.getName());
                        List<StageNodeExt> stages = getSortedStageNodes(workflowRun);
                        for (StageNodeExt stage : stages) {
                            observeStage(job, run, stage);
                        }
                    } catch (NullPointerException e) {
                        // ignored
                    }
                }
            }
            run = run.getPreviousBuild();
        }
    }

    private void observeStage(Job job, Run run, StageNodeExt stage) {
        logger.debug("Observing stage[{}] in run [{}] from job [{}]", stage.getName(), run.getNumber(), job.getName());
        // Add this to the repo as well so I can group by Github Repository
        String repoName = StringUtils.substringBetween(job.getFullName(), "/");
        if (repoName == null) {
            repoName = "NA";
        }
        String jobName = job.getFullName();
        String stageName = stage.getName();
        String[] labelValueArray = {jobName, repoName, stageName};

        if (stage.getStatus() == StatusExt.SUCCESS || stage.getStatus() == StatusExt.UNSTABLE) {
            logger.debug("getting duration for stage[{}] in run [{}] from job [{}]", stage.getName(), run.getNumber(), job.getName());
            long duration = stage.getDurationMillis();
            logger.debug("duration was [{}] for stage[{}] in run [{}] from job [{}]", duration, stage.getName(), run.getNumber(), job.getName());
            stageSummary.labels(labelValueArray).observe(duration);
        } else {
            logger.debug("Stage[{}] in run [{}] from job [{}] was not successful and will be ignored", stage.getName(), run.getNumber(), job.getName());
        }
    }

    private boolean hasTestResults(Run<?, ?> job) {
        return job.getAction(AbstractTestResultAction.class) != null;
    }
}
