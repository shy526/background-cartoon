package com.github.shy526.http;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * http统一返回结果集
 *
 * @author shy526
 */
@Slf4j
public class HttpResult implements Closeable {
    @Getter
    private Integer httpStatus = 0;
    @Getter
    private CloseableHttpResponse response;
    private String entityStr;
    @Getter
    private Exception error;
    @Getter
    private HttpRequestBase request;

    public HttpResult() {
    }

    public HttpResult(Exception error, HttpRequestBase request) {
        this.error = error;
        this.request = request;
    }

    public HttpResult(CloseableHttpResponse response, HttpRequestBase request) {
        this.response = response;
        this.request = request;
        if (this.response != null) {
            this.httpStatus = response.getStatusLine().getStatusCode();
        }
    }

    /**
     * 输出字符串 形式
     *
     * @param encode 字符编码
     * @return String
     */
    public String getEntityStr(String encode) {
        try {
            if (httpStatus.equals(HttpStatus.SC_OK) && StringUtils.isEmpty(this.entityStr)) {
                HttpEntity entity = this.response.getEntity();
                this.entityStr = entity == null ? null : EntityUtils.toString(entity, encode);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        consume();
        return this.entityStr;
    }

    public String getEntityStr() {
        return this.getEntityStr(CharEncoding.UTF_8);
    }

    private void consumeHttpEntity(HttpEntity httpEntity) {
        if (httpEntity != null) {
            try {
                EntityUtils.consume(httpEntity);
            } catch (IOException e) {
            } finally {
                httpEntity = null;
            }
        }
    }

    public void consume() {
        if (response != null) {
            consumeHttpEntity(response.getEntity());
            try {
                response.close();
            } catch (IOException e) {
            } finally {
                response = null;
            }
        }
    }

    public InputStream getInputStream() throws IOException {
        return new BufferedInputStream(this.getResponse().getEntity().getContent());
    }

    @Override
    public void close() throws IOException {
        this.consume();
    }
}
