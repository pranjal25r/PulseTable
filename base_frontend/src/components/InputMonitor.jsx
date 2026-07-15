import React from "react";
import { useLocation, useNavigate } from "react-router-dom";

const InputMonitor = () => {
  //const [isLoading, setIsLoading] = useState(false);
  //const [dataLoaded, setDataLoaded] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();

  const {
    streamId,
    streamName,
    columns,
    aggregatedColumns
  } = location.state || {};

  const handleGoToDashboard = () => {
    navigate("/dashboard", {
      state: {
        streamId,
        streamName,
        columns,
        aggregatedColumns,
        fromInputMonitor: true,
      }
    });
  };

  return (
    <div style={styles.outer}>
      <div style={styles.container}>
        <h2 style={styles.title}>Monitoring Input Stream</h2>
        <p style={styles.subtitle}>
          Monitoring has started. You can proceed when you're ready.
        </p>

        <button
          onClick={handleGoToDashboard}
          style={{ ...styles.button, backgroundColor: "#007bff" }}
        >
          Go to Dashboard
        </button>
      </div>
    </div>
  );
};

const styles = {
  outer: {
    height: "100vh",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: "#e9ecef"
  },
  container: {
    padding: "30px",
    maxWidth: "500px",
    width: "100%",
    textAlign: "center",
    backgroundColor: "#f8f9fa",
    borderRadius: "10px",
    boxShadow: "0 4px 8px rgba(0,0,0,0.1)"
  },
  title: {
    marginBottom: "10px",
    fontSize: "24px",
    color: "#343a40"
  },
  subtitle: {
    marginBottom: "30px",
    fontSize: "16px",
    color: "#6c757d"
  },
  button: {
    padding: "12px 20px",
    fontSize: "16px",
    color: "white",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer"
  }
};

export default InputMonitor;
