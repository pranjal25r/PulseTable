package com.pranjal.pulsetable.inputmonitor.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InsertionService {

    private final InsertionHelperService helperService;
    private volatile boolean insertionRunning = false;  //this is to stop the process


    public InsertionService(InsertionHelperService helperService) {
        this.helperService = helperService;
    }

    public void stopInsertion() {
        this.insertionRunning = false;
    }

    public Map<String, Object> getStreamParametersById(Integer streamId) {
        return helperService.getStreamParametersById(streamId);
    }


    public Map<String, Object> insertRowsInBatches(int streamId, String tableName, int windowSize, int windowVelocity) {
        // for anti sql injections
        if (!tableName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name");
        }

        // to get details about columns
        List<Map<String, Object>> columns = helperService.getTableColumns(tableName);
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("Table not found or has no columns");
        }

        try {
            // Read all data from CSV
            List<Map<String, String>> allCsvData = helperService.readCsvData(streamId);
            if (allCsvData.isEmpty()) {
                throw new IllegalArgumentException("CSV file empty or could not be read");
            }

            // fetching last row that was processed from the tick table last entry
            Integer lastProcessedRowId = helperService.getLastProcessedRowId(tableName, streamId);
            int startRowIndex = (lastProcessedRowId >= 0) ? lastProcessedRowId + 1 : 0;

            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> allInsertedRows = new ArrayList<>();
            int rowsProcessed = 0;
            int totalRows = allCsvData.size() - startRowIndex;

            if (totalRows <= 0) {
                result.put("message", "No new rows to process");
                result.put("table", tableName);
                result.put("final_row_count", helperService.getCurrentRowCount(tableName));
                return result;
            }

            int currentRowCount = helperService.getCurrentRowCount(tableName);
            this.insertionRunning = true;   //making the flag true so that It can start running
            while (rowsProcessed < totalRows && insertionRunning) {
                int rowsToInsert = Math.min(windowVelocity, totalRows - rowsProcessed);

                // Get current batch - adjust for starting position
                int batchStartIndex = startRowIndex + rowsProcessed;
                int batchEndIndex = batchStartIndex + rowsToInsert;
                List<Map<String, String>> batchData = allCsvData.subList(batchStartIndex, batchEndIndex);

                // this will delete the rows to keep the window size constant
                int totalAfterInsert = currentRowCount + rowsToInsert;
                if (totalAfterInsert > windowSize) {
                    int rowsToDelete = totalAfterInsert - windowSize;
                    helperService.deleteOldestRows(tableName, rowsToDelete);    //here rows get deleted
                    currentRowCount -= rowsToDelete;
                }

                List<Map<String, Object>> batchInsertedRows = new ArrayList<>();
                for (Map<String, String> csvRow : batchData) {
                    Map<String, Object> insertedValues = helperService.insertRowFromCsv(tableName, streamId, columns, csvRow);
                    batchInsertedRows.add(insertedValues);
                    allInsertedRows.add(insertedValues);
                    currentRowCount++;
                }

                //summary of the window as it is now (old rows were already deleted above)
                helperService.updateBatchSummaryStatistics(tableName, streamId);

                //calling service to put entry in tick table
                int windowStartId = batchStartIndex;
                int windowEndId = batchEndIndex - 1;
                helperService.createTickEntry(tableName, streamId, windowStartId, windowEndId);

                rowsProcessed += rowsToInsert;
                System.out.printf("Inserted %d rows (total: %d/%d) - Table size: %d/%d%n",
                        rowsToInsert, rowsProcessed, totalRows, currentRowCount, windowSize);

                // 1second wait time
                if (rowsProcessed < totalRows) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Batch processing interrupted", e);
                    }
                }
            }
            this.insertionRunning=false; //switch off the running flag

            result.put("message", allInsertedRows.size() + " row(s) inserted successfully");
            result.put("table", tableName);
            result.put("inserted_data", allInsertedRows);
            result.put("window_size", windowSize);
            result.put("window_velocity", windowVelocity);
            result.put("final_row_count", currentRowCount);

            return result;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV file: " + e.getMessage(), e);
        }
    }
    public Map<String, Object> insertTimeWiseRowsInBatches(int streamId, String tableName, int windowSize, int windowVelocity) {
        if (!tableName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name");
        }

        // retrieving table columns
        List<Map<String, Object>> columns = helperService.getTableColumns(tableName);
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("Table not found or has no columns");
        }

        try {
            // Read CSV data
            List<Map<String, String>> allCsvData = helperService.readCsvData(streamId);
            if (allCsvData.isEmpty()) {
                throw new IllegalArgumentException("CSV file empty or could not be read");
            }

            // fetching last row that was processed from the tick table last entry
            Integer lastProcessedRowId = helperService.getLastProcessedRowId(tableName, streamId);
            int startRowIndex = (lastProcessedRowId >= 0) ? lastProcessedRowId + 1 : 0;

            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> allInsertedRows = new ArrayList<>();
            int rowsProcessed = 0;
            int totalRows = allCsvData.size() - startRowIndex;

            if (totalRows <= 0) {
                result.put("message", "No new rows to process");
                result.put("table", tableName);
                result.put("final_row_count", helperService.getCurrentRowCount(tableName));
                return result;
            }

            int currentRowCount = helperService.getCurrentRowCount(tableName);

            this.insertionRunning = true;   //making the flag true so that It can start running
            while (rowsProcessed < totalRows && insertionRunning) {
                int rowsToInsert = Math.min(windowVelocity, totalRows - rowsProcessed);

                int batchStartIndex = startRowIndex + rowsProcessed;
                int batchEndIndex = batchStartIndex + rowsToInsert;
                List<Map<String, String>> batchData = allCsvData.subList(batchStartIndex, batchEndIndex);

                // inserting batch data
                List<Map<String, Object>> batchInsertedRows = new ArrayList<>();
                for (Map<String, String> csvRow : batchData) {
                    Map<String, Object> insertedValues = helperService.insertRowFromCsv(tableName, streamId, columns, csvRow);
                    batchInsertedRows.add(insertedValues);
                    allInsertedRows.add(insertedValues);
                    currentRowCount++;
                }

                // delete all rows older than windowsize in seconds
                helperService.deleteRowsOlderThanTimeWindow(tableName, windowSize);

                // updating summary table, after the delete so it only covers rows still in the window
                helperService.updateBatchSummaryStatistics(tableName, streamId);

                //calling service to put entry in tick table
                int windowStartId = batchStartIndex;
                int windowEndId = batchEndIndex - 1;
                helperService.createTickEntry(tableName, streamId, windowStartId, windowEndId);

                currentRowCount = helperService.getCurrentRowCount(tableName);
                rowsProcessed += rowsToInsert;
                System.out.printf("Inserted %d rows (total: %d/%d) - Current table size: %d - Time window: %d second%n",
                        rowsToInsert, rowsProcessed, totalRows, currentRowCount, windowSize);

                if (rowsProcessed < totalRows) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Batch processing interrupted", e);
                    }
                }
            }

            result.put("message", allInsertedRows.size() + " row(s) inserted successfully");
            result.put("table", tableName);
            result.put("inserted_data", allInsertedRows);
            result.put("window_size_seconds", windowSize);
            result.put("window_velocity", windowVelocity);
            result.put("final_row_count", currentRowCount);

            return result;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV file: " + e.getMessage(), e);
        }
    }

    public String getStreamNameById(int streamId) {
        return helperService.getStreamNameById(streamId);
    }
}