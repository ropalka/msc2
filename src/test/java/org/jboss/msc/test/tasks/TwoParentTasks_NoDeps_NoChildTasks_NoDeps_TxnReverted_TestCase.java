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

package org.jboss.msc.test.tasks;

import static org.junit.Assert.assertNotNull;

import java.util.concurrent.CountDownLatch;

import org.jboss.msc.test.utils.AbstractTransactionTest;
import org.jboss.msc.test.utils.TestExecutable;
import org.jboss.msc.test.utils.TestRevertible;
import org.jboss.msc.txn.BasicTransaction;
import org.jboss.msc.txn.CompletionListener;
import org.jboss.msc.txn.RollbackResult;
import org.jboss.msc.txn.TaskController;
import org.junit.Test;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class TwoParentTasks_NoDeps_NoChildTasks_NoDeps_TxnReverted_TestCase extends AbstractTransactionTest {

    /**
     * Scenario:
     * <UL>
     * <LI>task0 completes at EXECUTE</LI>
     * <LI>task1 completes at EXECUTE</LI>
     * <LI>no dependencies</LI>
     * <LI>no children</LI>
     * <LI>transaction rolled back</LI>
     * </UL>
     */
    @Test
    public void usecase1() {
        final BasicTransaction transaction = newTransaction();
        final CountDownLatch signal = new CountDownLatch(1);
        // installing task0
        final TestExecutable<Void> e0 = new TestExecutable<Void>(signal);
        final TestRevertible r0 = new TestRevertible();
        final TaskController<Void> task0Controller = newTask(transaction, e0, r0);
        assertNotNull(task0Controller);
        // installing task1
        final TestExecutable<Void> e1 = new TestExecutable<Void>(signal);
        final TestRevertible r1 = new TestRevertible();
        final TaskController<Void> task1Controller = newTask(transaction, e1, r1);
        assertNotNull(task1Controller);
        // reverting transaction
        final CompletionListener<RollbackResult<BasicTransaction>> rollbackListener = new CompletionListener<>();
        rollback(transaction, rollbackListener);
        signal.countDown();
        rollbackListener.awaitCompletionUninterruptibly();
        // e0.wasCalled() can return either true or false, depends on threads scheduling
        if (e0.wasCalled()) {
            assertCalled(r0);
            assertCallOrder(e0, r0);
        }
        // e1.wasCalled() can return either true or false, depends on threads scheduling
        if (e1.wasCalled()) {
            assertCalled(r1);
            assertCallOrder(e1, r1);
        }
    }

    /**
     * Scenario:
     * <UL>
     * <LI>task0 cancels at EXECUTE</LI>
     * <LI>task1 cancels at EXECUTE</LI>
     * <LI>no dependencies</LI>
     * <LI>no children</LI>
     * <LI>transaction rolled back</LI>
     * </UL>
     */
    @Test
    public void usecase2() {
        final BasicTransaction transaction = newTransaction();
        final CountDownLatch signal = new CountDownLatch(1);
        // installing task0
        final TestExecutable<Void> e0 = new TestExecutable<Void>(true, signal);
        final TestRevertible r0 = new TestRevertible();
        final TaskController<Void> task0Controller = newTask(transaction, e0, r0);
        assertNotNull(task0Controller);
        // installing task1
        final TestExecutable<Void> e1 = new TestExecutable<Void>(true, signal);
        final TestRevertible r1 = new TestRevertible();
        final TaskController<Void> task1Controller = newTask(transaction, e1, r1);
        assertNotNull(task1Controller);
        // reverting transaction
        final CompletionListener<RollbackResult<BasicTransaction>> rollbackListener = new CompletionListener<>();
        rollback(transaction, rollbackListener);
        signal.countDown();
        rollbackListener.awaitCompletionUninterruptibly();
        // e0.wasCalled() can return either true or false, depends on threads scheduling
        assertNotCalled(r0);
        // e1.wasCalled() can return either true or false, depends on threads scheduling
        assertNotCalled(r1);
    }

    /**
     * Scenario:
     * <UL>
     * <LI>task0 completes at EXECUTE</LI>
     * <LI>task1 cancels at EXECUTE</LI>
     * <LI>no dependencies</LI>
     * <LI>no children</LI>
     * <LI>transaction rolled back</LI>
     * </UL>
     */
    @Test
    public void usecase3() {
        final BasicTransaction transaction = newTransaction();
        final CountDownLatch signal = new CountDownLatch(1);
        // installing task0
        final TestExecutable<Void> e0 = new TestExecutable<Void>(signal);
        final TestRevertible r0 = new TestRevertible();
        final TaskController<Void> task0Controller = newTask(transaction, e0, r0);
        assertNotNull(task0Controller);
        // installing task1
        final TestExecutable<Void> e1 = new TestExecutable<Void>(true, signal);
        final TestRevertible r1 = new TestRevertible();
        final TaskController<Void> task1Controller = newTask(transaction, e1, r1);
        assertNotNull(task1Controller);
        // reverting transaction
        final CompletionListener<RollbackResult<BasicTransaction>> rollbackListener = new CompletionListener<>();
        rollback(transaction, rollbackListener);
        signal.countDown();
        rollbackListener.awaitCompletionUninterruptibly();
        // e0.wasCalled() can return either true or false, depends on threads scheduling
        if (e0.wasCalled()) {
            assertCalled(r0);
            assertCallOrder(e0, r0);
        }
        // e1.wasCalled() can return either true or false, depends on threads scheduling
        assertNotCalled(r1);
    }

}
