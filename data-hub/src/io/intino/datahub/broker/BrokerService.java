package io.intino.datahub.broker;

public interface BrokerService {

	void start() throws Exception;

	void stop() throws Exception;

	BrokerManager manager();
}