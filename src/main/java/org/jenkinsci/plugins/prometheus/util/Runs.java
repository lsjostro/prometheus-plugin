package org.jenkinsci.plugins.prometheus.util;

import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Result;
import hudson.model.Run;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Runs {
    public static boolean includeBuildInMetrics(Run build) {
        return !build.isBuilding();
    }

    public static String getResultText(Run run) {
        if (run != null) {
            Result result = run.getResult();
            if (result != null) {
                return result.toString();
            }
        }
        return null;
    }

    public static Map<String, Object> getBuildParameters(Run build) {
        List<ParametersAction> actions = build.getActions(ParametersAction.class);
        if (actions != null) {
            Map<String, Object> answer = new HashMap<String, Object>();
            for (ParametersAction action : actions) {
                List<ParameterValue> parameters = action.getParameters();
                if (parameters != null) {
                    for (ParameterValue parameter : parameters) {
                        String name = parameter.getName();
                        Object value = parameter.getValue();
                        answer.put(name, value);
                    }
                }
            }
            return answer;
        }
        return null;
    }
}