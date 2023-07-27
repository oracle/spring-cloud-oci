/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.logging;

import com.oracle.bmc.loggingingestion.model.LogEntry;
import com.oracle.bmc.loggingingestion.model.LogEntryBatch;
import com.oracle.bmc.loggingingestion.model.PutLogsDetails;
import com.oracle.bmc.loggingingestion.requests.PutLogsRequest;
import com.oracle.bmc.loggingingestion.responses.PutLogsResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

/**
 * Implementation for the OCI Logging module
 */
public class LoggingImpl implements Logging {

    private final String logSpecVersion = "1.0";
    private final String logSource = "Spring application";
    private final String logType = "custom.application";
    private final String subject = "custom.logging";

    private com.oracle.bmc.loggingingestion.Logging logging;

    private String logId;
    public LoggingImpl(com.oracle.bmc.loggingingestion.Logging logging, String logId) {
        this.logging = logging;
        this.logId = logId;
    }


    /**
     * Direct instance of OCI Java SDK Logging Client.
     * @return Logging
     */
    @Override
    public com.oracle.bmc.loggingingestion.Logging getClient() {
        return logging;
    }

    /**
     * Ingest logs associated with a Log OCID
     * @param logText Content of the log to be ingested
     * @return
     */
    public PutLogsResponse putLogs(String logText) {

        PutLogsDetails putLogsDetails = PutLogsDetails.builder()
                .specversion(logSpecVersion)
                .logEntryBatches(new ArrayList<>(Arrays.asList(LogEntryBatch.builder()
                        .entries(new ArrayList<>(Arrays.asList(LogEntry.builder()
                                .data(logText)
                                .id(UUID.randomUUID().toString())
                                .time(new Date()).build())))
                        .source(logSource)
                        .type(logType)
                        .subject(subject)
                        .defaultlogentrytime(new Date()).build()))).build();

        PutLogsRequest putLogsRequest = PutLogsRequest.builder()
                .logId(logId)
                .putLogsDetails(putLogsDetails)
                .build();
        PutLogsResponse response = logging.putLogs(putLogsRequest);
        return response;
    }
}