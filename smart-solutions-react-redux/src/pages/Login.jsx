import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, Link } from 'react-router-dom'; 
import {
  Container,
  Box,
  Typography,
  TextField,
  Button,
  Grid,
  Divider,
  IconButton,
  InputAdornment,
  Alert,
  CircularProgress,
  Paper
} from '@mui/material';
import {
  Email,
  Lock,
  Visibility,
  VisibilityOff,
  Google,
  Facebook,
  GitHub
} from '@mui/icons-material';
import { fetchUserProfile, login } from '../features/auth/authSlice';
import logo from '../logo.svg';

const Login = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { isAuthenticated, loading, error, userId } = useSelector((state) => state.auth);

  const [formData, setFormData] = useState({
    email: '',
    password: '',
    showPassword: false
  });

  const [errors, setErrors] = useState({
    email: '',
    password: ''
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
    // Clear error when user types
    if (errors[name]) {
      setErrors({
        ...errors,
        [name]: ''
      });
    }
  };

  const handleClickShowPassword = () => {
    setFormData({
      ...formData,
      showPassword: !formData.showPassword
    });
  };

  const validate = () => {
    let valid = true;
    const newErrors = {
      email: '',
      password: ''
    };

    if (!formData.email) {
      newErrors.email = 'Email is required';
      valid = false;
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email is invalid';
      valid = false;
    }

    if (!formData.password) {
      newErrors.password = 'Password is required';
      valid = false;
    } else if (formData.password.length < 8) {
      newErrors.password = 'Password must be at least 8 characters';
      valid = false;
    }

    setErrors(newErrors);
    return valid;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    try {
      await dispatch(login({
        email: formData.email,
        password: formData.password
      })).unwrap();

      // Navigation is now handled by the useEffect listening to isAuthenticated
    } catch (error) {
      console.error('Login failed:', error);
    }
  };

  // Redirect if authenticated
  useEffect(() => {
    if (isAuthenticated) {
      setTimeout(() => {
        dispatch(fetchUserProfile(userId));

        navigate('/dashboard');
      }, 2000);
    }
  }, [isAuthenticated, navigate, userId, dispatch]);

  const handleSocialLogin = (provider) => {
    // Implement social login redirect
    window.location.href = `/api/auth/${provider}`;
  };

  return (
    <Container component="main" maxWidth="xs">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <img src={logo} alt="Smart Solutions" style={{ height: 60, marginBottom: 20 }} />
        <Typography component="h1" variant="h5" sx={{ mb: 2 }}>
          Sign in to Smart Solutions
        </Typography>
        
        <Paper elevation={3} sx={{ p: 3, width: '100%' }}>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}
          
          <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="email"
              label="Email Address"
              name="email"
              autoComplete="email"
              autoFocus
              value={formData.email}
              onChange={handleChange}
              error={!!errors.email}
              helperText={errors.email}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Email />
                  </InputAdornment>
                ),
              }}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              name="password"
              label="Password"
              type={formData.showPassword ? 'text' : 'password'}
              id="password"
              autoComplete="current-password"
              value={formData.password}
              onChange={handleChange}
              error={!!errors.password}
              helperText={errors.password}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Lock />
                  </InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      aria-label="toggle password visibility"
                      onClick={handleClickShowPassword}
                      edge="end"
                    >
                      {formData.showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2, py: 1.5 }}
              disabled={loading}
            >
              {loading ? (
                <CircularProgress size={24} color="inherit" />
              ) : (
                'Sign In'
              )}
            </Button>
            <Grid container>
              <Grid item xs>
                <Link to="/forgot-password" style={{ textDecoration: 'none' }}>
                  <Typography variant="body2" color="primary">
                    Forgot password?
                  </Typography>
                </Link>
              </Grid>
              <Grid item>
                <Link to="/register" style={{ textDecoration: 'none' }}>
                  <Typography variant="body2" color="primary">
                    Don't have an account? Sign Up
                  </Typography>
                </Link>
              </Grid>
            </Grid>
          </Box>

          <Divider sx={{ my: 3 }}>OR</Divider>

          <Box sx={{ display: 'flex', justifyContent: 'center', gap: 1 }}>
            <IconButton 
              onClick={() => handleSocialLogin('google')}
              sx={{ 
                backgroundColor: theme => theme.palette.grey[100],
                '&:hover': { backgroundColor: theme => theme.palette.grey[300] }
              }}
            >
              <Google color="error" />
            </IconButton>
            <IconButton 
              onClick={() => handleSocialLogin('facebook')}
              sx={{ 
                backgroundColor: theme => theme.palette.grey[100],
                '&:hover': { backgroundColor: theme => theme.palette.grey[300] }
              }}
            >
              <Facebook color="primary" />
            </IconButton>
            <IconButton 
              onClick={() => handleSocialLogin('github')}
              sx={{ 
                backgroundColor: theme => theme.palette.grey[100],
                '&:hover': { backgroundColor: theme => theme.palette.grey[300] }
              }}
            >
              <GitHub />
            </IconButton>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default Login;