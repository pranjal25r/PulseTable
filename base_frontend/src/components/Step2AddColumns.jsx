import React, { useState } from "react";

const Step2AddColumns = ({ streamId, onNext }) => {
  const [columns, setColumns] = useState([
    { colName: "", dataType: "", isPrimaryKey: false, saved: false },
  ]);
  const [savedColumns, setSavedColumns] = useState([]);

  const sqlDataTypes = [
    "integer",
    "VARCHAR",
    "TEXT",
    "float",
    "double",
    "date",
    "boolean",
    "long",
  ];

  const handleColumnChange = (index, field, value) => {
    const updated = [...columns];
    updated[index][field] = value;
    setColumns(updated);
  };

  const handleDeleteColumn = (index) => {
    const updated = columns.filter((_, i) => i !== index);
    setColumns(updated);
  };

  const handleSaveColumn = async (index) => {
    const column = columns[index];

    if (!column.colName || !column.dataType) {
      alert("Column name and data type are required.");
      return;
    }

    const payload = {
      stream: { streamId: parseInt(streamId, 10) }, // use passed-in prop
      streamColName: column.colName,
      streamColDataType: column.dataType,
    };
    console.log("Payload to /stream-col:", payload);

    try {
      const response = await fetch("http://localhost:8081/ingestion/stream-col", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error("Backend response:", errorText);
        throw new Error("Failed to save column: " + errorText);
      }

      const resultText = await response.text();
      console.log("Column saved:", resultText);

      // Mark the column as saved and disable its inputs
      const updatedColumns = [...columns];
      updatedColumns[index].saved = true; // Mark as saved
      setColumns(updatedColumns);

      setSavedColumns((prevSaved) => [
        ...prevSaved,
        { ...column, streamId },
      ]);
    } catch (err) {
      console.error("Save failed:", err);
      alert("Failed to save column: " + err.message);
    }
  };

  const handleNext = async () => {
    if (savedColumns.length === columns.length) {
      try {
        const response = await fetch(`http://localhost:8081/ingestion/stream-col/${streamId}`);
        if (!response.ok) throw new Error("Failed to fetch all columns");

        const allColumns = await response.json();
        onNext(allColumns); // pass all columns to next step
      } catch (err) {
        console.error("Fetch failed:", err);
        alert("Failed to fetch all columns: " + err.message);
      }
    } else {
      alert("Please save all columns before proceeding.");
    }
  };

  return (
    <div className="d-flex align-items-center justify-content-center" style={{ height: "100vh" }}>
      <div className="bg-white p-4 rounded shadow" style={{ width: "700px", minHeight: "600px" }}>
        <h2 className="text-primary mb-4 text-center">Step 2: Add Columns</h2>

        {columns.map((column, index) => (
          <div key={index} className="mb-3">
            <div className="d-flex gap-3 align-items-center flex-wrap">
              <input
                type="text"
                value={column.colName}
                onChange={(e) => handleColumnChange(index, "colName", e.target.value)}
                className="form-control"
                placeholder="Column Name"
                disabled={column.saved} // Disable input if column is saved
                style={{
                  backgroundColor: column.saved ? "#d4edda" : "", // Light green background for saved columns
                  color: column.saved ? "#155724" : "", // Dark green text for saved columns
                }}
              />
              <select
                value={column.dataType}
                onChange={(e) => handleColumnChange(index, "dataType", e.target.value)}
                className="form-select"
                disabled={column.saved} // Disable input if column is saved
                style={{
                  backgroundColor: column.saved ? "#d4edda" : "", // Light green background for saved columns
                  color: column.saved ? "#155724" : "", // Dark green text for saved columns
                }}
              >
                <option value="">-- Select Data Type --</option>
                {sqlDataTypes.map((dt) => (
                  <option key={dt} value={dt}>
                    {dt}
                  </option>
                ))}
              </select>

              {!column.saved && (
                <>
                  <label>
                    <input
                      type="checkbox"
                      checked={column.isPrimaryKey}
                      onChange={(e) =>
                        handleColumnChange(index, "isPrimaryKey", e.target.checked)
                      }
                    />{" "}
                    PK
                  </label>
                  <button className="btn btn-danger" onClick={() => handleDeleteColumn(index)}>
                    Delete
                  </button>
                  <button className="btn btn-success" onClick={() => handleSaveColumn(index)}>
                    Save
                  </button>
                </>
              )}
            </div>

            {/* Add horizontal line below saved columns */}
            {column.saved && <hr style={{ marginTop: "20px", marginBottom: "20px" }} />}
          </div>
        ))}

        <button
          className="btn btn-secondary w-100 mt-3"
          onClick={() =>
            setColumns([...columns, { colName: "", dataType: "", isPrimaryKey: false, saved: false }])
          }
        >
          ➕ Add Column
        </button>

        <button
          className="btn btn-primary w-100 mt-4"
          onClick={handleNext}
        >
          Next →
        </button>
      </div>
    </div>
  );
};

export default Step2AddColumns;
