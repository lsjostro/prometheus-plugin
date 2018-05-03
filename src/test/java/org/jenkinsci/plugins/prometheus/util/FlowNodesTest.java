package org.jenkinsci.plugins.prometheus.util;

import org.jenkinsci.plugins.workflow.actions.StageAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import static org.jenkinsci.plugins.prometheus.util.FlowNodes.getSortedStageNodes;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FlowNodesTest {

    @Test
    public void validateAbortWhenSameNodeIsEncountered() {
        FlowNode topNode = mock(FlowNode.class);
        List<FlowNode> nodeList = new ArrayList<>();
        nodeList.add(topNode);
        when(topNode.getId()).thenReturn("999");
        when(topNode.getParents()).thenReturn(nodeList);
        when(topNode.getAction(StageAction.class)).thenReturn(mock(StageAction.class));
        List<FlowNode> sortedNodes = getSortedStageNodes(nodeList);
        assertEquals(1, sortedNodes.size());
    }
}
