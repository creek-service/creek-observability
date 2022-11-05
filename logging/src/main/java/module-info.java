/** Module containing machine friendly logging */
module creek.observability.logging {
    requires creek.base.type;
    requires org.slf4j;

    exports org.creekservice.api.observability.logging.structured;
    exports org.creekservice.internal.observability.logging.structured to
            creek.observability.logging.fixtures;
}
