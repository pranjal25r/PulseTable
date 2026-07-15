import React from "react";

const Step1DatabaseName = ({
  streamName,
  setStreamname,
  windowType,
  setWindowtype,
  windowSize,
  setWindowsize,
  windowVelocity,
  setWindowvelocity,
  dataSourceType,
  setDataSourceType,
  dataSourcePath,
  setDataSourcePath,
  onNext,
  onBack,
}) => {
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
      {/* Status bar */}
      <div className="position-absolute top-0 start-0 w-100 p-3 bg-white shadow-sm">
        <div className="d-flex justify-content-center gap-4">
          <span className="fw-bold text-primary">Step 1</span>
        </div>
      </div>

      {/* Center Card */}
      <div className="bg-white p-4 rounded shadow" style={{ width: "500px", minHeight: "400px" }}>
        <h2 className="text-primary mb-4 text-center">Step 1: Stream Info</h2>

        <div className="mb-3">
          <label className="form-label fw-bold">Stream Name</label>
          <input
            type="text"
            value={streamName}
            onChange={(e) => setStreamname(e.target.value)}
            className="form-control"
            placeholder="Enter stream name"
          />
        </div>

        <div className="mb-3">
          <label className="form-label fw-bold">Window Type</label>
          <select
            value={windowType}
            onChange={(e) => setWindowtype(e.target.value)}
            className="form-select"
          >
            <option value="">-- Select Type --</option>
            <option value="sizetype">Size Type</option>
            <option value="timetype">Time Type</option>
          </select>
        </div>

        <div className="mb-3">
          <label className="form-label fw-bold">Window Size</label>
          <input
            type="number"
            value={windowSize}
            onChange={(e) => setWindowsize(Number(e.target.value))}
            className="form-control"
            placeholder="Enter window size"
            min="1" // Added min value for validation
          />
        </div>

        <div className="mb-4">
          <label className="form-label fw-bold">Window Velocity</label>
          <input
            type="number"
            value={windowVelocity}
            onChange={(e) => setWindowvelocity(Number(e.target.value))}
            className="form-control"
            placeholder="Enter window velocity"
            min="1" // Added min value for validation
          />
        </div>
        <div className="mb-3">
          <label className="form-label fw-bold">Data Source Type</label>
          <select
            value={dataSourceType}
            onChange={(e) => setDataSourceType(e.target.value)}
            className="form-select"
          >
            <option value="">-- Select Source Type --</option>
            <option value="url">URL</option>
            <option value="folder">Folder</option>
          </select>
        </div>

        <div className="mb-4">
          <label className="form-label fw-bold">Data Source Path</label>
          <input
            type="text"
            value={dataSourcePath}
            onChange={(e) => setDataSourcePath(e.target.value)}
            className="form-control"
            placeholder="Enter data source path"
          />
        </div>

        {/* <button
          onClick={onNext}
          disabled={!streamName.trim() || !windowType || !windowSize || !windowVelocity ||!dataSourceType ||
            !dataSourcePath.trim()}
          className="btn btn-primary w-100"
        >
          Next →
        </button> */}
        {/* Buttons */}
        <div className="d-flex justify-content-between">
          <button
            onClick={onBack}
            className="btn btn-secondary"
          >
            ← Back
          </button>
          <button
            onClick={onNext}
            disabled={
              !streamName.trim() ||
              !windowType ||
              !windowSize ||
              !windowVelocity ||
              !dataSourceType ||
              !dataSourcePath.trim()
            }
            className="btn btn-primary"
          >
            Next →
          </button>
            </div>
      </div>
    </div>
  );
};

export default Step1DatabaseName;


