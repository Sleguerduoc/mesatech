import React from 'react';
import ReactDOM from 'react-dom/client';
import { EventType, PublicClientApplication } from '@azure/msal-browser';
import { MsalProvider } from '@azure/msal-react';
import './index.css';
import App from './App';
import { msalConfig } from './authConfig';
import { configurarMsal } from './api';

const msalInstance = new PublicClientApplication(msalConfig);
configurarMsal(msalInstance);

const root = ReactDOM.createRoot(document.getElementById('root'));

msalInstance.initialize().then(() => {
  // Si ya hay una cuenta en caché (recarga de página), la dejamos como cuenta activa
  const cuentas = msalInstance.getAllAccounts();
  if (!msalInstance.getActiveAccount() && cuentas.length > 0) {
    msalInstance.setActiveAccount(cuentas[0]);
  }

  // Al volver del loginRedirect, marcar la cuenta recién autenticada como activa
  msalInstance.addEventCallback((evento) => {
    if (evento.eventType === EventType.LOGIN_SUCCESS && evento.payload && evento.payload.account) {
      msalInstance.setActiveAccount(evento.payload.account);
    }
  });

  root.render(
    <React.StrictMode>
      <MsalProvider instance={msalInstance}>
        <App />
      </MsalProvider>
    </React.StrictMode>,
  );
});
