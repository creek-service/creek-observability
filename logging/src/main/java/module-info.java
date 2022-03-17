module creek.observability.logging {
    requires creek.base.type;
    requires org.slf4j;

    exports org.creek.api.observability.logging.structured;
    exports org.creek.internal.observability.logging.structured to
            creek.observability.logging.fixtures;
}
