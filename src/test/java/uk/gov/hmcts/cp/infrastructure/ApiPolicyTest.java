package uk.gov.hmcts.cp.infrastructure;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApiPolicyTest {

    private static final Path POLICY = Path.of("infrastructure/policies/api-policy.xml");

    private static Document policy;

    @BeforeAll
    static void parsePolicy() throws Exception {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setXIncludeAware(false);
        policy = factory.newDocumentBuilder().parse(POLICY.toFile());
    }

    @Test
    void every_section_should_declare_base_exactly_once() {
        assertThat(childrenOf("inbound", "base")).hasSize(1);
        assertThat(childrenOf("outbound", "base")).hasSize(1);
        assertThat(childrenOf("on-error", "base")).hasSize(1);
    }

    @Test
    void inbound_should_run_base_first_so_an_early_return_cannot_skip_global_policy() {
        assertThat(childrenOf("inbound").getFirst().getTagName()).isEqualTo("base");
    }

    @Test
    void rate_limiting_should_run_before_authentication_so_rejected_calls_are_counted() {
        assertThat(positionOf("rate-limit-by-key")).isLessThan(positionOf("choose"));
    }

    @Test
    void daily_quota_should_run_after_authentication_so_only_real_usage_is_metered() {
        assertThat(positionOf("quota-by-key")).isGreaterThan(positionOf("choose"));
    }

    private int positionOf(final String tagName) {
        final List<Element> inbound = childrenOf("inbound");
        for (int i = 0; i < inbound.size(); i++) {
            if (inbound.get(i).getTagName().equals(tagName)) {
                return i;
            }
        }
        throw new AssertionError("inbound has no <" + tagName + ">");
    }

    private Element sectionNamed(final String name) {
        return (Element) policy.getElementsByTagName(name).item(0);
    }

    private List<Element> childrenOf(final String section) {
        final List<Element> children = new ArrayList<>();
        final NodeList nodes = sectionNamed(section).getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                children.add((Element) nodes.item(i));
            }
        }
        return children;
    }

    private List<Element> childrenOf(final String section, final String tagName) {
        return childrenOf(section).stream().filter(e -> e.getTagName().equals(tagName)).toList();
    }
}
