package org.jenkinsci.plugins.prometheus.rest;

import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;

import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.jenkinsci.plugins.prometheus.service.PrometheusMetrics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.prometheus.client.exporter.common.TextFormat;
import jenkins.metrics.api.Metrics;
import jenkins.model.Jenkins;


@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class})
// PowerMockIgnore needed for: https://github.com/powermock/powermock/issues/864
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "com.sun.org.apache.xalan.*"})
public class PrometheusActionTest {

    @Mock
    private Jenkins jenkins;

    @Mock
    private PrometheusConfiguration configuration;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Jenkins.class);
        PowerMockito.when(Jenkins.getInstance()).thenReturn(jenkins);
        PowerMockito.when(jenkins.getDescriptor(PrometheusConfiguration.class)).thenReturn(configuration);
        PowerMockito.when(configuration.getAdditionalPath()).thenReturn("prometheus");
    }

    @Test
    public void shouldThrowExceptionWhenDoesNotMatchPath() throws IOException, ServletException {
        // given
        PrometheusAction action = new PrometheusAction();
        StaplerRequest request = Mockito.mock(StaplerRequest.class);
        String url = "";
        Mockito.when(request.getRestOfPath()).thenReturn(url);

        // when
        HttpResponse actual = action.doDynamic(request);

        // then
        AssertStaplerResponse.from(actual)
                .call()
                .assertHttpStatus(HTTP_NOT_FOUND);
    }

    @Test
    public void shouldThrowExceptionWhenAuthenticationEnabledAndInsufficientPermission() throws IOException, ServletException {
        // given
        PrometheusAction action = new PrometheusAction();
        StaplerRequest request = Mockito.mock(StaplerRequest.class);
        String url = "prometheus";
        Mockito.when(request.getRestOfPath()).thenReturn(url);
        Mockito.when(configuration.isUseAuthenticatedEndpoint()).thenReturn(true);
        Mockito.when(jenkins.hasPermission(Metrics.VIEW)).thenReturn(false);

        // when
        HttpResponse actual = action.doDynamic(request);

        // then
        AssertStaplerResponse.from(actual)
                .call()
                .assertHttpStatus(HTTP_FORBIDDEN);
    }

    @Test
    public void shouldReturnMetrics() throws IOException, ServletException {
        // given
        PrometheusAction action = new PrometheusAction();
        PrometheusMetrics prometheusMetrics = Mockito.mock(PrometheusMetrics.class);
        String responseBody = "testMetric";
        Mockito.when(prometheusMetrics.getMetrics()).thenReturn(responseBody);
        action.setPrometheusMetrics(prometheusMetrics);
        StaplerRequest request = Mockito.mock(StaplerRequest.class);
        String url = "prometheus";
        Mockito.when(request.getRestOfPath()).thenReturn(url);

        // when
        HttpResponse actual = action.doDynamic(request);

        // then
        AssertStaplerResponse.from(actual)
                .call()
                .assertHttpStatus(HTTP_OK)
                .assertContentType(TextFormat.CONTENT_TYPE_004)
                .assertHttpHeader("Cache-Control", "must-revalidate,no-cache,no-store")
                .assertBody(responseBody);
    }


    private static class AssertStaplerResponse {
        private final StaplerResponse response;
        private final HttpResponse httpResponse;
        private final StringWriter stringWriter;

        private AssertStaplerResponse(HttpResponse httpResponse) throws IOException {
            this.httpResponse = httpResponse;
            this.response = Mockito.mock(StaplerResponse.class);
            stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            Mockito.when(response.getWriter()).thenReturn(writer);
        }

        static AssertStaplerResponse from(HttpResponse actual) throws IOException {
            return new AssertStaplerResponse(actual);
        }

        private AssertStaplerResponse assertHttpStatus(int status) {
            Mockito.verify(response).setStatus(status);
            return this;
        }

        private AssertStaplerResponse assertContentType(String contentType) {
            Mockito.verify(response).setContentType(contentType);
            return this;
        }

        private AssertStaplerResponse assertHttpHeader(String name, String value) {
            Mockito.verify(response).addHeader(name, value);
            return this;
        }

        private AssertStaplerResponse assertBody(String payload) {
            assertEquals(payload, stringWriter.toString());
            return this;
        }

        private AssertStaplerResponse call() throws IOException, ServletException {
            httpResponse.generateResponse(null, response, null);
            return this;
        }
    }

}
