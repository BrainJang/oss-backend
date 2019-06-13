package com.berry.oss.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.berry.oss.erasure.ReedSolomon;
import com.berry.oss.remote.IDataServiceClient;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Command-line program that decodes a file using Reed-Solomon 4+2.
 * <p>
 * The file name given should be the name of the file to decode, say
 * "foo.txt".  This program will expected to find "foo.txt.0" through
 * "foo.txt.5", with at most two missing.  It will then write
 * "foo.txt.decoded".
 */
@Service
public class ReedSolomonDecoderService {

    private static final int DATA_SHARDS = 4;
    private static final int PARITY_SHARDS = 2;
    private static final int TOTAL_SHARDS = 6;
    private static final int BYTES_IN_INT = 4;

    private final IDataServiceClient dataServiceClient;

    public ReedSolomonDecoderService(IDataServiceClient dataServiceClient) {
        this.dataServiceClient = dataServiceClient;
    }

    public InputStream readData(String shardJson) throws IOException {

        JSONArray jsonArray = JSONArray.parseArray(shardJson);

        final byte[][] shards = new byte[TOTAL_SHARDS][];
        final boolean[] shardPresent = new boolean[TOTAL_SHARDS];
        int shardSize = 0;
        int shardCount = 0;
        for (int i = 0; i < TOTAL_SHARDS; i++) {
            JSONObject shard = jsonArray.getJSONObject(i);
            String path = shard.getString("path");
            String ip = shard.getString("ip");
            byte[] bytes = dataServiceClient.readShard(path);
            shardSize= bytes.length;
            shards[i] = bytes;
            shardPresent[i] = true;
            shardCount += 1;
        }

        // We need at least DATA_SHARDS to be able to reconstruct the file.
        if (shardCount < DATA_SHARDS) {
            System.out.println("Not enough shards present");
            return null;
        }

        // Make empty buffers for the missing shards.
        for (int i = 0; i < TOTAL_SHARDS; i++) {
            if (!shardPresent[i]) {
                shards[i] = new byte[shardSize];
            }
        }

        // Use Reed-Solomon to fill in the missing shards
        ReedSolomon reedSolomon = ReedSolomon.create(DATA_SHARDS, PARITY_SHARDS);
        reedSolomon.decodeMissing(shards, shardPresent, 0, shardSize);

        // Combine the data shards into one buffer for convenience.
        // (This is not efficient, but it is convenient.)
        byte[] allBytes = new byte[shardSize * DATA_SHARDS];
        for (int i = 0; i < DATA_SHARDS; i++) {
            System.arraycopy(shards[i], 0, allBytes, shardSize * i, shardSize);
        }
        int fileSize = ByteBuffer.wrap(allBytes).getInt();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(allBytes, BYTES_IN_INT, fileSize);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}