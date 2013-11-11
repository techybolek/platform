This sample is related to ws-event event adaptor

This sample uses
InputEventAdaptor:      ws-event
EventBuilder:           xml
EventFormatter:         xml
OutputEventAdaptor:     ws-event

Producers:  atm-transaction-stats

Consumers: log-service (axis2 service receiver)

Note : - It is necessary to run WSO2 MB in offset 1 (Need to create subscription for topic "foo/bar" to log service.)
