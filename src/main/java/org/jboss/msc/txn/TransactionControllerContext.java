/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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
package org.jboss.msc.txn;

import static org.jboss.msc._private.MSCLogger.TXN;

import org.jboss.msc.service.ServiceRegistry;

/**
 * A context scoped by a {@link TransactionController}. This context can be used with multiple transactions,
 * as long as those transactions belong to the same {@code TransactionController} that provided this context. 
 * 
 * @author <a href="mailto:frainone@redhat.com">Flavia Rainone</a>
 *
 */
abstract class TransactionControllerContext {

    protected final TransactionController transactionController;

    public TransactionControllerContext(TransactionController transactionController) {
        this.transactionController = transactionController;
    }

    /**
     * Validates {@code transaction} is a {@code TransactionImpl} created by the same transaction
     * controller that is associated with this context.
     * <p>
     * This method must be invoked by all methods in the subclass that use a transaction to control one or more 
     * tasks.
     * 
     * @param transaction the transaction to be validated
     * @throws IllegalArgumentException if {@code transaction} does not belong to the same transaction controller
     *                                  that created this context
     */
    void validateTransaction(Transaction transaction) {
        if (transaction == null) {
            throw TXN.methodParameterIsNull("transaction");
        }
        //if (!(transaction instanceof BasicTransaction) && !(transaction instanceof XATransaction)) {
        //    throw TXN.methodParameterIsInvalid("transaction");
        //} TODO should we test this?
        if (!transactionController.owns(transaction)) {
            // cannot be used by this context
            throw new IllegalArgumentException("Transaction does not belong to this context (transaction was created by a different transaction controller)");
        }
    }

    void validateRegistry(ServiceRegistry registry) {
        if (registry == null) {
            throw TXN.methodParameterIsNull("registry");
        }
        if (!(registry instanceof ServiceRegistryImpl)) {
            throw TXN.methodParameterIsInvalid("registry");
        }
    }
}