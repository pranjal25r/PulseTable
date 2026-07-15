import React, { useEffect, useState } from "react";
import { Container, Card, Button, ListGroup, Navbar, Alert } from "react-bootstrap";
import { useNavigate } from "react-router-dom";


const StreamHome = ({ setStreamId, setStreamname}) => {
  const [streams, setStreams] = useState([]);
  const [error, setError] = useState(null);
  const navigate = useNavigate();


  useEffect(() => {
    const fetchStreams = async () => {
      try {
        const response = await fetch("http://localhost:8083/ingestion/stream-master");
        const data = await response.json();
        setStreams(data);
      } catch (err) {
        console.error("Error fetching streams:", err);
        setError("Failed to fetch streams. Please try again later.");
      }
    };

    fetchStreams();
  }, []);

  const handleStreamClick = async (stream) => {
    try {
      setStreamId(stream.streamId);
      setStreamname(stream.streamName);
  
      // Fetch columns
      const colsResponse = await fetch("http://localhost:8081/retrieve/getStreamColsTableData", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ streamId: stream.streamId }),
      });
      const columns = await colsResponse.json();
  
      // Fetch aggregated columns
      const aggResponse = await fetch("http://localhost:8081/retrieve/getStreamQueriesTableData", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ streamId: stream.streamId }),
      });
      const aggregatedColumns = await aggResponse.json();
  
      // Navigate to dashboard with both sets of data
      navigate("/dashboard", {
        state: {
          streamId: stream.streamId,
          streamName: stream.streamName,
          columns,
          aggregatedColumns,
        },
      });
    } catch (err) {
      console.error("Error fetching stream data:", err);
      setError("Failed to fetch stream details. Please try again later.");
    }
  };
  

  return (
    <>
      {/* Navbar */}
      <Navbar bg="primary" variant="dark" className="mb-4">
        <Container>
          <Navbar.Brand>Stream Selection</Navbar.Brand>
        </Container>
      </Navbar>

      {/* Main Content */}
      <Container>
        <Card className="shadow-lg">
          <Card.Body>
            <Card.Title className="text-center text-primary mb-4">
              Available Streams
            </Card.Title>

            {error && (
              <Alert variant="danger" className="text-center">
                {error}
              </Alert>
            )}

            {streams.length > 0 ? (
              <ListGroup>
                {streams.map((stream) => (
                  <ListGroup.Item
                    action
                    key={stream.streamId}
                    onClick={() => handleStreamClick(stream)}
                    className="d-flex justify-content-between align-items-center"
                  >
                    <span>{stream.streamName}</span>
                  </ListGroup.Item>
                ))}
              </ListGroup>
            ) : (
              <p className="text-muted text-center">No streams available.</p>
            )}

            <div className="text-center mt-4">
              <Button
                variant="primary"
                onClick={() => navigate("/step1")}
              >
                + Create New Stream
              </Button>
            </div>
          </Card.Body>
        </Card>
      </Container>
    </>
  );
};

export default StreamHome;
