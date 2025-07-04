import { createSlice } from '@reduxjs/toolkit';

const notificationsSlice = createSlice({
  name: 'notifications',
  initialState: {
    unreadNotifications: 3,
    notifications: []
  },
  reducers: {
    markAsRead: (state) => {
      state.unreadNotifications = 0;
    },
  }
});

export const { markAsRead } = notificationsSlice.actions;
export default notificationsSlice.reducer;