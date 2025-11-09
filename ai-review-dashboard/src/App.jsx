import { useState } from "react";
import { Box, CssBaseline, AppBar, Toolbar, Typography } from "@mui/material";
import Sidebar from "./components/Sidebar";
import Dashboard from "./components/Dashboard";
import Reviews from "./components/Reviews";

function App() {
  const [page, setPage] = useState("dashboard");

  return (
    <Box sx={{ display: "flex", background: "#0c0c0c", minHeight: "100vh" }}>
      <CssBaseline />

      {/* Sidebar */}
      <Sidebar page={page} setPage={setPage} />

      {/* Top Bar */}
      <AppBar position="fixed" sx={{ ml: 30, background: "#111" }}>
        <Toolbar>
          <Typography variant="h6">AI Code Review Dashboard</Typography>
        </Toolbar>
      </AppBar>

      {/* Main Content */}
      <Box component="main" sx={{ flexGrow: 1, p: 4, mt: 8 }}>
        {page === "dashboard" && <Dashboard />}
        {page === "dashboard" && <Dashboard />}
        {page === "reviews" && <Reviews />}
      </Box>
    </Box>
  );
}

export default App;
