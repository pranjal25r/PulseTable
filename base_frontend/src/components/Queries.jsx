import React, { useState,useEffect } from 'react';
import { Button, Form, Tabs, Tab, Table, Spinner, Alert } from 'react-bootstrap';
import axios from 'axios';

const Queries = ({ streamId ,columns,streamName}) => {

  const [key, setKey] = useState('querySelection');
  const [selectCols, setSelectCols] = useState([]);
  const [groupByCols, setGroupByCols] = useState([]);
  const [orderByCols, setOrderByCols] = useState([]);
  const [whereClause, setWhereClause] = useState('');
  const [aggregation, setAggregation] = useState({});
  const [havingClause, setHavingClause] = useState('');
  const [queryPreview, setQueryPreview] = useState('');
  const [queryResult, setQueryResult] = useState([]);
  const [freeformQuery, setFreeformQuery] = useState('');
  const [freeformResult, setFreeformResult] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [groupByError, setGroupByError] = useState('');
  const [selectedColumn, setSelectedColumn] = useState(""); // Use this state to control the dropdown selection
  //columns = ['id'	,'age',	'gender',	'income',	'education',	'region',	'purchase_amount',	'product_category',	'promotion_usage',	'satisfaction_score',	'created_at'];
  const availableColumns = columns || [];
  //streamName ="customer";
  console.log("columns: ",availableColumns);
  useEffect(() => {
    const handler = setTimeout(() => {
      const selectParts = selectCols.map((col) =>
        aggregation[col] ? `${aggregation[col]}(${col}) AS ${aggregation[col]}_${col}` : col
      );
  
      let query = `SELECT ${selectParts.length > 0 ? selectParts.join(', ') : '*'} FROM sdb_${streamName}`;
      if (whereClause) query += ` WHERE ${whereClause}`;
      if (groupByCols.length > 0) query += ` GROUP BY ${groupByCols.join(', ')}`;
      if (havingClause) query += ` HAVING ${havingClause}`;
      if (orderByCols.length > 0) {
        const orderByString = orderByCols.map(({ col, order }) => `${col} ${order}`).join(', ');
        query += ` ORDER BY ${orderByString}`;
      }
      setQueryPreview(query);
  
      const nonAggregatedCols = selectCols.filter((col) => !aggregation[col]);
      if (groupByCols.length > 0 && nonAggregatedCols.length === 0) {
        setGroupByError('GROUP BY cannot be used when all columns are aggregated.');
      } else {
        setGroupByError('');
      }
    }, 300); // debounce for 300ms
  
    return () => clearTimeout(handler);
  }, [selectCols, aggregation, whereClause, groupByCols, havingClause, orderByCols, streamId]);
  
  // Execute the query
  const executeQuery = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await axios.post('http://localhost:8083/api/execute-query', {
        raw_query: queryPreview,
        timestamp: new Date().toISOString(),
      });
      setQueryResult(response.data.data || []);
    } catch (err) {
      setError('Failed to execute the query. Please try again.');
      console.log(err);
    } finally {
      setLoading(false);
    }
  };

  // Execute freeform query
  const executeFreeform = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await axios.post('http://localhost:8083/api/execute-query', {
        raw_query: freeformQuery,
        timestamp: new Date().toISOString(),
      });
      setFreeformResult(response.data.data || []);
    } catch (err) {
      setError('Failed to execute the query. Please try again.');
      console.log(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h4 className="mb-4">Queries</h4>
      <Tabs activeKey={key} onSelect={(k) => setKey(k)} className="mb-4">
        {/* Query Selection Tab */}
        <Tab eventKey="querySelection" title="Query Selection">
          {/* SELECT Section */}
          <h5>SELECT</h5>
          <Form.Group className="mb-3">
          <Form.Label>Select Columns</Form.Label>
<Form.Select
  onChange={(e) => {
    const col = e.target.value;
    if (col && !selectCols.includes(col)) {
      setSelectCols([...selectCols, col]);
    }
  }}
  value={selectedColumn}
  defaultValue=""
>
  <option value="" disabled>
    Select column
  </option>
  {availableColumns
    .filter((col) => !selectCols.includes(col.streamColName)) // Access streamColName here
    .map((col) => (
      <option key={col.streamColId} value={col.streamColName}> {/* Use streamColName here */}
        {col.streamColName} {/* Render the streamColName property here */}
      </option>
    ))}
</Form.Select>


          </Form.Group>
          <div className="d-flex flex-column gap-2 mb-3">
            {selectCols.map((col) => (
              <div key={col} className="d-flex align-items-center gap-3">
                <Button
                  variant="outline-danger"
                  size="sm"
                  onClick={() => {
                    setSelectCols(selectCols.filter((c) => c !== col));
                    const updatedAggregation = { ...aggregation };
                    delete updatedAggregation[col];
                    setAggregation(updatedAggregation);
                    setGroupByCols(groupByCols.filter((g) => g !== col));
                    setOrderByCols(orderByCols.filter((o) => o.col !== col));
                    //updateQueryPreview();
                  }}
                >
                  {col} &times;
                </Button>
                <Form.Select
                  value={aggregation[col] || ''}
                  onChange={(e) => {
                    const value = e.target.value;
                    setAggregation({
                      ...aggregation,
                      [col]: value ? value : '',
                    });
                    //updateQueryPreview();
                  }}
                >
                  <option value="">-- No Aggregation --</option>
                  <option value="SUM">SUM</option>
                  <option value="AVG">AVG</option>
                  <option value="MAX">MAX</option>
                  <option value="MIN">MIN</option>
                  <option value="COUNT">COUNT</option>
                </Form.Select>
              </div>
            ))}
          </div>

          {/* WHERE Section */}
          <h5>WHERE</h5>
          <Form.Group className="mb-3">
            <Form.Label>Enter WHERE Clause</Form.Label>
            <Form.Control
              type="text"
              placeholder="e.g., column1 = 'value'"
              value={whereClause}
              onChange={(e) => {
                setWhereClause(e.target.value);
                //updateQueryPreview();
              }}
            />
          </Form.Group>

          {/* GROUP BY Section */}
          {selectCols.some((col) => !aggregation[col]) && (
            <>
              <h5>GROUP BY</h5>
              <Form.Group className="mb-3">
                <Form.Label>Select Columns for GROUP BY</Form.Label>
                {selectCols
                  .filter((col) => !aggregation[col])
                  .map((col) => (
                    <Form.Check
                      key={col}
                      type="checkbox"
                      label={col}
                      checked={groupByCols.includes(col)}
                      onChange={() => {
                        const updated = groupByCols.includes(col)
                          ? groupByCols.filter((g) => g !== col)
                          : [...groupByCols, col];
                        setGroupByCols(updated);
                        //updateQueryPreview();
                      }}
                    />
                  ))}
              </Form.Group>
              {groupByError && <Alert variant="danger">{groupByError}</Alert>}
            </>
          )}

          {/* HAVING Section */}
          {groupByCols.length > 0 && (
            <>
              <h5>HAVING</h5>
              <Form.Group className="mb-3">
                <Form.Label>Enter HAVING Clause</Form.Label>
                <Form.Control
                  type="text"
                  placeholder="e.g., SUM(column1) > 100"
                  value={havingClause}
                  onChange={(e) => {
                    setHavingClause(e.target.value);
                    //updateQueryPreview();
                  }}
                />
              </Form.Group>
            </>
          )}

          {/* ORDER BY Section */}
          <h5>ORDER BY</h5>
          <Form.Group className="mb-3">
            <Form.Label>Select Columns for ORDER BY</Form.Label>
            <Form.Select
              onChange={(e) => {
                const col = e.target.value;
                if (col && !orderByCols.some((o) => o.col === col)) {
                  setOrderByCols([...orderByCols, { col, order: 'ASC' }]);
                  //updateQueryPreview();
                }
              }}
              defaultValue=""
            >
              <option value="" disabled>
                Select column
              </option>
              {selectCols
                .filter((col) => !orderByCols.some((o) => o.col === col))
                .map((col) => (
                  <option key={col} value={col}>
                    {col}
                  </option>
                ))}
            </Form.Select>
          </Form.Group>
          <div className="d-flex flex-wrap gap-2 mb-3">
            {orderByCols.map(({ col, order }, idx) => (
              <div key={idx} className="d-flex align-items-center gap-2">
                <Button
                  variant="outline-danger"
                  size="sm"
                  onClick={() => {
                    setOrderByCols(orderByCols.filter((o) => o.col !== col));
                    //updateQueryPreview();
                  }}
                >
                  {col} &times;
                </Button>
                <Form.Select
                  value={order}
                  onChange={(e) => {
                    const updated = [...orderByCols];
                    updated[idx].order = e.target.value;
                    setOrderByCols(updated);
                   // updateQueryPreview();
                  }}
                >
                  <option value="ASC">Ascending</option>
                  <option value="DESC">Descending</option>
                </Form.Select>
              </div>
            ))}
          </div>

          {/* Query Preview */}
          <h5>Query Preview</h5>
          <Form.Control as="textarea" rows={4} value={queryPreview} readOnly className="mb-3" />

          {/* Execute Button */}
          <Button variant="primary" onClick={executeQuery} disabled={loading}>
            {loading ? <Spinner animation="border" size="sm" /> : 'Execute Query'}
          </Button>

          {/* Query Result */}
          <h5 className="mt-4">Query Result</h5>
          {error && <div className="text-danger">{error}</div>}
          {queryResult.length > 0 ? (
            <Table striped bordered hover>
              <thead>
                <tr>
                  {Object.keys(queryResult[0]).map((key) => (
                    <th key={key}>{key}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {queryResult.map((row, idx) => (
                  <tr key={idx}>
                    {Object.values(row).map((value, colIdx) => (
                      <td key={colIdx}>{value}</td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </Table>
          ) : (
            <div>No data available.</div>
          )}
        </Tab>

        {/* Freeform Query Tab */}
        <Tab eventKey="freeform" title="Freeform">
          <h5>Freeform Query</h5>
          <Form.Group className="mb-3">
            <Form.Label>Write your query</Form.Label>
            <Form.Control
              as="textarea"
              rows={5}
              value={freeformQuery}
              onChange={(e) => setFreeformQuery(e.target.value)}
              placeholder="Enter your custom SQL query here..."
            />
          </Form.Group>
          <Button variant="primary" onClick={executeFreeform} disabled={loading}>
            {loading ? <Spinner animation="border" size="sm" /> : 'Execute Query'}
          </Button>

          {/* Freeform Query Result */}
          <h5 className="mt-4">Query Result</h5>
          {error && <div className="text-danger">{error}</div>}
          {freeformResult.length > 0 ? (
            <Table striped bordered hover>
              <thead>
                <tr>
                  {Object.keys(freeformResult[0]).map((key) => (
                    <th key={key}>{key}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {freeformResult.map((row, idx) => (
                  <tr key={idx}>
                    {Object.values(row).map((value, colIdx) => (
                      <td key={colIdx}>{value}</td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </Table>
          ) : (
            <div>No data available.</div>
          )}
        </Tab>
      </Tabs>
    </div>
  );
};

export default Queries;
