/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

package org.jboss.msc.txn;

import org.jboss.msc.service.ServiceRegistry;

import java.security.AccessController;

import static org.jboss.msc._private.MSCLogger.TXN;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class Helper {

    private Helper() {}

    static void validateTransaction(final Transaction txn, final TransactionController txnController)
        throws IllegalArgumentException, InvalidTransactionStateException {
        if (txn == null) {
            throw TXN.methodParameterIsNull("txn");
        }
        final AbstractTransaction abstractTxn = getAbstractTransaction(txn);
        if (txnController != abstractTxn.txnController) {
            throw TXN.transactionControllerMismatch();
        }
        abstractTxn.ensureIsActive();
    }

    static BasicReadTransaction validateReadTransaction(final ReadTransaction readTxn, final TransactionController txnController) throws SecurityException {
        if (readTxn == null) {
            throw TXN.methodParameterIsNull("readTxn");
        }
        final boolean isReadTxn = readTxn instanceof BasicReadTransaction;
        final boolean isUpdateTxn = readTxn instanceof BasicUpdateTransaction;
        if (!isReadTxn && !isUpdateTxn) {
            throw new SecurityException("Transaction not created by this controller");
        }
        final BasicReadTransaction basicReadTxn = isUpdateTxn ? ((BasicUpdateTransaction)readTxn).getDelegate() : (BasicReadTransaction) readTxn;
        if (basicReadTxn.txnController != txnController) {
            throw new SecurityException("Transaction not created by this controller");
        }
        return basicReadTxn;
    }

    static BasicUpdateTransaction validateUpdateTransaction(final UpdateTransaction updateTxn, final TransactionController txnController) throws IllegalArgumentException, SecurityException {
        if (updateTxn == null) {
            throw TXN.methodParameterIsNull("updateTxn");
        }
        if (!(updateTxn instanceof BasicUpdateTransaction)) {
            throw new SecurityException("Transaction not created by this controller");
        }
        final BasicUpdateTransaction basicUpdateTxn = (BasicUpdateTransaction)updateTxn;
        if (basicUpdateTxn.getController() != txnController) {
            throw new SecurityException("Transaction not created by this controller");
        }
        return basicUpdateTxn;
    }

    static AbstractTransaction getAbstractTransaction(final Transaction transaction) throws IllegalArgumentException {
        if (transaction instanceof BasicUpdateTransaction) return ((BasicUpdateTransaction)transaction).getDelegate();
        if (transaction instanceof BasicReadTransaction) return (BasicReadTransaction)transaction;
        throw TXN.illegalTransaction();
    }

    static void validateRegistry(final ServiceRegistry registry) {
        if (registry == null) {
            throw TXN.methodParameterIsNull("registry");
        }
        if (!(registry instanceof ServiceRegistryImpl)) {
            throw TXN.methodParameterIsInvalid("registry");
        }
    }

    static void setModified(final UpdateTransaction transaction) {
        ((BasicUpdateTransaction)transaction).setModified();
    }

    static ClassLoader setTCCL(final ClassLoader newTCCL) {
        final SecurityManager sm = System.getSecurityManager();
        final SetTCCLAction setTCCLAction = new SetTCCLAction(newTCCL);
        if (sm != null) {
            return AccessController.doPrivileged(setTCCLAction);
        } else {
            return setTCCLAction.run();
        }
    }

}
