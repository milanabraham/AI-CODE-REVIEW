import { Box, List, ListItemButton, ListItemText } from "@mui/material";

export default function Sidebar({ page, setPage }) {
  return (
    <Box
      sx={{
        width: 240,
        background: "#141414",
        height: "100vh",
        color: "#fff",
        position: "fixed",
        borderRight: "1px solid #222",
      }}
    >
      <List>
        <ListItemButton selected={page === "dashboard"} onClick={() => setPage("dashboard")}>
          <ListItemText primary="Dashboard" />
        </ListItemButton>

        <ListItemButton selected={page === "reviews"} onClick={() => setPage("reviews")}>
          <ListItemText primary="PR Reviews" />
        </ListItemButton>

      </List>
    </Box>
  );
}
