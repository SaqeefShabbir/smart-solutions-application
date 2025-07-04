import React from 'react';
import {
  Drawer,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Box,
  Divider,
  useTheme,
  useMediaQuery
} from '@mui/material';
import {
  Dashboard as DashboardIcon,
  Devices as DevicesIcon,
  ShowChart as SensorDataIcon,
  Notifications as AlertsIcon,
  Map as MapIcon,
  Settings as SettingsIcon,
  Person as ProfileIcon
} from '@mui/icons-material';
import { Link } from 'react-router-dom';

const Sidebar = ({ mobileOpen, handleDrawerToggle }) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  
  const drawer = (
    <div>
      <Toolbar />
      <Divider />
      <List>
        <ListItem button="true" component={Link} to="/dashboard">
          <ListItemIcon><DashboardIcon /></ListItemIcon>
          <ListItemText primary="Dashboard" />
        </ListItem>
        <ListItem button="true" component={Link} to="/devices">
          <ListItemIcon><DevicesIcon /></ListItemIcon>
          <ListItemText primary="Devices" />
        </ListItem>
        <ListItem button="true" component={Link} to="/sensor-data">
          <ListItemIcon><SensorDataIcon /></ListItemIcon>
          <ListItemText primary="Sensor Data" />
        </ListItem>
        <ListItem button="true" component={Link} to="/alerts">
          <ListItemIcon><AlertsIcon /></ListItemIcon>
          <ListItemText primary="Alerts" />
        </ListItem>
        <ListItem button="true" component={Link} to="/map">
          <ListItemIcon><MapIcon /></ListItemIcon>
          <ListItemText primary="Map" />
        </ListItem>
      </List>
      <Divider />
      <List>
        <ListItem button="true" component={Link} to="/profile">
          <ListItemIcon><ProfileIcon /></ListItemIcon>
          <ListItemText primary="Profile" />
        </ListItem>
        <ListItem button="true" component={Link} to="/settings">
          <ListItemIcon><SettingsIcon /></ListItemIcon>
          <ListItemText primary="Settings" />
        </ListItem>
      </List>
    </div>
  );

  return (
    <Box
      component="nav"
      sx={{ width: { md: theme.drawerWidth }, flexShrink: { md: 0 } }}
    >
      <Drawer
        variant={isMobile ? 'temporary' : 'permanent'}
        open={isMobile ? mobileOpen : true}
        onClose={handleDrawerToggle}
        ModalProps={{
          keepMounted: true, // Better open performance on mobile.
        }}
        sx={{
          '& .MuiDrawer-paper': {
            width: theme.drawerWidth,
            boxSizing: 'border-box',
          },
        }}
      >
        {drawer}
      </Drawer>
    </Box>
  );
};

export default Sidebar;