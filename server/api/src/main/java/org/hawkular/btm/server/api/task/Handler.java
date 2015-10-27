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
package org.hawkular.btm.server.api.task;

import java.util.List;

/**
 * This interface represents a generic handler of a list of items.
 *
 * @author gbrown
 */
public interface Handler<T> {

    /**
     * This method handles the list of items.
     *
     * @param items The list of items
     * @throws Exception Failed to handle the list of items
     */
    void handle(List<T> items) throws Exception;

}