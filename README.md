# Jenkins Prometheus Metrics Plugin

## About
Jenkins Prometheus Plugin expose an endpoint (default `/prometheus`) with metrics where a Prometheus Server can scrape.

Documentation can be found [here](https://plugins.jenkins.io/prometheus)

Please note that the documentation is a WIP.

## Metrics exposed
Currently only metrics from the [Metrics-plugin](https://github.com/jenkinsci/metrics-plugin) and summary of build
duration of jobs and pipeline stages

## Environment variables

`PROMETHEUS_NAMESPACE` Prefix of metric (Default: `default`).

`PROMETHEUS_ENDPOINT` REST Endpoint (Default: `prometheus`)

`COLLECTING_METRICS_PERIOD_IN_SECONDS` Async task period in seconds (Default: `120` seconds)

## Building

    mvn clean install
    mvn hpi:hpi

## Author / Maintainer
[Lars Sjöström](https://github.com/lsjostro)

[Marky Jackson](https://github.com/markyjackson-taulia)

# Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## Opening or finding an issue
OPENING AN ISSUE:
You should usually open an issue in the following situations:

Report an error you can’t solve yourself
Discuss a high-level topic or idea (for example, community, vision or policies)
Propose a new feature or other project idea

FINDING AN ISSUE:
If you found an open issue that you want to tackle, comment on the issue to let people know you’re on it. That way, people are less likely to duplicate your work.

If an issue was opened a while ago, it’s possible that it’s being addressed somewhere else, or has already been resolved, so comment to ask for confirmation before starting work.

## Opening a pull request
You should usually open a pull request in the following situations:

Submit trivial fixes (for example, a typo, a broken link or an obvious error)
Start work on a contribution that was already asked for, or that you’ve already discussed, in an issue

### Forking a repository
Fork the repository and clone it locally. Connect your local to the original “upstream” repository by adding it as a remote. Pull in changes from “upstream” often so that you stay up to date so that when you submit your pull request, merge conflicts will be less likely.

Create a branch for your edits.

Reference any relevant issues or supporting documentation in your PR (for example, “Closes #37.”)

Include screenshots of the before and after if your changes include differences in HTML/CSS. Drag and drop the images into the body of your pull request.

Test your changes! Run your changes against any existing tests if they exist and create new ones when needed. Whether tests exist or not, make sure your changes don’t break the existing project.

###  Steps to make a Pull Request

```bash
git clone https://github.com/your_github_username/prometheus-plugin.git

cd prometheus-plugin

git checkout -b branchname

git diff

git add -A

git status

git commit -m "Write the change you have made"

git status

git push origin branchname

```

## And Voila, you have made your PR! 
