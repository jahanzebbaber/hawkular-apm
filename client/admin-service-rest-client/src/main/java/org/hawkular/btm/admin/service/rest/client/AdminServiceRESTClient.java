/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
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
package org.hawkular.btm.admin.service.rest.client;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import org.hawkular.btm.api.logging.Logger;
import org.hawkular.btm.api.logging.Logger.Level;
import org.hawkular.btm.api.model.admin.CollectorConfiguration;
import org.hawkular.btm.api.services.AdminService;
import org.hawkular.btm.api.services.ConfigurationLoader;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class provides the REST client implementation for the Admin Service
 * API.
 *
 * @author gbrown
 */
public class AdminServiceRESTClient implements AdminService {

    private static final Logger log = Logger.getLogger(AdminServiceRESTClient.class.getName());

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String HAWKULAR_PERSONA = "Hawkular-Persona";

    private String username = System.getProperty("hawkular-btm.username");
    private String password = System.getProperty("hawkular-btm.password");

    private String authorization = null;

    private String baseUrl;

    {
        baseUrl = System.getProperty("hawkular-btm.base-uri");

        if (baseUrl != null && baseUrl.length() > 0 && baseUrl.charAt(baseUrl.length() - 1) != '/') {
            baseUrl = baseUrl + '/';
        }
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;

        // Clear any previously computed authorization string
        this.authorization = null;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;

        // Clear any previously computed authorization string
        this.authorization = null;
    }

    /**
     * @return the baseUrl
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * @param baseUrl the baseUrl to set
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /* (non-Javadoc)
     * @see org.hawkular.btm.api.services.AdminService#getConfiguration(java.lang.String,
     *                                  java.lang.String, java.lang.String)
     */
    @Override
    public CollectorConfiguration getConfiguration(String tenantId, String host, String server) {
        if (log.isLoggable(Level.FINEST)) {
            log.finest("Get business transaction: tenantId=[" + tenantId + "] host=[" + host
                                        + "] server=[" + server + "]");
        }

        // Check if BTM configuration provided locally
        if (System.getProperty(ConfigurationLoader.HAWKULAR_BTM_CONFIG) != null) {
            return ConfigurationLoader.getConfiguration();
        }

        StringBuilder builder = new StringBuilder().append(baseUrl).append("admin/config");

        if (host != null) {
            builder.append("?host=");
            builder.append(host);
        }

        if (server != null) {
            if (host == null) {
                builder.append('?');
            } else {
                builder.append('&');
            }
            builder.append("server=");
            builder.append(server);
        }

        try {
            URL url = new URL(builder.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            connection.setRequestProperty("Content-Type",
                    "application/json");

            addHeaders(connection, tenantId);

            java.io.InputStream is = connection.getInputStream();

            StringBuilder resp=new StringBuilder();
            byte[] b=new byte[10000];

            while (true) {
                int len=is.read(b);

                if (len == -1) {
                    break;
                }

                resp.append(new String(b, 0, len));
            }

            is.close();

            if (connection.getResponseCode() == 200) {
                if (log.isLoggable(Level.FINEST)) {
                    log.finest("Returned json=[" + resp.toString() + "]");
                }
                try {
                    return mapper.readValue(resp.toString(), CollectorConfiguration.class);
                } catch (Throwable t) {
                    log.log(Level.SEVERE, "Failed to deserialize", t);
                }
            } else {
                if (log.isLoggable(Level.FINEST)) {
                    log.finest("Failed to get collector configuration: status=[" + connection.getResponseCode() + "]:"
                            + connection.getResponseMessage());
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to send 'get' collector configuration request", e);
        }

        return null;
    }

    /**
     * Add the header values to the supplied connection.
     *
     * @param connection The connection
     * @param tenantId The optional tenant id
     */
    protected void addHeaders(HttpURLConnection connection, String tenantId) {
        if (tenantId != null) {
            connection.setRequestProperty(HAWKULAR_PERSONA, tenantId);
        }

        if (authorization == null && username != null) {
            String authString = username + ":" + password;
            String encoded = Base64.getEncoder().encodeToString(authString.getBytes());

            authorization = "Basic " + encoded;
        }

        if (authorization != null) {
            connection.setRequestProperty("Authorization", authorization);
        }
    }
}