package org.jboss.arquillian.warp.client.execution;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.client.filter.RequestFilter;
import org.jboss.arquillian.warp.client.result.WarpResult;

/**
 * Takes client action and specifies how to verify its execution.
 */
public interface ClientActionExecutor {

    /**
     * Asserts given server state
     * 
     * @param assertion the object containing assertions which should be verified on the server
     * @return the verified server state returned from the server
     */
    <T extends ServerAssertion> T verify(T assertion);

    /**
     * Asserts given server state
     * 
     * @param assertions the objects containing assertions which should be verified on the server in the given order of
     *        execution
     * @return the result of server state verification
     */
    WarpResult verifyAll(ServerAssertion... assertions);

    /**
     * Specifies filter which will be used to select which requests will be enriched and verified
     * 
     * @param filter the filter which specifies which requests will be enriched and verified
     * @return the interface for executing single server verification
     */
    SingleRequestExecutor filter(RequestFilter<?> filter);

    /**
     * Specifies class of a filter which will be used to select which requests will be enriched and verified
     * 
     * @param filterClass the class of the filter which specifies which requests will be enriched and verified
     * @return the interface for executing single server verification
     */
    SingleRequestExecutor filter(Class<? extends RequestFilter<?>> filterClass);

    /**
     * Specifies anonymous group of execution - each specified group will be independently filtered and executed, providing
     * interface for verifying different assertions for several requests caused by single client action.
     * 
     * After execution, the details of execution can be retrieved for each group independently by the sequence number given by
     * the order of definition (starting with 0). For result retrival by names, see {@link #group(Object)}.
     * 
     * @return the group executor which specifies what assertions to verify on the server
     */
    ExecutionGroup group();

    /**
     * Specifies named group of execution - each specified group will be independently filtered and executed, providing
     * interface for verifying different assertions for several requests caused by single client action.
     * 
     * After execution, the details of execution can be retrieved for each group independently by the provided identified.
     * 
     * @return the group executor which specifies what assertions to verify on the server
     */
    ExecutionGroup group(Object identifier);
}