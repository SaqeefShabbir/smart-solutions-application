import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  CardHeader,
  TextField,
  MenuItem,
  Button,
  CircularProgress,
  Alert,
  IconButton,
  Paper,
} from '@mui/material';
import {
  Refresh,
  ArrowBack,
  ShowChart,
  Sensors,
  CalendarToday
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers';
import { LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { Line } from 'react-chartjs-2';
import 'chart.js/auto';
import {
  fetchSensorData,
  fetchDeviceSensors,
  setCurrentDevice,
  setCurrentSensorType,
  setDateRange,
  selectCurrentSensorData,
  selectDeviceSensors,
  selectLoadingStatus,
  selectDateRange,
  selectLoadingDeviceSensorsStatus
} from '../features/sensors/sensorSlice';
import { selectAllDevices, fetchDevices, selectDeviceLoadingStatus } from '../features/devices/deviceSlice';
import { format, subDays } from 'date-fns';

const SensorData = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { deviceId = "1" } = useParams();

  // Redux state
  const [selectedDevice, setSelectedDevice] = useState(deviceId);

  const devices = useSelector((state) => selectAllDevices(state));
  const deviceLoadingStatus = useSelector(selectDeviceLoadingStatus);
  const sensorData = useSelector(selectCurrentSensorData);
  const sensors = useSelector((state) => selectDeviceSensors(selectedDevice)(state));
  const loading = useSelector(selectLoadingStatus);
  const loadingDeviceSensors = useSelector(selectLoadingDeviceSensorsStatus);
  const { start, end } = useSelector(selectDateRange);
  const error = useSelector((state) => state.sensor.error);

  // Local state
  const [selectedSensor, setSelectedSensor] = useState('');
  const [localDateRange, setLocalDateRange] = useState({
    start: new Date(start),
    end: new Date(end)
  });

  // Initialize device and sensors (runs once when deviceId changes)
  useEffect(() => {
    dispatch(fetchDevices());
    dispatch(setCurrentDevice(selectedDevice));
    dispatch(fetchDeviceSensors(selectedDevice));

    dispatch(setDateRange({
      start: localDateRange.start.toISOString(),
      end: localDateRange.end.toISOString()
    }));
  }, [selectedDevice, localDateRange.start, localDateRange.end, dispatch]);

  // Set initial sensor (only when sensors array changes)
  useEffect(() => {
    if (sensors.length > 0 && !selectedSensor) {
      setSelectedSensor(sensors[0]);
    }
  }, [sensors, selectedSensor]); // Removed other dependencies

  // Fetch data when dependencies change
  useEffect(() => {
    if (selectedSensor && selectedDevice) {
      dispatch(setCurrentSensorType(selectedSensor));
      const startDate = localDateRange.start.toISOString();
      const endDate = localDateRange.end.toISOString();
      
      dispatch(fetchSensorData({ 
        deviceId: selectedDevice, 
        sensorType: selectedSensor,
        startDate, 
        endDate 
      }));
    }
  }, [selectedSensor, selectedDevice, localDateRange.start, localDateRange.end, dispatch]);

  const handleRefresh = () => {
    dispatch(fetchSensorData({ 
      deviceId: selectedDevice, 
      startDate: localDateRange.start.toISOString(),
      endDate: localDateRange.end.toISOString()
    }));
  };

  const handleDateChange = (name, date) => {
    setLocalDateRange(prev => ({
      ...prev,
      [name]: date
    }));
  };

  const handleApplyDateRange = () => {
    dispatch(setDateRange({
      start: localDateRange.start.toISOString(),
      end: localDateRange.end.toISOString()
    }));
  };

  const handleQuickDateRange = (days) => {
    let newRange = {
      start: subDays(new Date(), days),
      end: new Date()
    };
    setLocalDateRange(newRange);

    newRange = {
      start: subDays(new Date(), days).toISOString(),
      end: new Date().toISOString()
    };

    dispatch(setDateRange(newRange));
  };

  // Prepare chart data
  const chartData = {
    labels: sensorData.map(data => format(new Date(data.timestamp), 'MMM dd HH:mm')),
    datasets: [
      {
        label: `${selectedSensor} Readings`,
        data: sensorData.map(data => data.value),
        borderColor: 'rgb(75, 192, 192)',
        backgroundColor: 'rgba(75, 192, 192, 0.2)',
        tension: 0.1,
        fill: true
      }
    ]
  };

  const chartOptions = {
    responsive: true,
    plugins: {
      legend: {
        position: 'top',
      },
      tooltip: {
        callbacks: {
          label: (context) => {
            return `${selectedSensor}: ${context.parsed.y}`;
          }
        }
      }
    },
    scales: {
      x: {
        title: {
          display: true,
          text: 'Timestamp'
        }
      },
      y: {
        title: {
          display: true,
          text: 'Value'
        }
      }
    }
  };

  if (loading && !sensorData.length) {
    return (
      <Container maxWidth="lg" sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress size={60} />
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 2 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
        <IconButton onClick={() => navigate('/devices')} sx={{ mr: 1 }}>
          <ArrowBack />
        </IconButton>
        <Typography variant="h4" component="h1">
          Sensor Data
        </Typography>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <Grid container spacing={3}>
        {/* Controls Card */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardHeader title="Data Controls" />
            <CardContent>
              <Grid container spacing={2}>
                {/* Device Filter */}
                <Grid item xs={12} sm={6} md={3}>
                  <TextField
                    select
                    fullWidth
                    label="Device"
                    value={selectedDevice}
                    onChange={(e) => setSelectedDevice(e.target.value)}
                    disabled={deviceLoadingStatus === true || devices.length === 0}
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
                <Grid item xs={12}>
                  <TextField
                    select
                    fullWidth
                    label="Sensor Type"
                    value={selectedSensor}
                    onChange={(e) => setSelectedSensor(e.target.value)}
                    disabled={loadingDeviceSensors || sensors.length === 0}
                  >
                    {loadingDeviceSensors ? (
                      <MenuItem disabled value="">
                        <Box sx={{ display: 'flex', alignItems: 'center' }}>
                          <CircularProgress size={20} sx={{ mr: 2 }} />
                          Loading sensors...
                        </Box>
                      </MenuItem>
                    ) : sensors.length > 0 ? (
                      sensors.map((sensorType) => (
                        <MenuItem key={sensorType} value={sensorType}>
                          <Box sx={{ display: 'flex', alignItems: 'center' }}>
                            <Sensors sx={{ mr: 1 }} />
                            {sensorType}
                          </Box>
                        </MenuItem>
                      ))
                    ) : (
                      <MenuItem disabled value="">
                        No sensors available
                      </MenuItem>
                    )}
                  </TextField>
                </Grid>

                <Grid item xs={12}>
                  <Typography variant="subtitle2" gutterBottom>
                    Quick Date Range
                  </Typography>
                  <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                    <Button variant="outlined" onClick={() => handleQuickDateRange(1)}>
                      24h
                    </Button>
                    <Button variant="outlined" onClick={() => handleQuickDateRange(7)}>
                      7d
                    </Button>
                    <Button variant="outlined" onClick={() => handleQuickDateRange(30)}>
                      30d
                    </Button>
                  </Box>
                </Grid>

                <Grid item xs={12} sm={6}>
                  <LocalizationProvider dateAdapter={AdapterDateFns}>
                    <DatePicker
                      label="Start Date"
                      value={localDateRange.start}
                      onChange={(date) => handleDateChange('start', date)}
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
                      onChange={(date) => handleDateChange('end', date)}
                      renderInput={(params) => <TextField {...params} fullWidth />}
                      maxDate={new Date()}
                    />
                  </LocalizationProvider>
                </Grid>

                <Grid item xs={12}>
                  <Button
                    variant="contained"
                    fullWidth
                    onClick={handleApplyDateRange}
                    startIcon={<CalendarToday />}
                  >
                    Apply Date Range
                  </Button>
                </Grid>

                <Grid item xs={12}>
                  <Button
                    variant="outlined"
                    fullWidth
                    onClick={handleRefresh}
                    startIcon={<Refresh />}
                    disabled={loading}
                  >
                    Refresh Data
                  </Button>
                </Grid>
              </Grid>
            </CardContent>
          </Card>

          {/* Always render stats card, show empty state if no data */}
          <Card sx={{ mt: 3 }}>
            <CardHeader title="Statistics" />
            <CardContent>
              {sensorData.length > 0 ? (
                <Grid container spacing={2}>
                  <Grid item xs={6}>
                    <Typography variant="subtitle2" gutterBottom>Latest Value</Typography>
                    <Typography variant="h5" color="primary">
                      {sensorData[0].value} {sensorData[0].unit || ''}
                    </Typography>
                  </Grid>
                  <Grid item xs={6}>
                    <Typography variant="subtitle2" gutterBottom>Timestamp</Typography>
                    <Typography variant="body2">
                      {format(new Date(sensorData[0].timestamp), 'PPpp')}
                    </Typography>
                  </Grid>
                  <Grid item xs={6}>
                    <Typography variant="subtitle2" gutterBottom>Readings Count</Typography>
                    <Typography variant="h5">
                      {sensorData.length}
                    </Typography>
                  </Grid>
                  <Grid item xs={6}>
                    <Typography variant="subtitle2" gutterBottom>Time Range</Typography>
                    <Typography variant="body2">
                      {format(new Date(start), 'PP')} – {format(new Date(end), 'PP')}
                    </Typography>
                  </Grid>
                </Grid>
              ) : (
                <Box sx={{ 
                  display: 'flex', 
                  flexDirection: 'column', 
                  alignItems: 'center',
                  py: 2,
                  textAlign: 'center'
                }}>
                  <Typography variant="body1" color="text.secondary">
                    No statistics available
                  </Typography>
                  <Typography variant="caption">
                    Select a sensor and date range
                  </Typography>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Chart Card */}
        <Grid item xs={12} md={8}>
          <Card>
            <CardHeader 
              title={`${selectedSensor || 'Sensor'} Data`} 
              subheader="Historical readings visualization"
              action={
                <IconButton onClick={handleRefresh} disabled={loading}>
                  <Refresh />
                </IconButton>
              }
            />
            <CardContent>
              {selectedSensor ? (
                <>
                  {loading && sensorData.length > 0 ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
                      <CircularProgress />
                    </Box>
                  ) : (
                    <Box sx={{ height: '400px' }}>
                      <Line data={chartData} options={chartOptions} />
                    </Box>
                  )}

                  {!loading && sensorData.length === 0 && (
                    <Box sx={{ 
                      display: 'flex', 
                      flexDirection: 'column', 
                      alignItems: 'center', 
                      py: 4,
                      textAlign: 'center'
                    }}>
                      <ShowChart sx={{ fontSize: 60, color: 'text.secondary', mb: 2 }} />
                      <Typography variant="h6" color="text.secondary">
                        No data available for the selected range
                      </Typography>
                      <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                        Try adjusting the date range or selecting a different sensor
                      </Typography>
                    </Box>
                  )}
                </>
              ) : (
                <Box sx={{ 
                  display: 'flex', 
                  flexDirection: 'column', 
                  alignItems: 'center', 
                  py: 4,
                  textAlign: 'center'
                }}>
                  <Sensors sx={{ fontSize: 60, color: 'text.secondary', mb: 2 }} />
                  <Typography variant="h6" color="text.secondary">
                    Select a sensor type to view data
                  </Typography>
                </Box>
              )}
            </CardContent>
          </Card>

          {/* Data Table Card */}
          {sensorData.length > 0 && (
            <Card sx={{ mt: 3 }}>
              <CardHeader title="Raw Data" />
              <CardContent>
                <Paper sx={{ width: '100%', overflow: 'hidden' }}>
                  <Box sx={{ maxHeight: 300, overflow: 'auto' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                      <thead>
                        <tr style={{ backgroundColor: '#f5f5f5' }}>
                          <th style={{ padding: '12px', textAlign: 'left' }}>Timestamp</th>
                          <th style={{ padding: '12px', textAlign: 'left' }}>Value</th>
                          <th style={{ padding: '12px', textAlign: 'left' }}>Unit</th>
                        </tr>
                      </thead>
                      <tbody>
                        {sensorData.map((data, index) => (
                          <tr 
                            key={index} 
                            style={{ 
                              borderBottom: '1px solid #e0e0e0',
                              backgroundColor: index % 2 === 0 ? '#fff' : '#fafafa'
                            }}
                          >
                            <td style={{ padding: '12px' }}>
                              {format(new Date(data.timestamp), 'PPpp')}
                            </td>
                            <td style={{ padding: '12px' }}>{data.value}</td>
                            <td style={{ padding: '12px' }}>{data.unit || 'N/A'}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </Box>
                </Paper>
              </CardContent>
            </Card>
          )}
        </Grid>
      </Grid>
    </Container>
  );
};

export default SensorData;