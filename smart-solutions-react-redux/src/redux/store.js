import { configureStore } from '@reduxjs/toolkit';
import authReducer from '../features/auth/authSlice';
import deviceReducer from '../features/devices/deviceSlice';
import sensorReducer from '../features/sensors/sensorSlice';
import alertReducer from '../features/alerts/alertSlice';
import notificationReducer from '../features/notification/notificationSlice';
import themeReducer from '../features/theme/themeSlice';

const store = configureStore({
  reducer: {
    auth: authReducer,
    devices: deviceReducer,
    sensor: sensorReducer,
    alerts: alertReducer,
    notifications: notificationReducer,
    theme: themeReducer
  },

  middleware: (getDefaultMiddleware) => getDefaultMiddleware(),
  devTools: process.env.NODE_ENV !== 'production'
});

export default store;