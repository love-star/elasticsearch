/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.compute.data;

import org.elasticsearch.TransportVersion;
import org.elasticsearch.common.breaker.CircuitBreaker;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.stream.ByteBufferStreamInput;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.common.util.MockBigArrays;
import org.elasticsearch.common.util.PageCacheRecycler;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.TransportVersionUtils;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.hamcrest.Matchers.equalTo;

public abstract class SerializationTestCase extends ESTestCase {
    BigArrays bigArrays;
    protected BlockFactory blockFactory;

    @Before
    public final void newBlockFactory() {
        bigArrays = new MockBigArrays(PageCacheRecycler.NON_RECYCLING_INSTANCE, ByteSizeValue.ofGb(1)).withCircuitBreaking();
        blockFactory = new BlockFactory(bigArrays.breakerService().getBreaker(CircuitBreaker.REQUEST), bigArrays);
    }

    @After
    public final void blockFactoryEmpty() {
        assertThat(blockFactory.breaker().getUsed(), equalTo(0L));
        blockFactory = null;
    }

    Page serializeDeserializePage(Page origPage) throws IOException {
        try (BytesStreamOutput out = new BytesStreamOutput()) {
            origPage.writeTo(out);
            return new Page(blockStreamInput(out));
        }
    }

    BlockStreamInput blockStreamInput(BytesStreamOutput out) {
        return new BlockStreamInput(ByteBufferStreamInput.wrap(BytesReference.toBytes(out.bytes())), blockFactory);
    }

    <T extends Block> T serializeDeserializeBlock(T origBlock) throws IOException {
        TransportVersion version = TransportVersionUtils.randomCompatibleVersion(random());
        return serializeDeserializeBlockWithVersion(origBlock, version);
    }

    @SuppressWarnings("unchecked")
    <T extends Block> T serializeDeserializeBlockWithVersion(T origBlock, TransportVersion version) throws IOException {
        try (BytesStreamOutput out = new BytesStreamOutput()) {
            out.setTransportVersion(version);
            Block.writeTypedBlock(origBlock, out);
            try (BlockStreamInput in = blockStreamInput(out)) {
                in.setTransportVersion(version);
                return (T) Block.readTypedBlock(in);
            }
        }
    }

    <T extends Block> T uncheckedSerializeDeserializeBlock(T origBlock) {
        try {
            return serializeDeserializeBlock(origBlock);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
