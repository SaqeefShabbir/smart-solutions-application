import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { authAPI } from '../../services/api';

export const login = createAsyncThunk(
  'auth/login',
  async (credentials, { rejectWithValue }) => {
    try {
      const response = await authAPI.login(credentials);
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response.data);
    }
  }
);

// Update user profile
export const updateUserProfile = createAsyncThunk(
  'auth/updateProfile',
  async (profileData, { rejectWithValue }) => {
    try {
      const response = await authAPI.updateProfile(profileData);
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response.data);
    }
  }
);

// Change password
export const changePassword = createAsyncThunk(
  'auth/changePassword',
  async (passwordData, { rejectWithValue }) => {
    try {
      const response = await authAPI.changePassword(passwordData);
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response.data);
    }
  }
);

// Fetch user profile
export const fetchUserProfile = createAsyncThunk(
  'auth/fetchUserProfile',
  async (userId, { rejectWithValue }) => {
    try {
      const response = await authAPI.fetchProfile(userId);
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response.data);
    }
  }
);


export const checkAuthState = createAsyncThunk(
  'auth/checkAuthState',
  async (_, { rejectWithValue }) => {
    try {
      // const token = localStorage.getItem('token');
      // if (!token) return rejectWithValue('No token found');
      
      const response = await authAPI.verifyToken();
      return response.data;
    } catch (err) {
      localStorage.removeItem('token');
      // return rejectWithValue('Invalid token');
    }
  }
);

export const register = createAsyncThunk(
  'auth/register',
  async (userData, { rejectWithValue }) => {
    try {
      const response = await authAPI.register(userData);
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response.data);
    }
  }
);

export const forgotPassword = createAsyncThunk(
  'auth/forgotPassword',
  async (email, { rejectWithValue }) => {
    try {
      const response = await authAPI.forgotPassword(email);
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Failed to send reset link');
    }
  }
);

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    userId: localStorage.getItem('userId') || null,
    token: localStorage.getItem('token') || null,
    isAuthenticated: localStorage.getItem('token') ? true : false,
    user: {},
    loading: false,
    error: null,
  },
  reducers: {
    logout: (state) => {
      localStorage.removeItem('token');
      localStorage.removeItem('userId');
      state.userId = null;
      state.token = null;
      state.isAuthenticated = false;
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(login.pending, (state) => {
        state.loading = true;
        state.isAuthenticated = false;
      })
      .addCase(login.fulfilled, (state, action) => {
        state.userId = action.payload.userId;
        state.token = action.payload.token;
        state.isAuthenticated = true;
        localStorage.setItem('userId', action.payload.userId);
        localStorage.setItem('token', action.payload.token);
        state.loading = false;
      })
      .addCase(login.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
        state.isAuthenticated = false;
      })
      // Update Profile
      .addCase(updateUserProfile.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateUserProfile.fulfilled, (state, action) => {
        state.loading = false;
        state.user = action.payload.data;
      })
      .addCase(updateUserProfile.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      
      // Change Password
      .addCase(changePassword.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(changePassword.fulfilled, (state, action) => {
        state.loading = false;
      })
      .addCase(changePassword.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      .addCase(checkAuthState.pending, (state) => {
        state.loading = true;
      })
      .addCase(checkAuthState.fulfilled, (state, action) => {
        state.loading = false;
        state.isAuthenticated = true;
        state.userId = action.payload.userId;
        state.token = action.payload.token;
        localStorage.setItem('userId', action.payload.userId);
        localStorage.setItem('token', action.payload.token);
      })
      .addCase(checkAuthState.rejected, (state, action) => {
        state.loading = false;
        state.isAuthenticated = false;
        state.error = action.payload;
      })
      .addCase(register.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(register.fulfilled, (state, action) => {
        state.loading = false;
        state.isAuthenticated = true;
        state.userId = action.payload.userId;
        state.token = action.payload.token;
        // Store token in localStorage or cookies
        localStorage.setItem('userId', action.payload.userId);
        localStorage.setItem('token', action.payload.token);
        
      })
      .addCase(register.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload?.message || 'Registration failed';
      })
      .addCase(forgotPassword.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(forgotPassword.fulfilled, (state, action) => {
        state.loading = false;
        state.message = action.payload.message;
      })
      .addCase(forgotPassword.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      .addCase(fetchUserProfile.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchUserProfile.fulfilled, (state, action) => {
        state.loading = false;
        state.user = action.payload;
        console.log(state.user)
      })
      .addCase(fetchUserProfile.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  }
});

export const selectCurrentUser = (state) => state.auth.user;
export const selectLodingStatus = (state) => state.auth.loading;

export const { logout } = authSlice.actions;
export default authSlice.reducer;