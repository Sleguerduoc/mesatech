import axios from 'axios';
import { InteractionRequiredAuthError } from '@azure/msal-browser';
import { apiRequest } from './authConfig';

// El frontend SOLO habla con AWS API Gateway; nunca llama directo a la instancia EC2
export const API_URL = process.env.REACT_APP_API_URL;

let msalInstance = null;

/** index.js entrega aquí la instancia de MSAL para poder pedir tokens fuera de los componentes. */
export function configurarMsal(instance) {
  msalInstance = instance;
}

/**
 * Obtiene el access token de la API (mesatech-api). Primero intenta en silencio (caché o refresh token);
 * si Entra ID exige interacción (consentimiento, sesión expirada), redirige al login.
 */
export async function obtenerAccessToken() {
  const account = msalInstance.getActiveAccount() || msalInstance.getAllAccounts()[0];
  if (!account) {
    throw new Error('No hay una sesión activa');
  }
  try {
    const resultado = await msalInstance.acquireTokenSilent({ ...apiRequest, account });
    return resultado.accessToken;
  } catch (error) {
    if (error instanceof InteractionRequiredAuthError) {
      await msalInstance.acquireTokenRedirect({ ...apiRequest, account });
      throw new Error('Se requiere interacción: redirigiendo a Microsoft para renovar el token...');
    }
    throw error;
  }
}

/**
 * Convierte cualquier error en { status, mensaje, cuerpo }.
 * El BFF responde { mensaje }, API Gateway responde { message }.
 */
export function normalizarError(error) {
  if (error && error.normalizado) {
    return error;
  }
  const respuesta = error && error.response;
  if (respuesta) {
    const data = respuesta.data;
    let mensaje = null;
    if (data && typeof data === 'object') {
      mensaje = data.mensaje || data.message || data.error;
    } else if (typeof data === 'string' && data.trim() !== '') {
      mensaje = data;
    }
    return {
      normalizado: true,
      status: respuesta.status,
      mensaje: mensaje || respuesta.statusText || 'Error sin mensaje',
      cuerpo: data,
    };
  }
  const mensaje = error && error.isAxiosError
    ? `Sin respuesta del servidor (red o CORS): ${error.message}`
    : (error && error.message) || 'Error desconocido';
  return { normalizado: true, status: 0, mensaje, cuerpo: null };
}

/** Texto listo para mostrar: "Error 409: Transición no permitida: ..." */
export function textoError(error) {
  const e = normalizarError(error);
  return `Error ${e.status || 'sin status'}: ${e.mensaje}`;
}

const api = axios.create({ baseURL: API_URL });

// Agrega "Authorization: Bearer <access token>" a cada petición
api.interceptors.request.use(async (config) => {
  const token = await obtenerAccessToken();
  config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (respuesta) => respuesta,
  (error) => Promise.reject(normalizarError(error)),
);

export default api;
