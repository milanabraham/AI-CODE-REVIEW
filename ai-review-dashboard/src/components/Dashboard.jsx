import { useEffect, useState } from "react";
import { Grid, Paper, Typography } from "@mui/material";

export default function Dashboard() {
  const [stats, setStats] = useState(null);

  useEffect(() => {
    fetch("http://localhost:8080/api/reviews/stats")
      .then((res) => res.json())
      .then((data) => setStats(data));
  }, []);

  if (!stats) return <Typography color="gray">Loading...</Typography>;

  return (
    <Grid container spacing={3}>
      {/* Total */}
      <Grid item xs={12} md={4}>
        <Paper sx={{ p: 3, background: "#1a1a1a", color: "#fff" }}>
          <Typography variant="h5">Total Reviews</Typography>
          <Typography variant="h3">{stats.total}</Typography>
        </Paper>
      </Grid>

      {/* Low */}
      <Grid item xs={12} md={4}>
        <Paper sx={{ p: 3, background: "#0f3d0f", color: "#a5ffa5" }}>
          <Typography variant="h5">Low Risk</Typography>
          <Typography variant="h3">{stats.byStatus["low-risk"]}</Typography>
        </Paper>
      </Grid>

      {/* High */}
      <Grid item xs={12} md={4}>
        <Paper sx={{ p: 3, background: "#3d0f0f", color: "#ffaaaa" }}>
          <Typography variant="h5">High Risk</Typography>
          <Typography variant="h3">{stats.byStatus["high-risk"]}</Typography>
        </Paper>
      </Grid>
    </Grid>
  );
}
