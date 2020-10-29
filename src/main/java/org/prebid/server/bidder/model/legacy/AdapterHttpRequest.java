package org.prebid.server.bidder.model.legacy;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Packages together the fields needed to make an http request.
 */
@Deprecated
@AllArgsConstructor(staticName = "of")
@Value
public class AdapterHttpRequest<T> {

    HttpMethod method;

    String uri;

    T payload;

    MultiMap headers;

}
