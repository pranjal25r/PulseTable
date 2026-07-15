// // import React,{useEffect} from 'react';
// // import { Tab, Nav, Container, Button, Navbar, NavDropdown } from 'react-bootstrap';
// // import WindowResults from './WindowResults';
// // import SummaryResults from './SummaryResults';
// // import Queries from "./Queries.jsx"
// // import { useNavigate } from "react-router-dom";

// // const Dashboard = ({ streamId ,streamName,columns}) => {
// //   const navigate = useNavigate();

// //   useEffect(() => {
// //     console.log("Received streamId in Dashboard:", streamId);
// //   }, [streamId]);
// //   const handleLogout = async () => {
// //     try {
// //       const response = await fetch("http://localhost:8085/api/stop-insertion", {
// //         method: "POST",
// //       });

// //       if (!response.ok) {
// //         throw new Error("Failed to stop insertion.");
// //       }

// //       alert("Successfully logged out!");
// //       navigate("/"); // Redirect to the home page
// //     } catch (error) {
// //       console.error("Error during logout:", error);
// //       alert("Failed to log out. Please try again.");
// //     }
// //   };

// //   return (
// //     <div>
// //       {/* Navbar with Blue Color */}
// //       <Navbar bg="primary" variant="dark">
// //         <Container>
// //           <Navbar.Brand href="#home">Stream Dashboard</Navbar.Brand>
// //           <Nav className="ml-auto">
// //             <Nav.Item>
// //               <Nav.Link>{streamName}</Nav.Link>
// //             </Nav.Item>
// //           </Nav>
// //         </Container>
// //       </Navbar>

// //       {/* Main Content Area */}
// //       <Container className="mt-4">

// //         <Tab.Container defaultActiveKey="window">
// //           <Nav variant="tabs" className="mb-3">
// //             <Nav.Item><Nav.Link eventKey="window">Window Results</Nav.Link></Nav.Item>
// //             <Nav.Item><Nav.Link eventKey="summary">Summary Results</Nav.Link></Nav.Item>
// //             <Nav.Item><Nav.Link eventKey="Queries">Queries</Nav.Link></Nav.Item>
// //           </Nav>

// //           <Tab.Content>
// //             <Tab.Pane eventKey="window">
// //               <WindowResults streamId={streamId}   />
// //             </Tab.Pane>

// //             <Tab.Pane eventKey="summary">
// //               <SummaryResults  streamId={streamId}/>
// //               </Tab.Pane>
// //             <Tab.Pane eventKey="Queries">
// //               <Queries streamId={streamId}     columns={columns}   streamName={streamName}

// //               />
// //             </Tab.Pane>

// //           </Tab.Content>
// //         </Tab.Container>
// //       </Container>
// //     </div>
// //   );
// // };

// // export default Dashboard;
// //**********************************************************8 */

// import React, { useState } from "react";
// import { Tab, Nav, Container, Button, Navbar } from "react-bootstrap";
// import { useLocation } from "react-router-dom";
// import WindowResults from "./WindowResults";
// import SummaryResults from "./SummaryResults";
// import Queries from "./Queries.jsx";
// import { useNavigate } from "react-router-dom";

// const Dashboard = () => {
//   const [ingestionStarted, setIngestionStarted] = useState(false);
//   const navigate = useNavigate();
//   const location = useLocation();

//   const {
//     streamId,
//     streamName,
//     columns,
//     aggregatedColumns
//   } = location.state || {};
//   console.log("Received streamId in Dashboard:", streamId);
//   console.log("Received streamName in Dashboard:", streamName);
//   console.log("Received columns in Dashboard:", columns);
//   console.log("Received aggregatedcolumns in Dashboard:", aggregatedColumns);

//   // Logout function
//   const handleLogout = async () => {
//     try {
//       const response = await fetch("http://localhost:8085/api/stop-insertion", {
//         method: "POST",
//       });

//       if (!response.ok) {
//         throw new Error("Failed to stop insertion.");
//       }

//       alert("Successfully logged out!");
//       navigate("/"); // Redirect to the home page
//     } catch (error) {
//       console.error("Error during logout:", error);
//       alert("Failed to log out. Please try again.");
//     }
//   };
//   const handleStartIngestion = async () => {
//     try {
//       const response = await fetch("http://localhost:8085/api/insert-batched", {
//         method: "POST",
//         headers: {
//           "Content-Type": "application/json"
//         },
//         body: JSON.stringify({ stream_id: streamId })
//       });

