package org.prebid.server.auction;

import com.iab.openrtb.request.BidRequest;
import org.junit.Before;
import org.junit.Test;
import org.prebid.server.VertxTest;
import org.prebid.server.proto.openrtb.ext.request.ExtRequest;
import org.prebid.server.proto.openrtb.ext.request.ExtRequestPrebid;
import org.prebid.server.proto.openrtb.ext.request.ExtRequestPrebidSchain;
import org.prebid.server.proto.openrtb.ext.request.ExtRequestPrebidSchainSchain;
import org.prebid.server.proto.openrtb.ext.request.ExtRequestPrebidSchainSchainNode;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class SchainResolverTest extends VertxTest {

    private SchainResolver schainResolver;

    @Before
    public void setUp() {
        schainResolver = SchainResolver.create(null, jacksonMapper);
    }

    @Test
    public void shouldResolveSchainsWhenCatchAllPresent() {
        // given
        final ExtRequestPrebidSchainSchainNode specificNodes = ExtRequestPrebidSchainSchainNode.of(
                "asi", "sid", 1, "rid", "name", "domain", null);
        final ExtRequestPrebidSchainSchain specificSchain = ExtRequestPrebidSchainSchain.of(
                "ver", 1, singletonList(specificNodes), null);
        final ExtRequestPrebidSchain schainForBidders = ExtRequestPrebidSchain.of(
                asList("bidder1", "bidder2"), specificSchain);

        final ExtRequestPrebidSchainSchainNode generalNodes = ExtRequestPrebidSchainSchainNode.of(
                "t", null, 0, "a", null, "ads", null);
        final ExtRequestPrebidSchainSchain generalSchain = ExtRequestPrebidSchainSchain.of(
                "t", 123, singletonList(generalNodes), null);
        final ExtRequestPrebidSchain allSchain = ExtRequestPrebidSchain.of(singletonList("*"), generalSchain);

        final BidRequest bidRequest = BidRequest.builder()
                .ext(ExtRequest.of(ExtRequestPrebid.builder()
                        .schains(asList(schainForBidders, allSchain))
                        .build()))
                .build();

        // when and then
        assertThat(schainResolver.resolveForBidder("bidder1", bidRequest)).isSameAs(specificSchain);
        assertThat(schainResolver.resolveForBidder("bidder2", bidRequest)).isSameAs(specificSchain);
        assertThat(schainResolver.resolveForBidder("bidder3", bidRequest)).isSameAs(generalSchain);
    }

    @Test
    public void shouldReturnNullWhenAbsentForBidderAndNoCatchAll() {
        // given
        final ExtRequestPrebidSchainSchainNode specificNodes = ExtRequestPrebidSchainSchainNode.of(
                "asi", "sid", 1, "rid", "name", "domain", null);
        final ExtRequestPrebidSchainSchain specificSchain = ExtRequestPrebidSchainSchain.of(
                "ver", 1, singletonList(specificNodes), null);
        final ExtRequestPrebidSchain schainForBidders = ExtRequestPrebidSchain.of(
                singletonList("bidder1"), specificSchain);

        final BidRequest bidRequest = BidRequest.builder()
                .ext(ExtRequest.of(ExtRequestPrebid.builder()
                        .schains(singletonList(schainForBidders))
                        .build()))
                .build();

        // when and then
        assertThat(schainResolver.resolveForBidder("bidder2", bidRequest)).isNull();
    }

    @Test
    public void shouldIgnoreDuplicatedBidderSchains() {
        // given
        final ExtRequestPrebidSchain schain1 = ExtRequestPrebidSchain.of(
                singletonList("bidder"), ExtRequestPrebidSchainSchain.of("ver1", null, null, null));
        final ExtRequestPrebidSchain schain2 = ExtRequestPrebidSchain.of(
                singletonList("bidder"), ExtRequestPrebidSchainSchain.of("ver2", null, null, null));

        final BidRequest bidRequest = BidRequest.builder()
                .ext(ExtRequest.of(ExtRequestPrebid.builder()
                        .schains(asList(schain1, schain2))
                        .build()))
                .build();

        // when and then
        assertThat(schainResolver.resolveForBidder("bidder", bidRequest)).isNull();
    }

    @Test
    public void shouldInjectGlobalNodeIntoResolvedSchain() {
        // given
        schainResolver = SchainResolver.create(
                "{\"asi\": \"pbshostcompany.com\", \"sid\":\"00001\"}",
                jacksonMapper);

        final ExtRequestPrebidSchainSchainNode node = ExtRequestPrebidSchainSchainNode.of(
                "asi", "sid", 1, "rid", "name", "domain", null);
        final ExtRequestPrebidSchainSchain schain = ExtRequestPrebidSchainSchain.of(
                "ver", 1, singletonList(node), null);
        final ExtRequestPrebidSchain schainEntry = ExtRequestPrebidSchain.of(
                singletonList("bidder"), schain);

        final BidRequest bidRequest = BidRequest.builder()
                .ext(ExtRequest.of(ExtRequestPrebid.builder()
                        .schains(singletonList(schainEntry))
                        .build()))
                .build();

        // when and then
        final ExtRequestPrebidSchainSchainNode globalNode = ExtRequestPrebidSchainSchainNode.of(
                "pbshostcompany.com", "00001", null, null, null, null, null);
        final ExtRequestPrebidSchainSchain expectedSchain = ExtRequestPrebidSchainSchain.of(
                "ver", 1, asList(node, globalNode), null);
        assertThat(schainResolver.resolveForBidder("bidder", bidRequest)).isEqualTo(expectedSchain);
    }

    @Test
    public void shouldReturnSchainWithGlobalNodeOnly() {
        // given
        schainResolver = SchainResolver.create(
                "{\"asi\": \"pbshostcompany.com\", \"sid\":\"00001\"}",
                jacksonMapper);

        final BidRequest bidRequest = BidRequest.builder().build();

        // when and then
        final ExtRequestPrebidSchainSchainNode globalNode = ExtRequestPrebidSchainSchainNode.of(
                "pbshostcompany.com", "00001", null, null, null, null, null);
        final ExtRequestPrebidSchainSchain expectedSchain = ExtRequestPrebidSchainSchain.of(
                null, null, singletonList(globalNode), null);
        assertThat(schainResolver.resolveForBidder("bidder", bidRequest)).isEqualTo(expectedSchain);
    }
}
