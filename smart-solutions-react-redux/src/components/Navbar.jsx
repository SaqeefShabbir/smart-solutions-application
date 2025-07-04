import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { 
  AppBar,
  Toolbar,
  IconButton,
  Typography,
  Box,
  Avatar,
  Menu,
  MenuItem,
  ListItemIcon,
  Divider,
  Badge,
  Tooltip,
  useMediaQuery,
  useTheme
} from '@mui/material';
import {
  Menu as MenuIcon,
  Notifications,
  AccountCircle,
  Logout,
  Settings,
  Devices,
  Warning,
  LightMode,
  DarkMode
} from '@mui/icons-material';
import { logout, selectCurrentUser } from '../features/auth/authSlice';
import { toggleTheme } from '../features/theme/themeSlice';

const Navbar = ({ toggleDrawer }) => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  
  const user = useSelector(selectCurrentUser);
  const { unreadNotifications } = useSelector((state) => state.notifications);
  const { darkMode } = useSelector((state) => state.theme);

  const [anchorEl, setAnchorEl] = useState(null);
  const [notificationsAnchorEl, setNotificationsAnchorEl] = useState(null);

  const handleMenuOpen = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleNotificationsOpen = (event) => {
    setNotificationsAnchorEl(event.currentTarget);
  };

  const handleNotificationsClose = () => {
    setNotificationsAnchorEl(null);
  };

  const handleLogout = () => {
    dispatch(logout());
    navigate('/login');
    handleMenuClose();
  };

  const handleThemeToggle = () => {
    dispatch(toggleTheme());
  };

  return (
    <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
      <Toolbar>
        {/* Mobile menu button */}
        {isMobile && (
          <IconButton
            size="large"
            edge="start"
            color="inherit"
            aria-label="menu"
            sx={{ mr: 2 }}
            onClick={toggleDrawer}
          >
            <MenuIcon />
          </IconButton>
        )}

        {/* Logo/Brand */}
        <Typography 
          variant="h6" 
          component="div" 
          sx={{ 
            flexGrow: 1,
            cursor: 'pointer',
            fontWeight: 'bold',
            display: 'flex',
            alignItems: 'center'
          }}
          onClick={() => navigate('/')}
        >
          <Box 
            component="span" 
            sx={{ 
              backgroundColor: 'white', 
              color: theme.palette.primary.main,
              px: 1.5,
              py: 0.5,
              borderRadius: 1,
              mr: 1
            }}
          >
            SMART
          </Box>
          Solutions
        </Typography>

        {/* Theme toggle */}
        <Tooltip title={darkMode ? "Switch to light mode" : "Switch to dark mode"}>
          <IconButton
            size="large"
            color="inherit"
            onClick={handleThemeToggle}
            sx={{ mr: 1 }}
          >
            {darkMode ? <LightMode /> : <DarkMode />}
          </IconButton>
        </Tooltip>

        {/* Notifications */}
        <Tooltip title="Notifications">
          <IconButton
            size="large"
            color="inherit"
            onClick={handleNotificationsOpen}
            sx={{ mr: 1 }}
          >
            <Badge badgeContent={unreadNotifications} color="error">
              <Notifications />
            </Badge>
          </IconButton>
        </Tooltip>

        {/* User profile */}
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <Typography variant="body1" sx={{ mr: 1 }}>
            {user?.firstname || 'User'}
          </Typography>
          <IconButton
            size="large"
            edge="end"
            aria-label="account of current user"
            aria-controls="menu-appbar"
            aria-haspopup="true"
            onClick={handleMenuOpen}
            color="inherit"
          >
            <Avatar 
              sx={{ width: 32, height: 32 }}
              src={user?.avatar}
              alt={user?.firstname}
            >
              {user?.firstname?.charAt(0)}
            </Avatar>
          </IconButton>
        </Box>

        {/* User menu */}
        <Menu
          id="menu-appbar"
          anchorEl={anchorEl}
          anchorOrigin={{
            vertical: 'top',
            horizontal: 'right',
          }}
          keepMounted
          transformOrigin={{
            vertical: 'top',
            horizontal: 'right',
          }}
          open={Boolean(anchorEl)}
          onClose={handleMenuClose}
        >
          {[
            <MenuItem key="profile" onClick={() => { navigate('/profile'); handleMenuClose(); }}>
              <ListItemIcon>
                <AccountCircle fontSize="small" />
              </ListItemIcon>
              Profile
            </MenuItem>,
            <MenuItem key="settings" onClick={() => { navigate('/settings'); handleMenuClose(); }}>
              <ListItemIcon>
                <Settings fontSize="small" />
              </ListItemIcon>
              Settings
            </MenuItem>,
            <Divider key="divider" />,
            <MenuItem key="logout" onClick={handleLogout}>
              <ListItemIcon>
                <Logout fontSize="small" />
              </ListItemIcon>
              Logout
            </MenuItem>
          ]}
        </Menu>

        {/* Notifications menu */}
        <Menu
          id="notifications-menu"
          anchorEl={notificationsAnchorEl}
          anchorOrigin={{
            vertical: 'top',
            horizontal: 'right',
          }}
          keepMounted
          transformOrigin={{
            vertical: 'top',
            horizontal: 'right',
          }}
          open={Boolean(notificationsAnchorEl)}
          onClose={handleNotificationsClose}
        >
          <MenuItem dense disabled>
            <Typography variant="subtitle2">Notifications</Typography>
          </MenuItem>
          <Divider />
          {unreadNotifications > 0 ? (
            [
              <MenuItem key="alerts" onClick={() => { navigate('/alerts'); handleNotificationsClose(); }}>
                <ListItemIcon>
                  <Warning color="warning" fontSize="small" />
                </ListItemIcon>
                <Box>
                  <Typography variant="body2">3 new alerts detected</Typography>
                  <Typography variant="caption" color="text.secondary">
                    10 minutes ago
                  </Typography>
                </Box>
              </MenuItem>,
              <MenuItem key="devices" onClick={() => { navigate('/devices'); handleNotificationsClose(); }}>
                <ListItemIcon>
                  <Devices color="info" fontSize="small" />
                </ListItemIcon>
                <Box>
                  <Typography variant="body2">Device firmware update available</Typography>
                  <Typography variant="caption" color="text.secondary">
                    2 hours ago
                  </Typography>
                </Box>
              </MenuItem>
            ]
          ) : (
            <MenuItem disabled>
              <Typography variant="body2">No new notifications</Typography>
            </MenuItem>
          )}
          <Divider />
          <MenuItem onClick={() => { navigate('/notifications'); handleNotificationsClose(); }}>
            <Typography variant="body2" color="primary">
              View all notifications
            </Typography>
          </MenuItem>
        </Menu>
      </Toolbar>
    </AppBar>
  );
};

export default Navbar;