//       if (!response.ok) {
//         throw new Error("Failed to start ingestion.");
//       }

//       //alert("Ingestion started successfully!");
//       setIngestionStarted(true);
//     } catch (error) {
//       console.error("Error starting ingestion:", error);
//       alert("Failed to start ingestion. Please try again.");
//     }
//   };
//   const handleStopIngestion = async () => {
//     try {
//       const response = await fetch("http://localhost:8085/api/stop-insertion", {
//         method: "POST",
//       });

//       if (!response.ok) {
//         throw new Error("Failed to stop ingestion.");
//       }

//       alert("Ingestion stopped.");
//       setIngestionStarted(false);
//     } catch (error) {
//       console.error("Error stopping ingestion:", error);
//       alert("Failed to stop ingestion. Please try again.");
//     }
//   };

//   return (
//     <div>
//       {/* Navbar with Blue Color */}
//       <Navbar bg="primary" variant="dark">
//         <Container>
//           <Navbar.Brand href="#home">Stream Dashboard</Navbar.Brand>
//           <Nav className="ml-auto">
//             <Nav.Item>
//               <Nav.Link>{streamName}</Nav.Link>
//             </Nav.Item>
//             <Nav.Item>
//         <Button
//           variant="outline-light"
//           size="sm"
//           onClick={handleStartIngestion}
//           className="ms-3"
//           disabled={ingestionStarted}
//         >
//           {ingestionStarted ? "Ingestion Started" : "Start Ingestion"}

//         </Button>
//       </Nav.Item>
//       {ingestionStarted && (
//         <Nav.Item>
//           <Button
//             variant="outline-warning"
//             size="sm"
//             onClick={handleStopIngestion}
//             className="ms-3"
//           >
//             Stop Ingestion
//           </Button>
//         </Nav.Item>
//       )}
//             <Nav.Item>
//               <Button
//                 variant="outline-light"
//                 size="sm"
//                 onClick={handleLogout}
//                 className="ms-3"
//               >
//                 Logout
//               </Button>
//             </Nav.Item>
//           </Nav>
//         </Container>
//       </Navbar>

//       {/* Main Content Area */}
//       <Container className="mt-4">
//         <Tab.Container defaultActiveKey="window">
//           <Nav variant="tabs" className="mb-3">
//             <Nav.Item>
//               <Nav.Link eventKey="window">Window Results</Nav.Link>
//             </Nav.Item>
//             <Nav.Item>
//               <Nav.Link eventKey="summary">Summary Results</Nav.Link>
//             </Nav.Item>
//             <Nav.Item>
//               <Nav.Link eventKey="Queries">Queries</Nav.Link>
//             </Nav.Item>
//           </Nav>

//           <Tab.Content>
//             <Tab.Pane eventKey="window">
//               <WindowResults streamId={streamId} />
//             </Tab.Pane>

//             <Tab.Pane eventKey="summary">
//               <SummaryResults streamId={streamId}
//               aggregatedcolumns={aggregatedColumns}/>
//             </Tab.Pane>
//             <Tab.Pane eventKey="Queries">
//               <Queries
//                 streamId={streamId}
//                 columns={columns}
//                 streamName={streamName}
//               />
//             </Tab.Pane>
//           </Tab.Content>
//         </Tab.Container>
//       </Container>
//     </div>
//   );
// };

// export default Dashboard;

import React, { useState, useEffect } from "react";
import { Tab, Nav, Container, Button, Navbar } from "react-bootstrap";
import { useLocation, useNavigate } from "react-router-dom";
import WindowResults from "./WindowResults";
import SummaryResults from "./SummaryResults";
import Queries from "./Queries.jsx";

