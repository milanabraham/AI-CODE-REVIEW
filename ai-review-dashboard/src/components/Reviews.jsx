import { useEffect, useState } from "react";
import {
  Box,
  Button,
  TextField,
  MenuItem,
  Grid,
  Paper,
  Dialog,
  DialogTitle,
  DialogContent,
  Typography,
} from "@mui/material";
import { DataGrid } from "@mui/x-data-grid";
import dayjs from "dayjs";

export default function Reviews() {
  const [rows, setRows] = useState([]);
  const [filters, setFilters] = useState({
    repo: "",
    status: "",
    riskMin: "",
    riskMax: "",
    q: "",
  });

  const [page, setPage] = useState(0);
  const [pageSize] = useState(20);
  const [totalRows, setTotalRows] = useState(0);

  const [modalOpen, setModalOpen] = useState(false);
  const [selectedReview, setSelectedReview] = useState(null);

  // Load table data from backend
  const loadData = () => {
    const params = new URLSearchParams({
      page,
      size: pageSize,
      ...filters,
    });

    fetch("http://localhost:8080/api/reviews?" + params.toString())
      .then((res) => res.json())
      .then((data) => {
        setRows(data.content);
        setTotalRows(data.totalElements);
      });
  };

  useEffect(() => {
    loadData();
  }, [page]);

  const handleFilterChange = (field, value) => {
    setFilters({ ...filters, [field]: value });
  };

  const applyFilters = () => {
    setPage(0);
    loadData();
  };

  const exportCsv = () => {
    const params = new URLSearchParams(filters);
    window.open("http://localhost:8080/api/reviews/export?" + params.toString());
  };

  const columns = [
    { field: "id", headerName: "ID", width: 70 },
    { field: "repo", headerName: "Repository", flex: 1 },
    { field: "prNumber", headerName: "PR #", width: 100 },
    { field: "riskScore", headerName: "Risk", width: 100 },
    { field: "status", headerName: "Status", flex: 1 },
    {
      field: "createdAt",
      headerName: "Date",
      width: 180,
      valueGetter: (v) => dayjs(v).format("YYYY-MM-DD HH:mm"),
    },
    {
      field: "view",
      headerName: "View",
      width: 120,
      renderCell: (params) => (
        <Button
          variant="contained"
          size="small"
          onClick={() => {
            setSelectedReview(params.row);
            setModalOpen(true);
          }}
        >
          Open
        </Button>
      ),
    },
  ];

  return (
    <Box>
      {/* Filters */}
      <Paper sx={{ p: 3, mb: 3, background: "#1a1a1a", color: "#fff" }}>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={3}>
            <TextField
              label="Repository"
              fullWidth
              value={filters.repo}
              onChange={(e) => handleFilterChange("repo", e.target.value)}
              InputProps={{ style: { color: "white" } }}
              InputLabelProps={{ style: { color: "gray" } }}
            />
          </Grid>

          <Grid item xs={12} sm={3}>
            <TextField
              select
              label="Status"
              fullWidth
              value={filters.status}
              onChange={(e) => handleFilterChange("status", e.target.value)}
              InputProps={{ style: { color: "white" } }}
              InputLabelProps={{ style: { color: "gray" } }}
            >
              <MenuItem value="">All</MenuItem>
              <MenuItem value="low-risk">Low Risk</MenuItem>
              <MenuItem value="medium-risk">Medium Risk</MenuItem>
              <MenuItem value="high-risk">High Risk</MenuItem>
            </TextField>
          </Grid>

          <Grid item xs={6} sm={2}>
            <TextField
              label="Risk Min"
              type="number"
              fullWidth
              value={filters.riskMin}
              onChange={(e) => handleFilterChange("riskMin", e.target.value)}
              InputProps={{ style: { color: "white" } }}
              InputLabelProps={{ style: { color: "gray" } }}
            />
          </Grid>

          <Grid item xs={6} sm={2}>
            <TextField
              label="Risk Max"
              type="number"
              fullWidth
              value={filters.riskMax}
              onChange={(e) => handleFilterChange("riskMax", e.target.value)}
              InputProps={{ style: { color: "white" } }}
              InputLabelProps={{ style: { color: "gray" } }}
            />
          </Grid>

          <Grid item xs={12} sm={2}>
            <TextField
              label="Search"
              fullWidth
              value={filters.q}
              onChange={(e) => handleFilterChange("q", e.target.value)}
              InputProps={{ style: { color: "white" } }}
              InputLabelProps={{ style: { color: "gray" } }}
            />
          </Grid>

          <Grid item xs={12} sm={2}>
            <Button
              variant="contained"
              fullWidth
              onClick={applyFilters}
              sx={{ height: "56px" }}
            >
              Apply
            </Button>
          </Grid>

          <Grid item xs={12} sm={2}>
            <Button
              variant="outlined"
              fullWidth
              onClick={exportCsv}
              sx={{ height: "56px", color: "white", borderColor: "gray" }}
            >
              Export CSV
            </Button>
          </Grid>
        </Grid>
      </Paper>

      {/* Table */}
      <Paper sx={{ height: 600, background: "#121212", color: "#fff" }}>
        <DataGrid
          rows={rows}
          columns={columns}
          pageSizeOptions={[20]}
          pagination
          paginationModel={{ page, pageSize }}
          onPaginationModelChange={(m) => setPage(m.page)}
          rowCount={totalRows}
          paginationMode="server"
          sx={{ color: "white", border: "none" }}
        />
      </Paper>

      {/* Modal */}
      <Dialog open={modalOpen} onClose={() => setModalOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>AI Review</DialogTitle>
        <DialogContent>
          <Typography
            component="pre"
            sx={{
              whiteSpace: "pre-wrap",
              background: "#111",
              p: 2,
              borderRadius: 2,
              color: "#ddd",
            }}
          >
            {selectedReview?.aiReview}
          </Typography>
        </DialogContent>
      </Dialog>
    </Box>
  );
}
