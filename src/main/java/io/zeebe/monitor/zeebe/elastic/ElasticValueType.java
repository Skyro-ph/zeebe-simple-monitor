package io.zeebe.monitor.zeebe.elastic;

public enum ElasticValueType {
    DEPLOYMENT("_deployment"),
    DEPLOYMENT_DISTRIBUTION("_deployment-distribution"),
    PROCESS("_process"),
    PROCESS_INSTANCE("_process-instance"),
    PROCESS_INSTANCE_CREATION("_process-instance-creation"),
    PROCESS_INSTANCE_MODIFICATION("_process-instance-modification"),
    PROCESS_MESSAGE_SUBSCRIPTION("_process-message-subscription"),
    JOB("_job"),
    INCIDENT("_incident"),
    MESSAGE("_message"),
    MESSAGE_SUBSCRIPTION("_message-subscription"),
    MESSAGE_START_EVENT_SUBSCRIPTION("_message-start-event-subscription"),
    ERROR("_error"),
    TIMER("_timer"),
    VARIABLE("_variable"),
    VARIABLE_DOCUMENT("_variable-document")
    ;

    private final String indexName;

    ElasticValueType(String indexName) {
        this.indexName = indexName;
    }

    public String getIndexName() {
        return indexName;
    }
}
