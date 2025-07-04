import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { deviceAPI } from '../../services/api';

export const fetchDevices = createAsyncThunk(
  'devices/fetchDevices',
  async () => {
    const response = await deviceAPI.getAllDevices();
    return response.data;
  }
);

export const addDevice = createAsyncThunk(
  'devices/addDevice',
  async (deviceData) => {
    const response = await deviceAPI.createDevice(deviceData);
    return response.data;
  }
);

export const updateDevice = createAsyncThunk(
  'devices/updateDevice',
  async ({ id, ...deviceData }) => {
    const response = await deviceAPI.updateDevice(id, deviceData);
    return response.data;
  }
);

export const deleteDevice = createAsyncThunk(
  'devices/deleteDevice',
  async (id) => {
    await deviceAPI.deleteDevice(id);
    return id;
  }
);

const deviceSlice = createSlice({
  name: 'devices',
  initialState: {
    devices: [],
    loading: false,
    error: null
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      // Fetch Devices
      .addCase(fetchDevices.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchDevices.fulfilled, (state, action) => {
        state.loading = false;
        state.devices = action.payload.content;
        console.log(state.devices)
      })
      .addCase(fetchDevices.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message;
      })
      
      // Add Device
      .addCase(addDevice.fulfilled, (state, action) => {
        state.devices.push(action.payload);
      })
      
      // Update Device
      .addCase(updateDevice.fulfilled, (state, action) => {
        const index = state.devices.findIndex(d => d.id === action.payload.id);
        if (index !== -1) {
          state.devices[index] = action.payload;
        }
      })
      
      // Delete Device
      .addCase(deleteDevice.fulfilled, (state, action) => {
        state.devices = state.devices.filter(d => d.id !== action.payload);
      });
  }
});

export const selectAllDevices = (state) => state.devices.devices;
export const selectDeviceLoadingStatus = (state) => state.devices.loading;

export default deviceSlice.reducer;