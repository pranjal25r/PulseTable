import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Table, Button } from 'react-bootstrap';

const WindowResults = ({ streamId }) => {
  const [data, setData] = useState([]); 
  const [loading, setLoading] = useState(false); 
  const [error, setError] = useState(null); 
  const [timestamp, setTimestamp] = useState(null); // ✅ new

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.post("http://localhost:8083/api/output/window", {
        streamId: streamId,
      });

      setData(response.data);
      setTimestamp(new Date().toLocaleString()); // ✅ set timestamp here
    } catch (err) {
      setError("Error fetching data: " + err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [streamId]);

  const columns = data.length > 0 ? Object.keys(data[0]) : [];

  return (
    <div className="container mt-5">
      <h2>Window Results</h2>

      {error && <p style={{ color: 'red' }}>{error}</p>}
      {loading && <p>Loading...</p>}

      {!loading && data.length === 0 && (
        <p style={{ color: 'gray' }}>No content available.</p>
      )}

      {!loading && data.length > 0 && (
        <Table striped bordered hover>
          <thead>
            <tr>
              {columns.map((col) => (
                <th key={col}>{col}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {data.map((item, index) => (
              <tr key={index}>
                {columns.map((col) => (
                  <td key={col}>{item[col]}</td>
                ))}
              </tr>
            ))}
          </tbody>
        </Table>
      )}

      {timestamp && <p><strong>Last updated:</strong> {timestamp}</p>}

      <Button onClick={fetchData}>Refresh</Button>
    </div>
  );
};

export default WindowResults;
