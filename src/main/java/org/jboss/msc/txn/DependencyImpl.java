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

package org.jboss.msc.txn;

import org.jboss.msc._private.MSCLogger;
import org.jboss.msc.problem.Problem;
import org.jboss.msc.problem.ProblemReport;
import org.jboss.msc.service.Dependency;
import org.jboss.msc.service.DependencyFlag;
import org.jboss.msc.problem.Problem.Severity;

import static org.jboss.msc._private.MSCLogger.SERVICE;

/**
 * Dependency implementation.
 * 
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 * @author <a href="mailto:frainone@redhat.com">Flavia Rainone</a>
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 *
 * @param <T>
 */
class DependencyImpl<T> implements Dependency<T> {

    private static final byte REQUIRED_FLAG   = (byte)(1 << DependencyFlag.REQUIRED.ordinal());
    private static final byte UNREQUIRED_FLAG = (byte)(1 << DependencyFlag.UNREQUIRED.ordinal());
    private static final byte DEMANDED_FLAG   = (byte)(1 << DependencyFlag.DEMANDED.ordinal());
    private static final byte UNDEMANDED_FLAG = (byte)(1 << DependencyFlag.UNDEMANDED.ordinal());

    /**
     * Dependency flags.
     */
    private final byte flags;
    /**
     * The dependency registration.
     */
    private volatile Registration dependencyRegistration;
    /**
     * The incoming dependency.
     */
    protected volatile ServiceControllerImpl<?> dependent;

    /**
     * Creates a simple dependency to {@code dependencyRegistration}.
     * 
     * @param flags dependency flags
     */
    protected DependencyImpl(final DependencyFlag... flags) {
        byte translatedFlags = 0;
        for (final DependencyFlag flag : flags) {
            if (flag != null) {
                translatedFlags |= (1 << flag.ordinal());
            }
        }
        if (Bits.allAreSet(translatedFlags, UNDEMANDED_FLAG | DEMANDED_FLAG)) {
            throw SERVICE.mutuallyExclusiveFlags(DependencyFlag.DEMANDED.toString(), DependencyFlag.UNDEMANDED.toString());
        }
        if (Bits.allAreSet(translatedFlags, REQUIRED_FLAG | UNREQUIRED_FLAG)) {
            throw SERVICE.mutuallyExclusiveFlags(DependencyFlag.REQUIRED.toString(), DependencyFlag.UNREQUIRED.toString());
        }
        this.flags = translatedFlags;
    }

    final void setDependencyRegistration(final Registration dependencyRegistration) {
        this.dependencyRegistration = dependencyRegistration;
    }

    public T get() {
        if (dependencyRegistration == null) return null;
        @SuppressWarnings("unchecked")
        ServiceControllerImpl<T> dependencyController = (ServiceControllerImpl<T>) dependencyRegistration.getController();
        return dependencyController == null ? null : dependencyController.getValue();
    }

    /**
     * Sets the dependency dependent, invoked during {@code dependentController} installation or {@link ParentDependency}
     * activation (when parent dependency is satisfied and installed).
     * 
     * @param dependent    dependent associated with this dependency
     * @param transaction  the active transaction
     */
    void setDependent(final ServiceControllerImpl<?> dependent, final Transaction transaction) {
        this.dependent = dependent;
        dependencyRegistration.addIncomingDependency(transaction, this);
        if (Bits.allAreSet(flags, DEMANDED_FLAG)) {
            dependencyRegistration.addDemand(transaction);
        }
    }

    /**
     * Clears the dependency dependent, invoked during {@code dependentController} removal.
     * 
     * @param transaction   the active transaction
     */
    void clearDependent(final Transaction transaction) {
        dependencyRegistration.removeIncomingDependency(this);
        if (Bits.allAreSet(flags, DEMANDED_FLAG)) {
            dependencyRegistration.removeDemand(transaction);
        }
        this.dependent = null;
    }

    /**
     * Returns the dependency registration.
     * 
     * @return the dependency registration
     */
    Registration getDependencyRegistration() {
        return dependencyRegistration;
    }

    /**
     * Demands this dependency to be satisfied.
     * 
     * @param transaction the active transaction
     */
    void demand(Transaction transaction) {
        if (Bits.allAreClear(flags, DEMANDED_FLAG | UNDEMANDED_FLAG)) {
            dependencyRegistration.addDemand(transaction);
        }
    }

    /**
     * Removes demand for this dependency to be satisfied.
     * 
     * @param transaction the active transaction
     */
    void undemand(Transaction transaction) {
        if (Bits.allAreClear(flags, DEMANDED_FLAG | UNDEMANDED_FLAG)) {
            dependencyRegistration.removeDemand(transaction);
        }
    }

    /**
     * Notifies that dependency is now {@code UP} or is scheduled to start.
     * 
     * @param transaction   the active transaction
     */
    void dependencyUp(final Transaction transaction) {
        dependent.dependencySatisfied(transaction);
    }

    /**
     * Notifies that dependency is now stopping.
     *  
     * @param transaction    the active transaction
     */
    void dependencyDown(final Transaction transaction) {
        dependent.dependencyUnsatisfied(transaction);
    }

    /**
     * Validates dependency state before active transaction commits.
     * 
     * @param report report where all validation problems found will be added
     */
    void validate(final ProblemReport report) {
        final ServiceControllerImpl<?> controller = dependencyRegistration.holderRef.get();
        if (controller == null && Bits.allAreClear(flags, UNREQUIRED_FLAG)) {
            report.addProblem(new Problem(Severity.ERROR, MSCLogger.SERVICE.requiredDependency(dependent.getServiceName(), dependencyRegistration.getServiceName())));
        }
    }

}