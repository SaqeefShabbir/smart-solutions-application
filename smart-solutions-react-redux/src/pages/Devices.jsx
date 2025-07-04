import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  Container,
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  TextField,
  Button,
  IconButton,
  MenuItem,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Tooltip,
  CircularProgress,
  Alert
} from '@mui/material';
import {
  Add,
  Edit,
  Delete,
  Refresh,
  CheckCircle,
  Warning,
  Error,
  Settings,
  FilterList,
  Clear
} from '@mui/icons-material';
import { fetchDevices, addDevice, updateDevice, deleteDevice } from '../features/devices/deviceSlice';

const Devices = () => {
  const dispatch = useDispatch();
  const { devices, loading, error } = useSelector((state) => state.devices);
  
  // State for filters
  const [filters, setFilters] = useState({
    status: '',
    type: '',
    location: '',
    search: ''
  });
  
  // State for add/edit dialog
  const [openDialog, setOpenDialog] = useState(false);
  const [currentDevice, setCurrentDevice] = useState(null);
  const [deviceForm, setDeviceForm] = useState({
    name: '',
    type: 'Temperature Sensor',
    location: 'Headquarters',
    status: 'Active'
  });

  // Fetch devices on component mount
  useEffect(() => {
    dispatch(fetchDevices());
  }, [dispatch]);

  // Handle filter change
  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters({
      ...filters,
      [name]: value
    });
  };

  // Clear all filters
  const handleClearFilters = () => {
    setFilters({
      status: '',
      type: '',
      location: '',
      search: ''
    });
  };

  // Open dialog for adding new device
  const handleOpenAddDialog = () => {
    setCurrentDevice(null);
    setDeviceForm({
      name: '',
      type: 'Temperature Sensor',
      location: 'Headquarters',
      status: 'Active'
    });
    setOpenDialog(true);
  };

  // Open dialog for editing device
  const handleOpenEditDialog = (device) => {
    setCurrentDevice(device);
    setDeviceForm({
      name: device.name,
      type: device.type,
      location: device.location,
      status: device.status
    });
    setOpenDialog(true);
  };

  // Close dialog
  const handleCloseDialog = () => {
    setOpenDialog(false);
  };

  // Handle form input changes
  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setDeviceForm({
      ...deviceForm,
      [name]: value
    });
  };

  // Handle form submission
  const handleSubmit = () => {
    if (currentDevice) {
      dispatch(updateDevice({ id: currentDevice.id, ...deviceForm }));
    } else {
      dispatch(addDevice(deviceForm));
    }
    handleCloseDialog();
  };

  // Handle device deletion
  const handleDelete = (id) => {
    if (window.confirm('Are you sure you want to delete this device?')) {
      dispatch(deleteDevice(id));
    }
  };

  // Filter devices based on filter state
  const filteredDevices = devices.filter(device => {
    if (filters.status && device.status !== filters.status) return false;
    if (filters.type && device.type !== filters.type) return false;
    if (filters.location && device.location !== filters.location) return false;
    if (filters.search && 
        !device.name.toLowerCase().includes(filters.search.toLowerCase()) && 
        !device.location.toLowerCase().includes(filters.search.toLowerCase())) {
      return false;
    }
    return true;
  });

  // Get status chip component
  const getStatusChip = (status) => {
    switch (status) {
      case 'Active':
        return <Chip icon={<CheckCircle />} label="Active" color="success" size="small" />;
      case 'warning':
        return <Chip icon={<Warning />} label="Warning" color="warning" size="small" />;
      case 'error':
        return <Chip icon={<Error />} label="Error" color="error" size="small" />;
      case 'maintenance':
        return <Chip icon={<Settings />} label="Maintenance" color="info" size="small" />;
      default:
        return <Chip label={status} size="small" />;
    }
  };

  if (loading && devices.length === 0) {
    return (
      <Container maxWidth="lg" sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress size={60} />
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 2 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">Device Management</Typography>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={handleOpenAddDialog}
        >
          Add Device
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Filters Card */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2} alignItems="center">
            <Grid item>
              <FilterList color="action" />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <TextField
                select
                fullWidth
                size="small"
                label="Status"
                name="status"
                value={filters.status}
                onChange={handleFilterChange}
              >
                <MenuItem value="">All Statuses</MenuItem>
                <MenuItem value="Active">Active</MenuItem>
                <MenuItem value="Inactive">Inactive</MenuItem>
                <MenuItem value="Retired">Retired</MenuItem>
                <MenuItem value="Maintenance">Maintenance</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <TextField
                select
                fullWidth
                size="small"
                label="Type"
                name="type"
                value={filters.type}
                onChange={handleFilterChange}
              >
                <MenuItem value="">All Types</MenuItem>
                <MenuItem value="Temperature Sensor">Temperature Sensor</MenuItem>
                <MenuItem value="Humidity Sensor">Humidity Sensor</MenuItem>
                <MenuItem value="Smart Actuator">Smart Actuator</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <TextField
                select
                fullWidth
                size="small"
                label="Location"
                name="location"
                value={filters.location}
                onChange={handleFilterChange}
              >
                <MenuItem value="">All Locations</MenuItem>
                <MenuItem value="Headquarters">Headquarters</MenuItem>
                <MenuItem value="Warehouse A">Warehouse A</MenuItem>
                <MenuItem value="Office Building">Office Building</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <TextField
                fullWidth
                size="small"
                label="Search"
                name="search"
                value={filters.search}
                onChange={handleFilterChange}
                placeholder="Search by name or location"
              />
            </Grid>
            <Grid item xs={12} sm={6} md={2}>
              <Button
                fullWidth
                size="small"
                startIcon={<Clear />}
                onClick={handleClearFilters}
              >
                Clear Filters
              </Button>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Devices Table */}
      <Card>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
            <Tooltip title="Refresh devices">
              <IconButton onClick={() => dispatch(fetchDevices())}>
                <Refresh />
              </IconButton>
            </Tooltip>
          </Box>
          
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Device Name</TableCell>
                  <TableCell>Type</TableCell>
                  <TableCell>Location</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Last Seen</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredDevices.length > 0 ? (
                  filteredDevices.map((device) => (
                    <TableRow key={device.id}>
                      <TableCell>{device.name}</TableCell>
                      <TableCell>
                        <Chip 
                          label={device.type} 
                          color={device.type === 'sensor' ? 'primary' : 'secondary'} 
                          size="small" 
                        />
                      </TableCell>
                      <TableCell>{device.location}</TableCell>
                      <TableCell>
                        {getStatusChip(device.status)}
                      </TableCell>
                      <TableCell>
                        {device.lastSeen ? new Date(device.lastSeen).toLocaleString() : 'Never'}
                      </TableCell>
                      <TableCell>
                        <Tooltip title="Edit device">
                          <IconButton onClick={() => handleOpenEditDialog(device)}>
                            <Edit color="primary" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Delete device">
                          <IconButton onClick={() => handleDelete(device.id)}>
                            <Delete color="error" />
                          </IconButton>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  ))
                ) : (
                  <TableRow>
                    <TableCell colSpan={6} align="center">
                      {devices.length === 0 ? 'No devices found' : 'No devices match your filters'}
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>

      {/* Add/Edit Device Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog}>
        <DialogTitle>{currentDevice ? 'Edit Device' : 'Add New Device'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Device Name"
                name="name"
                value={deviceForm.name}
                onChange={handleFormChange}
                required
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                select
                fullWidth
                label="Device Type"
                name="type"
                value={deviceForm.type}
                onChange={handleFormChange}
                required
              >
                <MenuItem value="Temperature Sensor">Temperature Sensor</MenuItem>
                <MenuItem value="Humidity Sensor">Humidity Sensor</MenuItem>
                <MenuItem value="Smart Actuator">Smart Actuator</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                select
                fullWidth
                label="Status"
                name="status"
                value={deviceForm.status}
                onChange={handleFormChange}
                required
              >
                <MenuItem value="Active">Active</MenuItem>
                <MenuItem value="Inactive">Inactive</MenuItem>
                <MenuItem value="Retired">Retired</MenuItem>
                <MenuItem value="Maintenance">Maintenance</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                select
                fullWidth
                label="Location"
                name="location"
                value={deviceForm.location}
                onChange={handleFormChange}
                required
              >
                <MenuItem value="Headquarters">Headquarters</MenuItem>
                <MenuItem value="Warehouse A">Warehouse A</MenuItem>
                <MenuItem value="Office Building">Office Building</MenuItem>
              </TextField>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button 
            onClick={handleSubmit} 
            variant="contained"
            disabled={!deviceForm.name}
          >
            {currentDevice ? 'Update' : 'Add'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default Devices;