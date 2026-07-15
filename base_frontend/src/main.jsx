import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import './index.css'; // Tailwind CSS should be imported here
import App from './App';
import 'bootstrap/dist/css/bootstrap.min.css';


// ReactDOM.render(
//   <React.StrictMode>
//     <Router>
//       <Routes>
//         <Route path="*" element={<App />} />
//       </Routes>
//     </Router>
//   </React.StrictMode>
//   document.getElementById("root")

// );


const root = ReactDOM.createRoot(document.getElementById('root'));

root.render(
  <React.StrictMode>
    <Router>
      <App />
    </Router>
  </React.StrictMode>
);
