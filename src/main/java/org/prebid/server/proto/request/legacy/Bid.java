package org.prebid.server.proto.request.legacy;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Value;

@Deprecated
@AllArgsConstructor(staticName = "of")
@Value
public class Bid {

    /* Unique bid ID for this bidder for this slot. */
    String bidId;

    /* Unique code for an adapter to call. */
    String bidder;

    /* Optional params to send to the adapter. */
    ObjectNode params;
}
