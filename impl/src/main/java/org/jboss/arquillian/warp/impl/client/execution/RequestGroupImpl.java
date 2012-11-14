package org.jboss.arquillian.warp.impl.client.execution;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.client.execution.ExecutionGroup;
import org.jboss.arquillian.warp.client.execution.GroupAssertionSpecifier;
import org.jboss.arquillian.warp.client.execution.GroupsExecutor;
import org.jboss.arquillian.warp.client.filter.RequestFilter;
import org.jboss.arquillian.warp.client.result.ResponseGroup;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;

public class RequestGroupImpl implements ExecutionGroup, GroupAssertionSpecifier, ResponseGroup {

    private Object id;
    private RequestFilter<?> filter;
    private GroupsExecutor groupsExecutor;
    private int expectCount = 1;

    private ServerAssertion[] assertions;

    private LinkedHashMap<RequestPayload, ResponsePayload> payloads = new LinkedHashMap<RequestPayload, ResponsePayload>();

    public RequestGroupImpl(GroupsExecutor groupsExecutor, Object identifier) {
        this.groupsExecutor = groupsExecutor;
        this.id = identifier;
    }

    @Override
    public GroupAssertionSpecifier filter(RequestFilter<?> filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public GroupAssertionSpecifier filter(Class<RequestFilter<?>> filterClass) {
        this.filter = SecurityActions.newInstance(filterClass.getName(), new Class<?>[] {}, new Object[] {},
                RequestFilter.class);
        return this;
    }

    @Override
    public GroupAssertionSpecifier expectCount(int numberOfRequests) {
        this.expectCount = numberOfRequests;
        return this;
    }

    @Override
    public GroupsExecutor verify(ServerAssertion... assertions) {
        addAssertions(assertions);
        return groupsExecutor;
    }

    void addAssertions(ServerAssertion... assertions) {
        this.assertions = assertions;
    }

    @Override
    public RequestFilter<?> getFilter() {
        return filter;
    }

    @Override
    public <T extends ServerAssertion> T getAssertion() {
        return (T) payloads.values().iterator().next().getAssertions().get(0); 
    }
 
    @Override
    public <T extends ServerAssertion> T getAssertionForHitNumber(int hitNumber) {
        return (T) getAssertionsForHitNumber(hitNumber).get(0);
    }

    @Override
    public List<ServerAssertion> getAssertions() {
        return payloads.values().iterator().next().getAssertions();
    }
    
    @Override
    public List<ServerAssertion> getAssertionsForHitNumber(int hitNumber) {
        ResponsePayload payload = (ResponsePayload) payloads.values().toArray()[hitNumber];
        return payload.getAssertions();
    }

    @Override
    public int getHitCount() {
        return payloads.size();
    }

    public Object getId() {
        return id;
    }

    RequestPayload generateRequestPayload() {
        if (payloads.size() + 1 > expectCount) {
            throw new IllegalStateException(String.format("There were more requests executed (%s) then expected (%s)", payloads.size() + 1, expectCount));
        }
        RequestPayload requestPayload = new RequestPayload(assertions);
        payloads.put(requestPayload, null);
        return requestPayload;
    }

    boolean pushResponsePayload(ResponsePayload payload) {
        for (Entry<RequestPayload, ResponsePayload> entry : payloads.entrySet()) {
            if (payload.getSerialId() == entry.getKey().getSerialId()) {
                if (entry.getValue() != null) {
                    throw new IllegalStateException("");
                }
            }
            entry.setValue(payload);
            return true;
        }
        return false;
    }
    
    boolean allRequestsPaired() {
        // TODO throw an exception when payloads count is bigger then expected
        if (payloads.size() < expectCount) {
            return false;
        }
        for (ResponsePayload responsePayload : payloads.values()) {
            if (responsePayload == null) {
                return false;
            }
        }
        return true;
    }
    
    TestResult getFirstNonSuccessfulResult() {
        for (ResponsePayload payload : payloads.values()) {
            TestResult testResult = payload.getTestResult();

            if (testResult != null) {
                switch (testResult.getStatus()) {
                    case FAILED:
                        return testResult;
                    case SKIPPED:
                        return testResult;
                    case PASSED:
                }
            }
        }

        return null;
    }
}