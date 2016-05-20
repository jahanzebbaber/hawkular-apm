/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.btm.processor.nodedetails;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.hawkular.btm.api.model.btxn.BusinessTransaction;
import org.hawkular.btm.api.model.btxn.Consumer;
import org.hawkular.btm.api.model.btxn.Producer;
import org.hawkular.btm.api.model.events.NodeDetails;
import org.junit.Test;

/**
 * @author gbrown
 */
public class NodeDetailsDeriverTest {

    /**  */
    private static final String INTERNAL_URI = "internalUri";

    /**  */
    private static final String TEST_URI = "testUri";

    @Test
    public void testProcessMultipleNotInternalConsumer() {
        NodeDetailsDeriver deriver = new NodeDetailsDeriver();

        BusinessTransaction btxn = new BusinessTransaction();

        Consumer consumer = new Consumer();
        consumer.setUri(INTERNAL_URI);
        btxn.getNodes().add(consumer);

        Producer producer = new Producer();
        producer.setEndpointType("HTTP");
        producer.setUri(TEST_URI);
        consumer.getNodes().add(producer);

        List<NodeDetails> details = null;

        try {
            details = deriver.processMultiple(null, btxn);
        } catch (Exception e) {
            fail("Failed: " + e);
        }

        assertNotNull(details);
        assertEquals(1, details.size());

        assertEquals(TEST_URI, details.get(0).getUri());
    }

    @Test
    public void testProcessMultipleNotInternalProducer() {
        NodeDetailsDeriver deriver = new NodeDetailsDeriver();

        BusinessTransaction btxn = new BusinessTransaction();

        Consumer consumer = new Consumer();
        consumer.setEndpointType("HTTP");
        consumer.setUri(TEST_URI);
        btxn.getNodes().add(consumer);

        Producer producer = new Producer();
        producer.setUri(INTERNAL_URI);
        consumer.getNodes().add(producer);

        List<NodeDetails> details = null;

        try {
            details = deriver.processMultiple(null, btxn);
        } catch (Exception e) {
            fail("Failed: " + e);
        }

        assertNotNull(details);
        assertEquals(1, details.size());

        assertEquals(TEST_URI, details.get(0).getUri());
    }

}
