package org.jenkinsci.plugins.prometheus.util;

import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

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
        when(topNode.getAction(LabelAction.class)).thenReturn(mock(LabelAction.class));
        List<FlowNode> sortedNodes = getSortedStageNodes(nodeList);
        assertThat(sortedNodes).hasSize(1);
    }
}
