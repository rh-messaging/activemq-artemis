/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.artemis.tests.integration.rest.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class RestAMQConnection implements Closeable {
   private HttpClient httpClient = new DefaultHttpClient();

   private String targetUri;
   private List<RestMessageContext> contexts = new ArrayList<>();

   public RestAMQConnection(String targetUri) {
      this.targetUri = targetUri;
   }

   public QueueRestMessageContext createQueueContext(String queue) throws Exception {
      QueueRestMessageContext ctx = new QueueRestMessageContext(this, queue);
      contexts.add(ctx);
      return ctx;
   }

   public TopicRestMessageContext createTopicContext(String topic) throws Exception {
      TopicRestMessageContext ctx = new TopicRestMessageContext(this, topic, false);
      contexts.add(ctx);
      return ctx;
   }

   @Override
   public void close() throws IOException {
      for (RestMessageContext ctx : contexts) {
         ctx.close();
      }
   }

   private String getFullLink(String link) {
      if (link.startsWith("http:")) {
         return link;
      }
      return targetUri + link;
   }

   public HttpGet prepareGet(String destLink) throws IOException {
      String fullLink = getFullLink(destLink);
      return new HttpGet(fullLink);
   }

   public HttpResponse request(HttpGet get) throws IOException {
      HttpResponse resp = httpClient.execute(get);
      return resp;
   }

   public HttpResponse request(String destLink) throws IOException {
      String fullLink = getFullLink(destLink);
      HttpGet request = new HttpGet(fullLink);
      HttpResponse resp = httpClient.execute(request);
      return resp;
   }

   public HttpPost preparePost(String uri) throws IOException {
      String fullLink = getFullLink(uri);
      return new HttpPost(fullLink);
   }

   public HttpResponse post(HttpPost post, String contentType, String body) throws IOException {
      StringEntity entity = new StringEntity(body,
              ContentType.create(contentType));
      post.setEntity(entity);
      HttpResponse resp = httpClient.execute(post);
      return resp;
   }

   public HttpResponse post(HttpPost post) throws IOException {
      HttpResponse resp = httpClient.execute(post);
      return resp;
   }

   public HttpResponse post(String uri, String contentType, String body) throws IOException {
      String fullLink = getFullLink(uri);
      HttpPost post = new HttpPost(fullLink);
      StringEntity entity = new StringEntity(body,
              ContentType.create(contentType));
      post.setEntity(entity);
      HttpResponse resp = httpClient.execute(post);
      return resp;
   }

   public HttpResponse post(String uri) throws IOException {
      String fullLink = getFullLink(uri);
      HttpPost post = new HttpPost(fullLink);
      HttpResponse resp = httpClient.execute(post);
      return resp;
   }

   public void delete(String uri) throws IOException {
      String consumerUri = getFullLink(uri);
      HttpDelete delete = new HttpDelete(consumerUri);
      HttpResponse resp = httpClient.execute(delete);
   }

   public String getTargetUri() {
      return this.targetUri;
   }

}
