import axios from 'axios';

const API = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json'
  }
});

API.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  const userId = localStorage.getItem('userId');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  if (userId) {
    config.headers['X-User-Id'] = userId;
  }
  return config;
});

export const authAPI = {
  login: (credentials) => API.post('/auth/authenticate', credentials),
  register: (userData) => API.post('/auth/register', userData),
  fetchProfile: (id) => API.get(`/users/getUserById/${id}`),
  updateProfile: (profileData) => API.patch('/users/profile', profileData),
  changePassword: (passwordData) => API.patch('/users/password', passwordData),
  forgotPassword: (email) => API.post('/auth/forgot-password', { email }),
  verifyToken: () => API.post('/auth/verify-token', {}, {
    headers: {
      Authorization: `Bearer ${localStorage.getItem('token') || ''}`
    }
  })
};

export const deviceAPI = {
  getAllDevices: () => API.get('/devices/getAllDevices'),
  getDeviceById: (id) => API.get(`/devices/getDeviceById/${id}`),
  createDevice: (deviceData) => API.post('/devices/createDevice', deviceData),
  updateDevice: (id, deviceData) => API.put(`/devices/updateDevice/${id}`, deviceData),
  deleteDevice: (id) => API.delete(`/devices/deleteDevice/${id}`)
};

export const sensorAPI = {
  getSensorData: (deviceId, params) => API.get(`/devices/sensor-data/${deviceId}`, { params }),
  getLatestReadings: () => API.get('/sensor-data/latest'),
  getDeviceSensors: (deviceId) => API.get(`/devices/sensors/${deviceId}`),
  addReading: (deviceId, data) => API.post(`/devices/sensor-data/${deviceId}`, data),
};

export const alertAPI = {
  getAlerts: (filters) => API.get('/alerts/getAllAlerts', { params: filters }),
  acknowledgeAlert: (alertId) => API.patch(`/alerts/acknowledgeAlert/${alertId}`),
};