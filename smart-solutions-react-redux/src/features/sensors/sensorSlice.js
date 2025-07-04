import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { sensorAPI } from '../../services/api';

// Async thunks for API calls
export const fetchSensorData = createAsyncThunk(
  'sensor/fetchSensorData',
  async ({ deviceId, startDate, endDate }, { rejectWithValue }) => {
    try {
      const response = await sensorAPI.getSensorData(deviceId, { startDate, endDate });
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response.data);
    }
  }
);

export const fetchLatestReadings = createAsyncThunk(
  'sensor/fetchLatestReadings',
  async (_, { rejectWithValue }) => {
    try {
      const response = await sensorAPI.getLatestReadings();
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response.data);
    }
  }
);

export const fetchDeviceSensors = createAsyncThunk(
  'sensor/fetchDeviceSensors',
  async (deviceId, { rejectWithValue }) => {
    try {
      const response = await sensorAPI.getDeviceSensors(deviceId);
      return { deviceId, sensors: response.data };
    } catch (err) {
      return rejectWithValue(err.response.data);
    }
  }
);

export const addSensorReading = createAsyncThunk(
  'sensor/addSensorReading',
  async ({ deviceId, sensorType, value }, { rejectWithValue }) => {
    try {
      const response = await sensorAPI.addReading(deviceId, { sensorType, value });
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response.data);
    }
  }
);

const initialState = {
  // Current sensor data view
  currentDeviceId: null,
  currentSensorType: null,
  dateRange: {
    start: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(), // Default: last 7 days
    end: new Date().toISOString()
  },
  
  // Data storage
  sensorData: [],
  latestReadings: [],
  deviceSensors: {}, // { deviceId: [sensors] }
  
  // Status flags
  loading: false,
  loadingLatest: false,
  loadingDeviceSensors: false,
  error: null,
  errorLatest: null,
  errorDeviceSensors: null
};

const sensorSlice = createSlice({
  name: 'sensor',
  initialState,
  reducers: {
    // Set current device and sensor type
    setCurrentDevice: (state, action) => {
      state.currentDeviceId = action.payload;
    },
    setCurrentSensorType: (state, action) => {
      state.currentSensorType = action.payload;
    },
    setDateRange: (state, action) => {
      state.dateRange = action.payload;
    },
    
    // Clear sensor data
    clearSensorData: (state) => {
      state.sensorData = [];
    },
    
    // Reset errors
    clearErrors: (state) => {
      state.error = null;
      state.errorLatest = null;
      state.errorDeviceSensors = null;
    }
  },
  extraReducers: (builder) => {
    builder
      // Fetch Sensor Data
      .addCase(fetchSensorData.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchSensorData.fulfilled, (state, action) => {
        state.loading = false;
        state.sensorData = action.payload.content;
      })
      .addCase(fetchSensorData.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload?.message || 'Failed to fetch sensor data';
      })
      
      // Fetch Latest Readings
      .addCase(fetchLatestReadings.pending, (state) => {
        state.loadingLatest = true;
        state.errorLatest = null;
      })
      .addCase(fetchLatestReadings.fulfilled, (state, action) => {
        state.loadingLatest = false;
        state.latestReadings = action.payload;
      })
      .addCase(fetchLatestReadings.rejected, (state, action) => {
        state.loadingLatest = false;
        state.errorLatest = action.payload?.message || 'Failed to fetch latest readings';
      })
      
      // Fetch Device Sensors
      .addCase(fetchDeviceSensors.pending, (state) => {
        state.loadingDeviceSensors = true;
        state.errorDeviceSensors = null;
      })
      .addCase(fetchDeviceSensors.fulfilled, (state, action) => {
        state.loadingDeviceSensors = false;
        state.deviceSensors = {
          ...state.deviceSensors,
          [action.payload.deviceId]: action.payload.sensors
        };
      })
      .addCase(fetchDeviceSensors.rejected, (state, action) => {
        state.loadingDeviceSensors = false;
        state.errorDeviceSensors = action.payload?.message || 'Failed to fetch device sensors';
      })
      
      // Add Sensor Reading
      .addCase(addSensorReading.fulfilled, (state, action) => {
        // Add to sensor data if it's for the current device and sensor type
        if (action.payload.deviceId === state.currentDeviceId && 
            action.payload.sensorType === state.currentSensorType) {
          state.sensorData = [action.payload, ...state.sensorData];
        }
        
        // Update latest readings if needed
        const readingIndex = state.latestReadings.findIndex(
          r => r.deviceId === action.payload.deviceId && 
               r.sensorType === action.payload.sensorType
        );
        
        if (readingIndex !== -1) {
          state.latestReadings[readingIndex] = action.payload;
        }
      });
  }
});

// Export actions
export const { 
  setCurrentDevice, 
  setCurrentSensorType, 
  setDateRange,
  clearSensorData,
  clearErrors
} = sensorSlice.actions;

// Selectors
export const selectCurrentSensorData = (state) => {
  const { sensorData, currentDeviceId, currentSensorType } = state.sensor;
  
  if (!currentDeviceId || !currentSensorType) return [];
  
  return sensorData.filter(
    data => data.deviceId === currentDeviceId && 
            data.sensorType === currentSensorType
  );
};

export const selectLatestReadings = (state) => state.sensor.latestReadings;
export const selectDeviceSensors = (deviceId) => (state) => 
  state.sensor.deviceSensors[deviceId] || [];
export const selectLoadingDeviceSensorsStatus = (state) => state.sensor.loadingDeviceSensors;
export const selectLoadingStatus = (state) => state.sensor.loading;
export const selectLatestLoadingStatus = (state) => state.sensor.loadingLatest;
export const selectDateRange = (state) => state.sensor.dateRange;

export default sensorSlice.reducer;