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
 * A service registry. Implementations of this interface are thread safe.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 * @author <a href="mailto:frainone@redhat.com">Flavia Rainone</a>
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public interface ServiceRegistry {

    /**
     * Gets a service controller, throwing an exception if it is not found.
     *
     * @param serviceName the service name
     * @param <T> service controller value type
     * @return the service controller corresponding to {@code serviceName}
     * @throws ServiceNotFoundException if the service is not present in the registry
     */
    <T> ServiceController<T> getRequiredService(ServiceName serviceName) throws ServiceNotFoundException;

    /**
     * Gets a service controller, returning {@code null} if it is not found.
     *
     * @param serviceName the service name
     * @param <T> service controller value type
     * @return the service controller corresponding to {@code serviceName}, or {@code null} if it is not found
     */
    <T> ServiceController<T> getService(ServiceName serviceName);

    /**
     * Disables this registry and all its services, causing {@code UP} services to stop.
     *
     * @param transaction the transaction
     * @throws java.lang.IllegalArgumentException if <code>transaction</code> is null
     * or if transaction controller associated with <code>transaction</code>
     * is not the same as the one associated with this service registry.
     * @throws org.jboss.msc.txn.InvalidTransactionStateException if transaction is not active.
     * @throws IllegalArgumentException if transaction was created by different transaction controller than this registry
     */
    void disable(UpdateTransaction transaction) throws IllegalArgumentException, InvalidTransactionStateException;

    /**
     * Enables this registry. As a result, its services may start, depending on their
     * {@link org.jboss.msc.service.ServiceMode mode} rules.
     * <p> Registries are enabled by default.
     *
     * @param transaction the transaction
     * @throws java.lang.IllegalArgumentException if <code>transaction</code> is null
     * or if transaction controller associated with <code>transaction</code>
     * is not the same as the one associated with this service registry.
     * @throws org.jboss.msc.txn.InvalidTransactionStateException if transaction is not active.
     * @throws IllegalArgumentException if transaction was created by different transaction controller than this registry
     */
    void enable(UpdateTransaction transaction) throws IllegalArgumentException, InvalidTransactionStateException;

    /**
     * Removes this registry from the container.
     *
     * @param transaction the transaction
     * @throws java.lang.IllegalArgumentException if <code>transaction</code> is null
     * or if transaction controller associated with <code>transaction</code>
     * is not the same as the one associated with this service registry.
     * @throws org.jboss.msc.txn.InvalidTransactionStateException if transaction is not active.
     * @throws IllegalArgumentException if transaction was created by different transaction controller than this registry
     */
    void remove(UpdateTransaction transaction) throws IllegalArgumentException, InvalidTransactionStateException;

    /**
     * Removes this registry from the container.
     *
     * @param transaction the transaction
     * @param completionListener called when operation is finished
     * @throws java.lang.IllegalArgumentException if <code>transaction</code> is null
     * or if transaction controller associated with <code>transaction</code>
     * is not the same as the one associated with this service registry.
     * @throws org.jboss.msc.txn.InvalidTransactionStateException if transaction is not active.
     * @throws IllegalArgumentException if transaction was created by different transaction controller than this registry
     */
    void remove(UpdateTransaction transaction, Listener<ServiceRegistry> completionListener) throws IllegalArgumentException, InvalidTransactionStateException;

}
