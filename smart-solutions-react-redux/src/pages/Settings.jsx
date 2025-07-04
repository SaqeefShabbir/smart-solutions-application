import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  Container,
  Typography,
  Grid,
  Card,
  CardContent,
  CardHeader,
  Avatar,
  TextField,
  Button,
  Divider,
  Switch,
  FormControlLabel,
  MenuItem,
  CircularProgress,
  Alert,
  Tabs,
  Tab,
  Box
} from '@mui/material';
import {
  Person,
  Notifications,
  Security,
  Save,
  LockReset,
  Language
} from '@mui/icons-material';
import { updateUserProfile, changePassword } from '../features/auth/authSlice';

const Settings = () => {
  const dispatch = useDispatch();
  const { user, loading, error, success } = useSelector((state) => state.auth);
  const [tabValue, setTabValue] = useState(0);
  const [profileForm, setProfileForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
  });
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  const [notifications, setNotifications] = useState({
    emailAlerts: true,
    pushNotifications: true,
    smsAlerts: false,
    criticalOnly: false
  });
  const [preferences, setPreferences] = useState({
    theme: 'light',
    language: 'en',
    timezone: 'UTC'
  });

  useEffect(() => {
    if (user) {
      setProfileForm({
        firstName: user.firstname || '',
        lastName: user.lastname || '',
        email: user.email || '',
      });
      setNotifications(user.notificationSettings || {
        emailAlerts: true,
        pushNotifications: true,
        smsAlerts: false,
        criticalOnly: false
      });
      setPreferences(user.preferences || {
        theme: 'light',
        language: 'en',
        timezone: 'UTC'
      });
    }
  }, [user]);

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
  };

  const handleProfileChange = (e) => {
    const { name, value } = e.target;
    setProfileForm({
      ...profileForm,
      [name]: value
    });
  };

  const handlePasswordChange = (e) => {
    const { name, value } = e.target;
    setPasswordForm({
      ...passwordForm,
      [name]: value
    });
  };

  const handleNotificationChange = (e) => {
    const { name, checked } = e.target;
    setNotifications({
      ...notifications,
      [name]: checked
    });
  };

  const handlePreferenceChange = (e) => {
    const { name, value } = e.target;
    setPreferences({
      ...preferences,
      [name]: value
    });
  };

  const handleProfileSubmit = (e) => {
    e.preventDefault();
    dispatch(updateUserProfile(profileForm));
  };

  const handlePasswordSubmit = (e) => {
    e.preventDefault();
    if (passwordForm.newPassword === passwordForm.confirmPassword) {
      dispatch(changePassword({
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword
      }));
      setPasswordForm({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      });
    }
  };

  const handleNotificationsSubmit = () => {
    dispatch(updateUserProfile({ ...profileForm, notificationSettings: notifications }));
  };

  const handlePreferencesSubmit = () => {
    dispatch(updateUserProfile({ ...profileForm, preferences }));
  };

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom>Settings</Typography>
      
      <Tabs value={tabValue} onChange={handleTabChange} aria-label="settings tabs">
        <Tab icon={<Person />} label="Profile" />
        <Tab icon={<Security />} label="Security" />
        <Tab icon={<Notifications />} label="Notifications" />
        <Tab icon={<Language />} label="Preferences" />
      </Tabs>
      <Divider sx={{ mb: 4 }} />

      {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 3 }}>Settings updated successfully!</Alert>}

      {/* Profile Tab */}
      <Box hidden={tabValue !== 0}>
        <Card>
          <CardHeader
            avatar={<Avatar><Person /></Avatar>}
            title="Profile Information"
            subheader="Update your personal details"
          />
          <CardContent>
            <form onSubmit={handleProfileSubmit}>
              <Grid container spacing={3}>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="First Name"
                    name="firstName"
                    value={profileForm.firstName}
                    onChange={handleProfileChange}
                    required
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Last Name"
                    name="lastName"
                    value={profileForm.lastName}
                    onChange={handleProfileChange}
                    required
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Email"
                    name="email"
                    type="email"
                    value={profileForm.email}
                    onChange={handleProfileChange}
                    required
                  />
                </Grid>
                <Grid item xs={12}>
                  <Button
                    type="submit"
                    variant="contained"
                    color="primary"
                    startIcon={<Save />}
                    disabled={loading}
                  >
                    {loading ? <CircularProgress size={24} /> : 'Save Profile'}
                  </Button>
                </Grid>
              </Grid>
            </form>
          </CardContent>
        </Card>
      </Box>

      {/* Security Tab */}
      <Box hidden={tabValue !== 1}>
        <Card>
          <CardHeader
            avatar={<Avatar><Security /></Avatar>}
            title="Security Settings"
            subheader="Change your password and security preferences"
          />
          <CardContent>
            <form onSubmit={handlePasswordSubmit}>
              <Grid container spacing={3}>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Current Password"
                    name="currentPassword"
                    type="password"
                    value={passwordForm.currentPassword}
                    onChange={handlePasswordChange}
                    required
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="New Password"
                    name="newPassword"
                    type="password"
                    value={passwordForm.newPassword}
                    onChange={handlePasswordChange}
                    required
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Confirm New Password"
                    name="confirmPassword"
                    type="password"
                    value={passwordForm.confirmPassword}
                    onChange={handlePasswordChange}
                    required
                    error={passwordForm.newPassword !== passwordForm.confirmPassword}
                    helperText={passwordForm.newPassword !== passwordForm.confirmPassword ? "Passwords don't match" : ""}
                  />
                </Grid>
                <Grid item xs={12}>
                  <Button
                    type="submit"
                    variant="contained"
                    color="primary"
                    startIcon={<LockReset />}
                    disabled={loading || passwordForm.newPassword !== passwordForm.confirmPassword}
                  >
                    {loading ? <CircularProgress size={24} /> : 'Change Password'}
                  </Button>
                </Grid>
              </Grid>
            </form>
          </CardContent>
        </Card>
      </Box>

      {/* Notifications Tab */}
      <Box hidden={tabValue !== 2}>
        <Card>
          <CardHeader
            avatar={<Avatar><Notifications /></Avatar>}
            title="Notification Preferences"
            subheader="Configure how you receive alerts and notifications"
          />
          <CardContent>
            <Grid container spacing={3}>
              <Grid item xs={12}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={notifications.emailAlerts}
                      onChange={handleNotificationChange}
                      name="emailAlerts"
                      color="primary"
                    />
                  }
                  label="Email Alerts"
                />
              </Grid>
              <Grid item xs={12}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={notifications.pushNotifications}
                      onChange={handleNotificationChange}
                      name="pushNotifications"
                      color="primary"
                    />
                  }
                  label="Push Notifications"
                />
              </Grid>
              <Grid item xs={12}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={notifications.smsAlerts}
                      onChange={handleNotificationChange}
                      name="smsAlerts"
                      color="primary"
                    />
                  }
                  label="SMS Alerts"
                />
              </Grid>
              <Grid item xs={12}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={notifications.criticalOnly}
                      onChange={handleNotificationChange}
                      name="criticalOnly"
                      color="primary"
                    />
                  }
                  label="Critical Alerts Only"
                />
              </Grid>
              <Grid item xs={12}>
                <Button
                  variant="contained"
                  color="primary"
                  startIcon={<Save />}
                  onClick={handleNotificationsSubmit}
                  disabled={loading}
                >
                  {loading ? <CircularProgress size={24} /> : 'Save Notification Settings'}
                </Button>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      </Box>

      {/* Preferences Tab */}
      <Box hidden={tabValue !== 3}>
        <Card>
          <CardHeader
            avatar={<Avatar><Language /></Avatar>}
            title="System Preferences"
            subheader="Customize your application experience"
          />
          <CardContent>
            <Grid container spacing={3}>
              <Grid item xs={12} sm={6}>
                <TextField
                  select
                  fullWidth
                  label="Theme"
                  name="theme"
                  value={preferences.theme}
                  onChange={handlePreferenceChange}
                >
                  <MenuItem value="light">Light</MenuItem>
                  <MenuItem value="dark">Dark</MenuItem>
                  <MenuItem value="system">System Default</MenuItem>
                </TextField>
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  select
                  fullWidth
                  label="Language"
                  name="language"
                  value={preferences.language}
                  onChange={handlePreferenceChange}
                >
                  <MenuItem value="en">English</MenuItem>
                  <MenuItem value="es">Spanish</MenuItem>
                  <MenuItem value="fr">French</MenuItem>
                  <MenuItem value="de">German</MenuItem>
                </TextField>
              </Grid>
              <Grid item xs={12}>
                <TextField
                  select
                  fullWidth
                  label="Timezone"
                  name="timezone"
                  value={preferences.timezone}
                  onChange={handlePreferenceChange}
                >
                  <MenuItem value="UTC">UTC</MenuItem>
                  <MenuItem value="EST">Eastern Time (EST)</MenuItem>
                  <MenuItem value="PST">Pacific Time (PST)</MenuItem>
                  <MenuItem value="CET">Central European Time (CET)</MenuItem>
                </TextField>
              </Grid>
              <Grid item xs={12}>
                <Button
                  variant="contained"
                  color="primary"
                  startIcon={<Save />}
                  onClick={handlePreferencesSubmit}
                  disabled={loading}
                >
                  {loading ? <CircularProgress size={24} /> : 'Save Preferences'}
                </Button>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      </Box>
    </Container>
  );
};

export default Settings;