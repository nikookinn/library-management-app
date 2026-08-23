package com.nikookinn.librarymanagement.logging;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("Correlation Id Filter Unit Tests")
class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Nested
    @DisplayName("doFilter")
    class DoFilter {

        @Test
        @DisplayName("should generate correlation id when none provided")
        void shouldGenerateCorrelationIdWhenNoneProvided() throws Exception {
            // Arrange
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/books");
            MockHttpServletResponse response = new MockHttpServletResponse();
            AtomicReference<String> mdcValueDuringRequest = new AtomicReference<>();
            FilterChain chain = (req, res) -> mdcValueDuringRequest.set(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY));

            // Act
            filter.doFilter(request, response, chain);

            // Assert
            String responseHeader = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
            assertThat(responseHeader).isNotBlank();
            assertThat(mdcValueDuringRequest.get()).isEqualTo(responseHeader);
        }

        @Test
        @DisplayName("should clear MDC after request completes")
        void shouldClearMdcAfterRequestCompletes() throws Exception {
            // Arrange
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/books");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            // Act
            filter.doFilter(request, response, chain);

            // Assert
            assertThat(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY)).isNull();
        }

        @Test
        @DisplayName("should reuse incoming correlation id header")
        void shouldReuseIncomingCorrelationIdHeader() throws Exception {
            // Arrange
            String incomingId = "client-supplied-id-123";
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/books");
            request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, incomingId);
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            // Act
            filter.doFilter(request, response, chain);

            // Assert
            assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo(incomingId);
            verify(chain).doFilter(request, response);
        }

        @Test
        @DisplayName("should clear MDC even when downstream filter throws")
        void shouldClearMdcEvenWhenDownstreamThrows() {
            // Arrange
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/books");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = (req, res) -> {
                throw new IllegalStateException("boom");
            };

            // Act & Assert
            assertThatThrownBy(() -> filter.doFilter(request, response, chain))
                    .isInstanceOf(IllegalStateException.class);

            assertThat(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY)).isNull();
        }
    }
}
