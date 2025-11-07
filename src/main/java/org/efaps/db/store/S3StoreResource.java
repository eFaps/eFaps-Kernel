/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.efaps.db.store;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3StoreResource
    extends AbstractStoreResource
{

    private static final Logger LOG = LoggerFactory.getLogger(S3StoreResource.class);

    private static Map<Long, S3Client> S3CLIENTS = new HashMap<>();

    private S3Client getS3Client()
    {
        if (!S3CLIENTS.containsKey(getStore().getId())) {
            LOG.info("Initializing S3 StoreResource for with: {} - {}", getStore().getName(), getStore().getId());
            final var region = getProperties().get("S3region");
            final var endpoint = getProperties().get("S3endpoint");
            final var accessKey = getProperties().get("S3accessKey");
            final var secretKey = getProperties().get("S3secretKey");
            final var bucketName = getProperties().get("S3bucketName");

            LOG.info("  region: {}, endpoint: {}, bucketName: {}, accessKey: {}", region, endpoint, bucketName,
                            accessKey);

            final var client = S3Client.builder()
                            .region(Region.of(region))
                            .endpointOverride(URI.create(endpoint))
                            .credentialsProvider(StaticCredentialsProvider.create(
                                            AwsBasicCredentials.create(accessKey, secretKey)))
                            .build();
            S3CLIENTS.put(getStore().getId(), client);
        }
        return S3CLIENTS.get(getStore().getId());
    }

    private String getBucketName()
    {

        return getProperties().get("S3bucketName");
    }

    @Override
    public InputStream read()
        throws EFapsException
    {
        final var objectRequest = GetObjectRequest
                        .builder()
                        .key(getInstance().getOid())
                        .bucket(getBucketName())
                        .build();
        final var responseInputStream = getS3Client().getObject(objectRequest);
        final var response = responseInputStream.response();

        LOG.info("Downloaded {}, response: {}", getInstance().getOid(), response);
        return responseInputStream;
    }

    @Override
    public long write(final InputStream in,
                      final long size,
                      final String fileName)
        throws EFapsException
    {
        final var objectRequest = PutObjectRequest.builder()
                        .bucket(getBucketName())
                        .contentLength(size)
                        .key(getInstance().getOid())
                        .metadata(Map.of("fileName", fileName))
                        .build();

        final var body = RequestBody.fromInputStream(in, size);
        final var response = getS3Client().putObject(objectRequest, body);
        LOG.info("Uploaded {}, response: {}", getInstance().getOid(), response);
        setFileInfo(fileName, size);
        return size;
    }

    @Override
    public boolean exists()
        throws EFapsException
    {
        var ret = super.exists();
        if (super.exists()) {
            ret = false;
            final var headObjectRequest = HeadObjectRequest.builder()
                            .bucket(getBucketName())
                            .key(getInstance().getOid())
                            .build();
            try {
                final var response = getS3Client().headObject(headObjectRequest);
                LOG.info("Checked for {}, response: {}", getInstance().getOid(), response);
                ret = true;
            } catch (final NoSuchKeyException e) {
                LOG.debug("Catched", e);
            }
        }
        return ret;
    }

    @Override
    public void delete()
        throws EFapsException
    {
        // no deletion is done
    }

    @Override
    public void commit(Xid arg0,
                       boolean arg1)
        throws XAException
    {
    }

    @Override
    public void forget(Xid arg0)
        throws XAException
    {
    }

    @Override
    public int getTransactionTimeout()
        throws XAException
    {
        return 0;
    }

    @Override
    public int prepare(Xid arg0)
        throws XAException
    {
        return 0;
    }

    @Override
    public Xid[] recover(int arg0)
        throws XAException
    {
        return null;
    }

    @Override
    public void rollback(Xid arg0)
        throws XAException
    {
    }

    @Override
    public boolean setTransactionTimeout(int arg0)
        throws XAException
    {
        return false;
    }

    @Override
    protected int add2Select(SQLSelect _select)
    {
        return 1;
    }
}
