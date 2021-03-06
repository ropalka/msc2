/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
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

package org.jboss.msc.service;

import org.jboss.msc.txn.InvalidTransactionStateException;
import org.jboss.msc.txn.UpdateTransaction;
import org.jboss.msc.util.Listener;

/**
 * A service container. Implementations of this interface are thread safe.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 * @author <a href="mailto:frainone@redhat.com">Flavia Rainone</a>
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public interface ServiceContainer {

    /**
     * Creates new registry associated with this container.
     *
     * @param transaction the transaction
     * @return container registry
     * @throws IllegalStateException if container have been shutdown
     */
    ServiceRegistry newRegistry(UpdateTransaction transaction);

    /**
     * Shuts down the container, removing all registries and their services.
     *
     * @param transaction the transaction
     * @throws java.lang.IllegalArgumentException if <code>transaction</code> is null
     * or if transaction controller associated with <code>transaction</code>
     * is not the same as the one associated with this service container.
     * @throws org.jboss.msc.txn.InvalidTransactionStateException if transaction is not active.
     * @throws IllegalArgumentException if transaction was created by different transaction controller than this container
     */
    void shutdown(UpdateTransaction transaction) throws IllegalArgumentException, InvalidTransactionStateException;

    /**
     * Shuts down the container, removing all registries and their services.
     *
     * @param transaction the transaction
     * @param completionListener called when operation is finished
     * @throws java.lang.IllegalArgumentException if <code>transaction</code> is null
     * or if transaction controller associated with <code>transaction</code>
     * is not the same as the one associated with this service container.
     * @throws org.jboss.msc.txn.InvalidTransactionStateException if transaction is not active.
     * @throws IllegalArgumentException if transaction was created by different transaction controller than this container
     */
    void shutdown(UpdateTransaction transaction, Listener<ServiceContainer> completionListener) throws IllegalArgumentException, InvalidTransactionStateException;

}
