package org.jenkinsci.plugins.prometheus;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.StringWriter;

public class MetricsRequest {
    public static HttpResponse prometheusResponse(final CollectorRegistry collectorRegistry) {
        return new HttpResponse() {
            @Override
            public void generateResponse(StaplerRequest request, StaplerResponse response, Object node) throws IOException, ServletException {
                response.setStatus(StaplerResponse.SC_OK);
                response.setContentType(TextFormat.CONTENT_TYPE_004);
                response.addHeader("Cache-Control","must-revalidate,no-cache,no-store");


                StringWriter buffer = new StringWriter();
                TextFormat.write004(buffer, collectorRegistry.metricFamilySamples());
                buffer.close();
                response.getWriter().write(buffer.toString());
            }
        };
    }
}
