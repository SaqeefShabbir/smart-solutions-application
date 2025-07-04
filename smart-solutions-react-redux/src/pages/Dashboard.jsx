import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  Container,
  Button,
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  CircularProgress,
  Alert,
  Divider
} from '@mui/material';
import {
  DeviceThermostat,
  Opacity,
  Warning,
  Devices,
  Refresh,
  CheckCircle,
  Error,
  Settings
} from '@mui/icons-material';
import { Bar } from 'react-chartjs-2';
import 'chart.js/auto';
import { checkAuthState } from '../features/auth/authSlice';
import {
  fetchLatestReadings,
  selectLatestReadings,
  selectLatestLoadingStatus
} from '../features/sensors/sensorSlice';
import {
  fetchDevices,
  selectAllDevices,
  selectDeviceLoadingStatus
} from '../features/devices/deviceSlice';
import {
  fetchAlerts,
  selectUnreadAlerts,
  selectAlertsLoadingStatus
} from '../features/alerts/alertSlice';

const Dashboard = () => {
  const dispatch = useDispatch();
  
  // Redux state
  const { userId } = useSelector((state) => state.auth);
  const latestReadings = useSelector(selectLatestReadings);
  const devices = useSelector(selectAllDevices);
  const unreadAlerts = useSelector(selectUnreadAlerts);
  const sensorLoading = useSelector(selectLatestLoadingStatus);
  const deviceLoading = useSelector(selectDeviceLoadingStatus);
  const alertsLoading = useSelector(selectAlertsLoadingStatus);
  const error = useSelector((state) => state.sensor.errorLatest || state.devices.error || state.alerts.error);

  useEffect(() => {
    // Optional: Only if you need periodic checks
    const interval = setInterval(() => {
      dispatch(checkAuthState());
    }, 300000); // Every 5 minutes

    return () => clearInterval(interval);
  }, [dispatch]);

  // Fetch data on component mount
  useEffect(() => {
    dispatch(fetchLatestReadings());
    dispatch(fetchDevices());
    dispatch(fetchAlerts({ status: 'Unacknowledged' }));

    // Set up polling for real-time updates (every 30 seconds)
    const interval = setInterval(() => {
      dispatch(fetchLatestReadings());
      dispatch(fetchAlerts({ status: 'Unacknowledged' }));
    }, 300000); // Every 5 minutes

    return () => clearInterval(interval);
  }, [dispatch, userId]);

  //For Debugging
  // useEffect(() => {
  //   console.log('Latest readings from Redux:', latestReadings);
  //   console.log('Filtered temperature readings:', 
  //     latestReadings?.filter(reading => reading?.sensorType === 'Temperature'));
  // }, [latestReadings]);

  // Handle refresh button click
  const handleRefresh = () => {
    dispatch(fetchLatestReadings());
    dispatch(fetchDevices());
    dispatch(fetchAlerts({ status: 'Unacknowledged' }));
  };

  // Prepare chart data for temperature readings
  const tempChartData = {
  labels: (latestReadings && Array.isArray(latestReadings) 
    ? latestReadings
        .filter(reading => reading?.sensorType === 'Temperature')
        .map(reading => reading?.deviceName || 'Unknown Device')
    : []),
  datasets: [{
    label: 'Temperature (°C)',
    data: (latestReadings && Array.isArray(latestReadings)
      ? latestReadings
          .filter(reading => reading?.sensorType === 'Temperature')
          .map(reading => reading?.value || 0)
      : []),
    backgroundColor: 'rgba(255, 99, 132, 0.2)',
    borderColor: 'rgba(255, 99, 132, 1)',
    borderWidth: 1
  }]
};

  // Prepare chart data for device status
  const statusChartData = {
    labels: ['Online', 'Offline', 'Active'],
    datasets: [{
      label: 'Device Status',
      data: [
        devices.filter(device => device.online === true).length,
        devices.filter(device => device.online === false).length,
        devices.filter(device => device.status === 'Active').length
      ],
      backgroundColor: [
        'rgba(75, 192, 192, 0.2)',
        'rgba(255, 99, 132, 0.2)',
        'rgba(255, 206, 86, 0.2)'
      ],
      borderColor: [
        'rgba(75, 192, 192, 1)',
        'rgba(255, 99, 132, 1)',
        'rgba(255, 206, 86, 1)'
      ],
      borderWidth: 1
    }]
  };

  // Loading state
  if (sensorLoading || deviceLoading || alertsLoading) {
    return (
      <Container maxWidth="lg" sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress size={60} />
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 2 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">Dashboard</Typography>
        <Button
          variant="outlined"
          startIcon={<Refresh />}
          onClick={handleRefresh}
          disabled={sensorLoading || deviceLoading || alertsLoading}
        >
          Refresh
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <Grid container spacing={3}>
        {/* Summary Cards */}
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <DeviceThermostat fontSize="large" color="primary" sx={{ mr: 2 }} />
                <Box>
                  <Typography variant="h5">
                    {!sensorLoading && latestReadings && Array.isArray(latestReadings)
                        ? latestReadings.find(r => r.sensorType === 'Temperature')?.value : '--'}°C
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Avg Temperature
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Opacity fontSize="large" color="info" sx={{ mr: 2 }} />
                <Box>
                  <Typography variant="h5">
                    {!sensorLoading && latestReadings && Array.isArray(latestReadings)
                     ? latestReadings.find(r => r.sensorType === 'Humidity')?.value : '--'}%
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Avg Humidity
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Devices fontSize="large" color="secondary" sx={{ mr: 2 }} />
                <Box>
                  <Typography variant="h5">{devices.length}</Typography>
                  <Typography variant="body2" color="text.secondary">
                    Total Devices
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Warning fontSize="large" color="warning" sx={{ mr: 2 }} />
                <Box>
                  <Typography variant="h5">{unreadAlerts.length}</Typography>
                  <Typography variant="body2" color="text.secondary">
                    Active Alerts
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Temperature Chart */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Temperature Readings
              </Typography>
              <Box sx={{ height: '300px' }}>
                <Bar 
                  data={tempChartData}
                  options={{
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                      y: {
                        beginAtZero: false,
                        title: {
                          display: true,
                          text: 'Temperature (°C)'
                        }
                      }
                    }
                  }}
                />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Device Status Chart */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Device Status
              </Typography>
              <Box sx={{ height: '300px' }}>
                <Bar 
                  data={statusChartData}
                  options={{
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                      y: {
                        beginAtZero: true,
                        ticks: {
                          stepSize: 1
                        }
                      }
                    }
                  }}
                />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Recent Alerts */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Recent Alerts
              </Typography>
              {unreadAlerts.length > 0 ? (
                <Box sx={{ maxHeight: '300px', overflow: 'auto' }}>
                  {unreadAlerts.slice(0, 5).map((alert, index) => (
                    <Box key={alert.id} sx={{ mb: 2 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center' }}>
                        {alert.severity === 'Critical' ? (
                          <Error color="error" sx={{ mr: 1 }} />
                        ) : (
                          <Warning color="warning" sx={{ mr: 1 }} />
                        )}
                        <Typography variant="subtitle1" sx={{ flexGrow: 1 }}>
                          {alert.deviceName}
                        </Typography>
                        <Typography variant="caption">
                          {new Date(alert.createdAt).toLocaleDateString()}{new Date(alert.createdAt).toLocaleTimeString()}
                        </Typography>
                      </Box>
                      <Typography variant="body2" color="text.secondary">
                        {alert.message}
                      </Typography>
                      {index < unreadAlerts.length - 1 && <Divider sx={{ my: 1 }} />}
                    </Box>
                  ))}
                </Box>
              ) : (
                <Box sx={{ 
                  display: 'flex', 
                  flexDirection: 'column', 
                  alignItems: 'center', 
                  py: 4,
                  textAlign: 'center'
                }}>
                  <CheckCircle sx={{ fontSize: 60, color: 'text.secondary', mb: 2 }} />
                  <Typography variant="h6" color="text.secondary">
                    No active alerts
                  </Typography>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Device Health */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Device Health Overview
              </Typography>
              <Grid container spacing={2}>
                {devices.slice(0, 4).map(device => (
                  <Grid item xs={12} sm={6} key={device.id}>
                    <Card variant="outlined">
                      <CardContent>
                        <Box sx={{ display: 'flex', alignItems: 'center' }}>
                          {device.online === true ? (
                            <CheckCircle color="success" sx={{ mr: 1 }} />
                          ) : device.status === 'Active' ? (
                            <Settings color="info" sx={{ mr: 1 }} />
                          ) : (
                            <Error color="error" sx={{ mr: 1 }} />
                          )}
                          <Box>
                            <Typography variant="subtitle1">
                              {device.name}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              {device.type} • {device.location}
                            </Typography>
                          </Box>
                        </Box>
                      </CardContent>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
};

export default Dashboard;