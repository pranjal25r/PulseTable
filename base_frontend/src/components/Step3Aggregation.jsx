import React, { useState } from "react";

const Step3Aggregation = ({ columns, onNext }) => {
  const [selectedColumn, setSelectedColumn] = useState("");
  const [selectedAggFunction, setSelectedAggFunction] = useState("");
  const [aggregations, setAggregations] = useState([]);

  // Predefined aggregation functions for selection
  const aggFunctions = ["SUM", "MIN", "MAX", "AVG"];

  // Handle column change
  const handleColumnChange = (e) => {
    setSelectedColumn(e.target.value);
  };

  // Handle aggregation function change
  const handleAggFunctionChange = (e) => {
    setSelectedAggFunction(e.target.value);
  };

  // Handle the 'Save' button to store the selected aggregation
  const handleSaveAggregation = () => {
    if (selectedColumn && selectedAggFunction) {
      const columnData = columns.find(
        (col) => col.streamColName === selectedColumn
      );

      if (columnData) {
        const aggregation = {
          stream: { streamId: columnData.streamId },
          streamCol: { streamColId: columnData.streamColId },
          aggFunction: selectedAggFunction,
          colName: columnData.streamColName // Add column name for display
        };

        // Log the aggregation being sent to the backend for debugging
        console.log("Sending Aggregation to Backend:", aggregation);

        // Add to the list of aggregations without resetting the form
        setAggregations([...aggregations, aggregation]);

        // Optionally, send the aggregation data to backend
        sendAggregationToBackend(aggregation);
      }
    } else {
      alert("Please select both a column and an aggregation function.");
    }
  };

  // Function to send aggregation data to the backend
  const sendAggregationToBackend = async (aggregation) => {
    try {
      const response = await fetch("http://localhost:8081/ingestion/stream-query", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(aggregation),
      });

      const rawResponse = await response.text();  // Use `.text()` to get raw response
      console.log("Raw Response from Backend:", rawResponse);

      if (response.ok) {
        // Check if the response is a success message (plain text)
        if (rawResponse.includes("Stream Query saved successfully")) {
          console.log("Aggregation saved successfully!");
        } else {
          console.error("Unexpected response:", rawResponse);
          alert("Unexpected response from backend.");
        }
      } else {
        console.error("Error response from backend:", rawResponse);
        alert("Error submitting aggregation.");
      }
    } catch (error) {
      console.error("Error submitting aggregation:", error);
      alert("Error submitting aggregation.");
    }
  };

  // Add another aggregation entry (without resetting existing values)
  const handleAddAnotherAggregation = () => {
    setSelectedColumn("");  // Clear only the column and aggregation function fields for a new input
    setSelectedAggFunction("");
  };

  const handleNext = () => {
  if (aggregations.length > 0) {
    const streamId = aggregations[0].stream.streamId;
    const streamName = columns.find(col => col.streamId === streamId)?.streamName || "Unknown Stream";
    const aggregatedColumns = aggregations.map((agg) => ({
      columnId: agg.streamCol?.streamColId || "unknown",
      aggregationType: agg.aggFunction || "UNKNOWN",
      columnName: agg.colName || "unknown",//columnName: agg.column.columnName || "unknown"
    }));
    console.log("Aggregated Columns:", aggregatedColumns);

    // Pass everything to onNext
    onNext(streamId, streamName, aggregatedColumns);  } else {
    alert("Please save at least one aggregation.");
  }
};


  return (
    <div
      style={{
        height: "100vh",
        width: "100vw",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        backgroundColor: "#f8f9fa",
        position: "relative",
      }}
    >
      {/* Step Progress Bar */}
      <div className="position-absolute top-0 start-0 w-100 p-3 bg-white shadow-sm">
        <div className="d-flex justify-content-center gap-4">
          <span className="text-muted">Step 1</span>
          <span className="text-muted">Step 2</span>
          <span className="fw-bold text-primary">Step 3</span>
          <span className="text-muted">Step 4</span>
        </div>
      </div>

      {/* Aggregation Card */}
      <div className="bg-white p-4 rounded shadow" style={{ width: "90%", maxWidth: "900px", maxHeight: "90vh", overflowY: "auto" }}>
        <h2 className="text-primary mb-4 text-center">Step 3: Select Aggregation</h2>

        {/* Column Selection */}
        <div className="mb-3">
          <label className="form-label">Column Name:</label>
          <select value={selectedColumn} onChange={handleColumnChange} className="form-select">
            <option value="">Select Column</option>
            {columns.map((column) => (
              <option key={column.streamColId} value={column.streamColName}>
                {column.streamColName}
              </option>
            ))}
          </select>
        </div>

        {/* Aggregation Function Selection */}
        <div className="mb-3">
          <label className="form-label">Aggregation Function:</label>
          <select value={selectedAggFunction} onChange={handleAggFunctionChange} className="form-select">
            <option value="">Select Aggregation Function</option>
            {aggFunctions.map((agg) => (
              <option key={agg} value={agg}>
                {agg}
              </option>
            ))}
          </select>
        </div>

        {/* Save and Add Buttons */}
        <div className="d-flex justify-content-between">
          <button className="btn btn-primary" onClick={handleSaveAggregation}>Save Aggregation</button>
          <button className="btn btn-outline-secondary" onClick={handleAddAnotherAggregation}>Add Another Aggregation</button>
        </div>

        {/* Saved Aggregations List */}
        <div className="mt-4">
          <h5 className="text-primary">Saved Aggregations</h5>
          <ul className="list-group">
            {aggregations.map((agg, index) => (
              <li key={index} className="list-group-item">
                Column: {agg.streamCol.streamColId}, Agg Function: {agg.aggFunction}
              </li>
            ))}
          </ul>
        </div>

        {/* Next Button */}
        <div className="d-flex justify-content-between mt-4">
          <button className="btn btn-primary" onClick={handleNext}>Next →</button>
        </div>
      </div>
    </div>
  );
};

export default Step3Aggregation;
