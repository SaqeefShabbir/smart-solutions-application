import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { alertAPI } from '../../services/api';
import { createSelector } from '@reduxjs/toolkit';

// Fetch all alerts with filters
export const fetchAlerts = createAsyncThunk(
  'alerts/fetchAlerts',
  async (filters) => {
    const response = await alertAPI.getAlerts(filters);
    return response.data;
  }
);

// Acknowledge an alert
export const acknowledgeAlert = createAsyncThunk(
  'alerts/acknowledgeAlert',
  async (alertId) => {
    const response = await alertAPI.acknowledgeAlert(alertId);
    return response.data;
  }
);

const alertSlice = createSlice({
  name: 'alerts',
  initialState: {
    alerts: [],
    devices: [],
    loading: false,
    error: null,
    dateRange: {
    start: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(), // Default: last 7 days
    end: new Date().toISOString()
  },
  },
  reducers: {
    setDateRange: (state, action) => {
      state.dateRange = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch Alerts
      .addCase(fetchAlerts.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAlerts.fulfilled, (state, action) => {
        state.loading = false;
        state.alerts = action.payload.content;
      })
      .addCase(fetchAlerts.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message;
      })
      
      // Acknowledge Alert
      .addCase(acknowledgeAlert.fulfilled, (state, action) => {
        const index = state.alerts.findIndex(a => a.id === action.payload.id);
        if (index !== -1) {
          state.alerts[index] = action.payload;
        }
      })
  }
});

//Export actions
export const { 
  setDateRange,
} = alertSlice.actions;

export const selectAllAlerts = (state) => state.alerts.alerts;
export const selectUnreadAlerts = createSelector(
  [selectAllAlerts],
  (alerts) => {
    // Return stable reference when no unread alerts
    if (alerts.length === 0) return [];
    
    // Filter and return new array only when needed
    return alerts.filter(alert => !alert.acknowledged);
  }
);
export const selectAlertsLoadingStatus = (state) => state.alerts.loading;
export const selectDateRange = (state) => state.alerts.dateRange;

export default alertSlice.reducer;