const Dashboard = () => {
  const [ingestionStarted, setIngestionStarted] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const { streamId, streamName, columns, aggregatedColumns } =
    location.state || {};

  // Restore ingestion state on reload
  useEffect(() => {
    const status = localStorage.getItem("ingestionStarted");
    if (location.state?.fromInputMonitor) {
      setIngestionStarted(false);
      localStorage.removeItem("ingestionStarted");
    } else if (status === "true") {
      setIngestionStarted(true);
    }
    console.log("Received streamId in Dashboard:", streamId);
    console.log("Received streamName in Dashboard:", streamName);
    console.log("Received columns in Dashboard:", columns);
    console.log("Received aggregatedColumns in Dashboard:", aggregatedColumns);
  }, [streamId, streamName, columns, aggregatedColumns]);

  // Start ingestion
  const handleStartIngestion = async () => {
    setIngestionStarted(true);

    try {
      const response = await fetch("http://localhost:8085/api/insert-batched", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ stream_id: streamId }),
      });

      if (!response.ok) {
        throw new Error("Failed to start ingestion.");
      }
      localStorage.setItem("ingestionStarted", "true");
    } catch (error) {
      console.error("Error starting ingestion:", error);
      alert("Failed to start ingestion. Please try again.");
    }
  };

  // Stop ingestion
  const handleStopIngestion = async () => {
    setIngestionStarted(false);

    try {
      const response = await fetch("http://localhost:8085/api/stop-insertion", {
        method: "POST",
      });

      if (!response.ok) {
        throw new Error("Failed to stop ingestion.");
      }

      localStorage.removeItem("ingestionStarted");
      alert("Ingestion stopped.");
    } catch (error) {
      console.error("Error stopping ingestion:", error);
      alert("Failed to stop ingestion. Please try again.");
    }
  };

  // Logout
  const handleLogout = async () => {
    try {
      const response = await fetch("http://localhost:8085/api/stop-insertion", {
        method: "POST",
      });

      if (!response.ok) {
        throw new Error("Failed to stop insertion.");
      }

      localStorage.removeItem("ingestionStarted");
      alert("Successfully logged out!");
      navigate("/");
    } catch (error) {
      console.error("Error during logout:", error);
      alert("Failed to log out. Please try again.");
    }
  };
  const StatusDot = ({ started }) => (
    <span
      style={{
        display: "inline-block",
        width: "10px",
        height: "10px",
        borderRadius: "50%",
        backgroundColor: started ? "limegreen" : "red",
        marginLeft: "8px"
      }}
      title={started ? "Ingestion Started" : "Ingestion Stopped"}
    />
  );
  

  return (
    <div>
      {/* Navbar with controls */}
      <Navbar bg="primary" variant="dark">
        <Container>
          <Navbar.Brand href="#home">Stream Dashboard</Navbar.Brand>
          <div className="d-flex align-items-center ms-auto">
            <div className="text-white me-3">{streamName}</div>

            {/* <Button
              variant="outline-light"
              size="sm"
              onClick={handleStartIngestion}
              disabled={ingestionStarted}
              className="me-2"
            >
              {ingestionStarted ? "Ingestion Started" : "Start Ingestion"}
            </Button>

            <Button
              variant="outline-warning"
              size="sm"
              onClick={handleStopIngestion}
              className="me-2"
              style={{ display: ingestionStarted ? "inline-block" : "none" }}
            >
              Stop Ingestion
            </Button> */}
            <Nav.Item>
  <Button
    variant="outline-light"
    size="sm"
    onClick={handleStartIngestion}
    className="ms-3"
    disabled={ingestionStarted}
  >
    {ingestionStarted ? "Ingestion Started" : "Start Ingestion"}
    <StatusDot started={ingestionStarted} />
  </Button>
</Nav.Item>

{ingestionStarted && (
  <Nav.Item>
    <Button
      variant="outline-warning"
      size="sm"
      onClick={handleStopIngestion}
      className="ms-3"
    >
      Stop Ingestion
    </Button>
  </Nav.Item>
)}

            <Button variant="outline-light" size="sm" onClick={handleLogout}>
              Logout
            </Button>
          </div>
        </Container>
      </Navbar>

      {/* Main content area */}
      <Container className="mt-4">
        <Tab.Container defaultActiveKey="window">
          <Nav variant="tabs" className="mb-3">
            <Nav.Item>
              <Nav.Link eventKey="window">Window Results</Nav.Link>
            </Nav.Item>
            <Nav.Item>
              <Nav.Link eventKey="summary">Summary Results</Nav.Link>
            </Nav.Item>
            <Nav.Item>
              <Nav.Link eventKey="Queries">Queries</Nav.Link>
            </Nav.Item>
          </Nav>

          <Tab.Content>
            <Tab.Pane eventKey="window">
              <WindowResults streamId={streamId} />
            </Tab.Pane>

            <Tab.Pane eventKey="summary">
              <SummaryResults
                streamId={streamId}
                aggregatedcolumns={aggregatedColumns}
              />
            </Tab.Pane>

            <Tab.Pane eventKey="Queries">
              <Queries
                streamId={streamId}
                columns={columns}
                streamName={streamName}
              />
            </Tab.Pane>
          </Tab.Content>
        </Tab.Container>
      </Container>
    </div>
  );
};

export default Dashboard;
