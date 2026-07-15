import React, { useState, useEffect } from "react";
import { Tab, Nav, Table } from "react-bootstrap";
import axios from "axios";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  Label,
} from "recharts";

const SummaryResults = ({ streamId, aggregatedcolumns }) => {
  const [activeKey, setActiveKey] = useState(null);
  const [data, setData] = useState([]);
  const [lastUpdated, setLastUpdated] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    let intervalId;

    const fetchData = async (colId) => {
      try {
        const response = await axios.post(
          "http://localhost:8083/api/output/columnsummary",
          {
            streamId: streamId,
            colId: colId,
          }
        );

        const newData = response.data;
        setData((prev) => {
          const timeStamps = new Set(prev.map((d) => d.time_stamp));
          const merged = [...prev];
          for (const item of newData) {
            if (!timeStamps.has(item.time_stamp)) {
              merged.push(item);
            }
          }
          return merged;
        });

        setLastUpdated(new Date().toLocaleTimeString());
        setError(null);
      } catch (err) {
        setError("Error fetching data: " + err.message);
      }
    };

    if (activeKey) {
      const [colName, aggType] = activeKey.split("-");
      const colId = aggregatedcolumns.find(
        (col) =>
          col.columnName === colName && col.aggregationType === aggType
      )?.columnId;

      if (colId) {
        setData([]); // Clear old data when switching tabs
        fetchData(colId);
        intervalId = setInterval(() => fetchData(colId), 5000);
      }
    }

    return () => clearInterval(intervalId);
  }, [activeKey, streamId, aggregatedcolumns]);

  const renderTabs = () =>
    aggregatedcolumns.map((column) => {
      const tabKey = `${column.columnName}-${column.aggregationType}`;
      return (
        <Nav.Item key={tabKey}>
          <Nav.Link eventKey={tabKey}>
            {`${column.columnName}-${column.aggregationType}`}
          </Nav.Link>
        </Nav.Item>
      );
    });

  const renderTabContent = () =>
    aggregatedcolumns.map((column) => {
      const tabKey = `${column.columnName}-${column.aggregationType}`;
      const aggKey = column.aggregationType.toLowerCase();

      return (
        <Tab.Pane eventKey={tabKey} key={tabKey}>
          <div className="container mt-3">
            {error && <p style={{ color: "red" }}>{error}</p>}
            {lastUpdated && (
              <p>
                <strong>Last updated at:</strong> {lastUpdated}
              </p>
            )}

            {data.length > 0 ? (
              <>
                <div style={{ maxHeight: "400px", overflowY: "auto", overflowX: "auto" }}>
                  <Table striped bordered hover className="mt-4">
                    <thead>
                      <tr>
                        {Object.keys(data[0]).map((col) => (
                          <th key={col}>{col}</th>
                        ))}
                      </tr>
                    </thead>
                    <tbody>
                      {data.map((item, index) => (
                        <tr key={index}>
                          {Object.keys(data[0]).map((col) => (
                            <td key={col}>{item[col]}</td>
                          ))}
                        </tr>
                      ))}
                    </tbody>
                  </Table>
                </div>
                <div className="mt-4">
                  <h4 style={{ borderBottom: "2px solid #ccc", paddingBottom: "10px" }}>Plots</h4>
                </div>

                {/* Graph appears below the table */}
                <ResponsiveContainer width="100%" height={350}>
                  <LineChart
                    data={data}
                    margin={{ top: 20, right: 40, bottom: 50, left: 50 }} // Added margins for spacing
                  >
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis
                      dataKey="time_stamp"
                      tickFormatter={(t) => new Date(t).toLocaleTimeString()}
                    >
                      <Label
                        value="Timestamp"
                        offset={0}
                        position="bottom"
                        style={{ fontSize: 14, fontWeight: "bold", marginTop: 20 }} // Added marginTop to avoid overlap
                      />
                    </XAxis>
                    <YAxis domain={['auto', 'auto']}>
                      <Label
                        value={aggKey.toUpperCase()}
                        angle={-90}
                        dx={-15} 
                        position="left"
                        style={{ fontSize: 14, fontWeight: "bold", marginTop: 40 }} // Added marginTop to push label down
                      />
                    </YAxis>
                    <Tooltip labelFormatter={(l) => new Date(l).toLocaleString()} />
                    <Legend
                      verticalAlign="top" // Adjusted legend positioning
                      align="right" // Positioned legend to the right to avoid overlap
                    />
                    <Line
                      type="monotone"
                      dataKey={aggKey}
                      stroke="#8884d8"
                      name={aggKey.toUpperCase()}
                      dot={{ r: 3 }}
                      activeDot={{ r: 6 }}
                    />
                  </LineChart>
                </ResponsiveContainer>
              </>
            ) : (
              <p>No data available yet.</p>
            )}
          </div>
        </Tab.Pane>
      );
    });

  return (
    <div className="container mt-5">
      <h2>Summary Results</h2>
      <Tab.Container activeKey={activeKey} onSelect={(k) => setActiveKey(k)}>
        <Nav variant="tabs" className="mb-3">
          {renderTabs()}
        </Nav>
        <Tab.Content>{renderTabContent()}</Tab.Content>
      </Tab.Container>
    </div>
  );
};

export default SummaryResults;
