import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { 
  Container, 
  Typography, 
  Grid, 
  Card, 
  CardContent,
  Box,
  Table, 
  TableBody, 
  TableCell, 
  TableContainer, 
  TableHead, 
  TableRow, 
  Paper,
  IconButton,
  Button,
  Chip,
  Badge,
  TextField,
  MenuItem,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Tooltip
} from '@mui/material';
import { 
  Refresh, 
  CheckCircle, 
  Warning, 
  Error, 
  Notifications, 
  FilterList,
  Clear,
  ArrowForward,
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers';
import { LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { fetchAlerts, acknowledgeAlert, selectDateRange, setDateRange } from '../features/alerts/alertSlice';
import { selectAllDevices, fetchDevices, selectDeviceLoadingStatus } from '../features/devices/deviceSlice';
import moment from 'moment';

const Alerts = () => {
  const dispatch = useDispatch();
  const { alerts, loading, error } = useSelector((state) => state.alerts);
  const devices = useSelector((state) => selectAllDevices(state));
  const deviceLoadingStatus = useSelector(selectDeviceLoadingStatus);
  const { start, end } = useSelector(selectDateRange);

  const now = moment();
  const [localDateRange, setLocalDateRange] = useState({
    start: new Date(start),
    end: new Date(end)
  });
  const [filters, setFilters] = useState({
    severity: '',
    status: 'Unacknowledged',
    deviceId: '',
    dateRange: '24h', 
    startDate: localDateRange.start, 
    endDate: localDateRange.end     
  });

  const [selectedAlert, setSelectedAlert] = useState(null);
  const [detailsOpen, setDetailsOpen] = useState(false);
  useEffect(() => {
    dispatch(fetchDevices());
    dispatch(fetchAlerts(filters));

    dispatch(setDateRange({
      start: localDateRange.start.toISOString(),
      end: localDateRange.end.toISOString()
    }));
  }, [dispatch, filters, localDateRange.start, localDateRange.end]);

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    
    if (name === 'custom') {
      setFilters({
        ...filters,
        dateRange: value,
        'startDate': localDateRange.start,
        'endDate': localDateRange.end
      });
    } 
    else if (name === 'dateRange') {
      const { startDate, endDate } = calculateDateRange(value);
      setFilters({
        ...filters,
        dateRange: value,
        startDate,
        endDate
      });
    } 
    else {
      setFilters({
        ...filters,
        [name]: value
      });
    }
  };

  const handleDateChange = (name, date) => {
    setLocalDateRange(prev => ({
      ...prev,
      [name]: date
    }));
  };

  const handleClearFilters = () => {
    setFilters({
      severity: '',
      status: 'Unacknowledged',
      deviceId: '',
      dateRange: '24h',
      startDate: localDateRange.start,  
      endDate: localDateRange.end     
    });
  };

  const handleAcknowledge = (alertId) => {
    dispatch(acknowledgeAlert(alertId));
  };

  const handleOpenDetails = (alert) => {
    setSelectedAlert(alert);
    setDetailsOpen(true);
  };

  const handleCloseDetails = () => {
    setDetailsOpen(false);
  };

  const calculateDateRange = (range) => {
    let startDate = null;
    let endDate = now.toISOString();

    switch (range) {
      case '1h':
        startDate = now.subtract(1, 'hours').toISOString();
        break;
      case '24h':
        startDate = now.subtract(24, 'hours').toISOString();
        break;
      case '7d':
        startDate = now.subtract(7, 'days').toISOString();
        break;
      case '30d':
        startDate = now.subtract(30, 'days').toISOString();
        break;
      case 'all':
        startDate = null;
        break;
      default:
        startDate = now.subtract(24, 'hours').toISOString();
    }

    return { startDate, endDate };
  };

  const getSeverityChip = (severity) => {
    switch (severity) {
      case 'Critical':
        return <Chip icon={<Error />} label="Critical" color="error" size="small" />;
      case 'High':
        return <Chip icon={<Warning />} label="High" color="warning" size="small" />;
      case 'Medium':
        return <Chip icon={<Notifications />} label="Medium" color="info" size="small" />;
      case 'Low':
        return <Chip icon={<CheckCircle />} label="Low" color="success" size="small" />;
      default:
        return <Chip label={severity} size="small" />;
    }
  };

  const getStatusBadge = (acknowledged) => {
    return (
      <Badge
        color={acknowledged ? "success" : "error"}
        variant="dot"
        sx={{ mr: 1 }}
      >
        {acknowledged ? "Acknowledged" : "Unacknowledged"}
      </Badge>
    );
  };

  const filteredAlerts = alerts.filter(alert => {
    if (filters.severity && alert.severity !== filters.severity) return false;
    if (filters.status === 'Acknowledged' && !alert.acknowledged) return false;
    if (filters.status === 'Unacknowledged' && alert.acknowledged) return false;
    if (filters.deviceId && alert.deviceId !== filters.deviceId) return false;
    return true;
  });

  if (loading|| deviceLoadingStatus === true) {
    return (
      <Container maxWidth="lg" style={{ display: 'flex', justifyContent: 'center', padding: '20px' }}>
        <CircularProgress />
      </Container>
    );
  }

  if (error) {
    return (
      <Container maxWidth="lg">
        <Typography color="error">Error loading alerts: {error}</Typography>
        <Button 
          variant="contained" 
          color="primary" 
          startIcon={<Refresh />}
          onClick={() => dispatch(fetchAlerts(filters))}
        >
          Retry
        </Button>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Grid container spacing={3}>
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Grid container justifyContent="space-between" alignItems="center">
                <Grid item>
                  <Typography variant="h5">Alerts Management</Typography>
                </Grid>
                <Grid item>
                  <Button 
                    variant="outlined" 
                    color="primary" 
                    startIcon={<Refresh />}
                    onClick={() => dispatch(fetchAlerts(filters))}
                  >
                    Refresh
                  </Button>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Grid container spacing={2} alignItems="center" sx={{ mb: 2 }}>
                <Grid item>
                  <FilterList color="action" />
                </Grid>
                <Grid item xs={12} sm={6} md={2}>
                  <TextField
                    select
                    fullWidth
                    size="small"
                    label="Severity"
                    name="severity"
                    value={filters.severity}
                    onChange={handleFilterChange}
                  >
                    <MenuItem value="">All</MenuItem>
                    <MenuItem value="Critical">Critical</MenuItem>
                    <MenuItem value="High">High</MenuItem>
                    <MenuItem value="Medium">Medium</MenuItem>
                    <MenuItem value="Low">Low</MenuItem>
                  </TextField>
                </Grid>
                <Grid item xs={12} sm={6} md={2}>
                  <TextField
                    select
                    fullWidth
                    size="small"
                    label="Status"
                    name="status"
                    value={filters.status}
                    onChange={handleFilterChange}
                  >
                    <MenuItem value="">All</MenuItem>
                    <MenuItem value="Acknowledged">Acknowledged</MenuItem>
                    <MenuItem value="Unacknowledged">Unacknowledged</MenuItem>
                  </TextField>
                </Grid>
                {/* Device Filter */}
                <Grid item xs={12} sm={6} md={3}>
                  <TextField
                    select
                    fullWidth
                    size="small"
                    label="Device"
                    name="deviceId"
                    value={filters.deviceId}
                    onChange={handleFilterChange}
                    disabled={deviceLoadingStatus === true}
                  >
                    <MenuItem value="">All Devices</MenuItem>
                    {deviceLoadingStatus === true ? (
                      <MenuItem disabled>
                        <CircularProgress size={20} sx={{ mr: 1 }} />
                        Loading devices...
                      </MenuItem>
                    ) : (
                      devices.map(device => (
                        <MenuItem key={device.id} value={device.id}>
                          <Box sx={{ display: 'flex', alignItems: 'center' }}>
                            {device.name} 
                            <Typography variant="caption" sx={{ ml: 1 }}>
                              ({device.type})
                            </Typography>
                          </Box>
                        </MenuItem>
                      ))
                    )}
                  </TextField>
                </Grid>
                <Grid item xs={12} sm={6} md={2}>
                  <TextField
                    select
                    fullWidth
                    size="small"
                    label="Time Range"
                    name="dateRange"
                    value={filters.dateRange}
                    onChange={handleFilterChange}
                  >
                    <MenuItem value="1h">Last 1 hour</MenuItem>
                    <MenuItem value="24h">Last 24 hours</MenuItem>
                    <MenuItem value="7d">Last 7 days</MenuItem>
                    <MenuItem value="30d">Last 30 days</MenuItem>
                    <MenuItem value="all">All time</MenuItem>
                    <MenuItem value="custom">Custom range</MenuItem>
                  </TextField>
                </Grid>
                {/* Add custom date pickers when 'custom' is selected */}
                {filters.dateRange === 'custom' && (
                  <>
                    <Grid item xs={12} sm={6}>
                      <LocalizationProvider dateAdapter={AdapterDateFns}>
                        <DatePicker
                          label="Start Date"
                          value={localDateRange.start}
                          onChange={(date) => { 
                            handleDateChange('start', date); 
                            setFilters({
                            ...filters,
                            'startDate': date
                          });}}
                          renderInput={(params) => <TextField {...params} fullWidth />}
                          maxDate={localDateRange.end}
                        />
                      </LocalizationProvider>
                    </Grid>

                    <Grid item xs={12} sm={6}>
                      <LocalizationProvider dateAdapter={AdapterDateFns}>
                        <DatePicker
                          label="End Date"
                          value={localDateRange.end}
                          onChange={(date) => { 
                            handleDateChange('end', date); 
                            setFilters({
                            ...filters,
                            'endDate': date
                          });}}
                          renderInput={(params) => <TextField {...params} fullWidth />}
                          maxDate={new Date()}
                        />
                      </LocalizationProvider>
                      </Grid>
                  </>
                )}
                <Grid item>
                  <Button 
                    startIcon={<Clear />}
                    onClick={handleClearFilters}
                  >
                    Clear
                  </Button>
                </Grid>
              </Grid>

              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Alert ID</TableCell>
                      <TableCell>Severity</TableCell>
                      <TableCell>Device</TableCell>
                      <TableCell>Message</TableCell>
                      <TableCell>Timestamp</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell>Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {filteredAlerts.map((alert) => (
                      <TableRow 
                        key={alert.id}
                        sx={{ 
                          '&:hover': { backgroundColor: 'action.hover' },
                          cursor: 'pointer'
                        }}
                        onClick={() => handleOpenDetails(alert)}
                      >
                        <TableCell>{alert.id}</TableCell>
                        <TableCell>
                          {getSeverityChip(alert.severity)}
                        </TableCell>
                        <TableCell>
                          <Box sx={{ display: 'flex', alignItems: 'center' }}>
                            {devices.find(d => d.id === alert.deviceId)?.name || 'Unknown Device'}
                            {alert.deviceId && !devices.some(d => d.id === alert.deviceId) && (
                              <Tooltip title="Device not found in current inventory">
                                <Error color="error" fontSize="small" sx={{ ml: 1 }} />
                              </Tooltip>
                            )}
                          </Box>
                        </TableCell>
                        <TableCell>
                          <Typography 
                            sx={{ 
                              display: '-webkit-box',
                              WebkitLineClamp: 1,
                              WebkitBoxOrient: 'vertical',
                              overflow: 'hidden'
                            }}
                          >
                            {alert.message}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          {moment(alert.createdAt).fromNow()}
                        </TableCell>
                        <TableCell>
                          {getStatusBadge(alert.acknowledged)}
                        </TableCell>
                        <TableCell>
                          {!alert.acknowledged && (
                            <Tooltip title="Acknowledge">
                              <IconButton 
                                color="primary"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleAcknowledge(alert.id);
                                }}
                              >
                                <CheckCircle />
                              </IconButton>
                            </Tooltip>
                          )}
                          <Tooltip title="Details">
                            <IconButton
                              onClick={(e) => {
                                e.stopPropagation();
                                handleOpenDetails(alert);
                              }}
                            >
                              <ArrowForward />
                            </IconButton>
                          </Tooltip>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Alert Details Dialog */}
      <Dialog 
        open={detailsOpen} 
        onClose={handleCloseDetails}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          Alert Details
          {selectedAlert && (
            <Chip 
              label={selectedAlert.severity.toUpperCase()} 
              color={
                selectedAlert.severity === 'Critical' ? 'error' : 
                selectedAlert.severity === 'High' ? 'warning' : 'info'
              } 
              sx={{ ml: 2 }}
            />
          )}
        </DialogTitle>
        <DialogContent dividers>
          {selectedAlert && (
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <Typography variant="subtitle1" gutterBottom>Device Information</Typography>
                <Typography>
                  <strong>Device:</strong> {devices.find(d => d.id === selectedAlert.deviceId)?.name || 'Unknown'}
                </Typography>
                <Typography>
                  <strong>Type:</strong> {devices.find(d => d.id === selectedAlert.deviceId)?.type || 'Unknown'}
                </Typography>
                <Typography>
                  <strong>Location:</strong> {devices.find(d => d.id === selectedAlert.deviceId)?.location || 'Unknown'}
                </Typography>
              </Grid>
              <Grid item xs={12} md={6}>
                <Typography variant="subtitle1" gutterBottom>Alert Information</Typography>
                <Typography>
                  <strong>Timestamp:</strong> {moment(selectedAlert.timestamp).format('LLL')}
                </Typography>
                <Typography>
                  <strong>Status:</strong> {selectedAlert.acknowledged ? 'acknowledged' : 'Unacknowledged'}
                </Typography>
                {selectedAlert.acknowledgedBy && (
                  <Typography>
                    <strong>Acknowledged by:</strong> {selectedAlert.acknowledgedBy}
                  </Typography>
                )}
              </Grid>
              <Grid item xs={12}>
                <Typography variant="subtitle1" gutterBottom>Message</Typography>
                <Card variant="outlined">
                  <CardContent>
                    <Typography>{selectedAlert.message}</Typography>
                  </CardContent>
                </Card>
              </Grid>
              {selectedAlert.additionalData && (
                <Grid item xs={12}>
                  <Typography variant="subtitle1" gutterBottom>Additional Data</Typography>
                  <Card variant="outlined">
                    <CardContent>
                      <pre style={{ margin: 0, whiteSpace: 'pre-wrap' }}>
                        {JSON.stringify(selectedAlert.additionalData, null, 2)}
                      </pre>
                    </CardContent>
                  </Card>
                </Grid>
              )}
            </Grid>
          )}
        </DialogContent>
        <DialogActions>
          {selectedAlert && !selectedAlert.acknowledged && (
            <Button
              startIcon={<CheckCircle />}
              variant="contained"
              color="primary"
              onClick={() => {
                handleAcknowledge(selectedAlert.id);
                handleCloseDetails();
              }}
            >
              Acknowledge
            </Button>
          )}
          <Button onClick={handleCloseDetails}>Close</Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default Alerts;