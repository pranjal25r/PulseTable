import React from "react";
import { Button, Container, Card } from "react-bootstrap";
import { useNavigate } from "react-router-dom";

const LandingPage = () => {
  const navigate = useNavigate();

  const handleEnterClick = () => {
    navigate("/stream-home");
  };

  return (
    <Container
      fluid
      className="d-flex justify-content-center align-items-center vh-100"
      style={{
        backgroundColor: "#ffffff", // White background for the entire screen
        margin: 0, // Ensure no margins around the container
        padding: 0, // Remove any padding
      }}
    >
      <Card style={{ width: "100%", maxWidth: "600px", padding: "30px" }}>
        <div className="text-center">
          <h1
            style={{
              fontSize: "2.5rem",
              marginBottom: "20px",
              color: "#007bff", // Same blue color as the Enter button
              whiteSpace: "normal", // Allow title to break to the next line if necessary
            }}
          >
            Stream Data Management System
          </h1>
          <Button
            variant="primary"
            onClick={handleEnterClick}
            style={{
              fontSize: "1.25rem",
              padding: "15px",
              width: "100%", // Make button extend to the full width of the card
            }}
          >
            Enter
          </Button>
        </div>
      </Card>
    </Container>
  );
};

export default LandingPage;
