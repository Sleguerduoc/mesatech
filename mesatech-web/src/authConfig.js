import { BrowserCacheLocation } from '@azure/msal-browser';

const CLIENT_ID = process.env.REACT_APP_CLIENT_ID;
const TENANT_ID = process.env.REACT_APP_TENANT_ID;
const API_CLIENT_ID = process.env.REACT_APP_API_CLIENT_ID;

// Configuración de MSAL para la SPA registrada en Microsoft Entra ID
export const msalConfig = {
  auth: {
    clientId: CLIENT_ID,
    authority: `https://login.microsoftonline.com/${TENANT_ID}`,
    // URL donde se sirve la SPA: bucket S3 por HTTPS (https://<bucket>.s3.us-east-1.amazonaws.com/index.html)
    // o http://localhost:3000/ en desarrollo. Ambas deben estar registradas como Redirect URI SPA en Entra ID.
    redirectUri: `${window.location.origin}${window.location.pathname}`,
    postLogoutRedirectUri: `${window.location.origin}${window.location.pathname}`,
  },
  cache: {
    // sessionStorage: la sesión se pierde al cerrar la pestaña (más seguro que localStorage)
    cacheLocation: BrowserCacheLocation.SessionStorage,
  },
};

// Scopes del inicio de sesión (ID token con datos básicos del usuario)
export const loginRequest = {
  scopes: ['openid', 'profile', 'email'],
};

// Scopes de la API mesatech-api: el access token obtenido con estos scopes es el que se envía a API Gateway
export const apiRequest = {
  scopes: [
    `api://${API_CLIENT_ID}/Solicitudes.Access`,
    `api://${API_CLIENT_ID}/Catalogo.Access`,
  ],
};
