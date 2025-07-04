import React, { useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { useSelector } from 'react-redux';

// Fix default marker icons (Leaflet issue with Webpack)
import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';
import { selectAllDevices } from '../features/devices/deviceSlice';

let DefaultIcon = L.icon({
  iconUrl: icon,
  shadowUrl: iconShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});
L.Marker.prototype.options.icon = DefaultIcon;

const MapContent = () => {
  const devices = useSelector((state) => selectAllDevices(state));
  const map = useMap();

  // Fit map to markers if devices exist
  useEffect(() => {
    if (devices.length > 0) {
      const bounds = L.latLngBounds(
        devices.map((device) => [device.latitude, device.longitude])
      );
      map.fitBounds(bounds, { padding: [50, 50] });
    }
  }, [devices, map]);

  return (
    <>
      <TileLayer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
      />
      {devices.map((device) => (
        <Marker
          key={device.id}
          position={[device.latitude, device.longitude]}
        >
          <Popup>
            <div>
              <h3>{device.name}</h3>
              <p>Status: {device.status}</p>
              {device.alertsCount > 0 && (
                <p>Alerts: {device.alertsCount}</p>
              )}
            </div>
          </Popup>
        </Marker>
      ))}
    </>
  );
};

const MapPage = () => {
  return (
    <div className="map-page">
      <h1>Device Locations</h1>
      <div className="map-container">
        <MapContainer
          center={[51.505, -0.09]} // Default center (London)
          zoom={13}
          style={{ height: '80vh', width: '100%' }}
        >
          <MapContent />
        </MapContainer>
      </div>
    </div>
  );
};

export default MapPage;