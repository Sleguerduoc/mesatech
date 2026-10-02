// IMPORTANTE: ocultar opciones en la UI según el rol es solo experiencia de usuario.
// La autorización real la hace el BFF con @PreAuthorize sobre el token validado; aunque alguien
// modifique el frontend, el backend responderá 403 si no tiene el rol requerido.

export const ROL_ADMINISTRADOR = 'ROLE_ADMINISTRADOR';
export const ROL_OPERADOR = 'ROLE_OPERADOR';
export const ROL_CLIENTE = 'ROLE_CLIENTE';

// Orden de mayor a menor privilegio
const JERARQUIA = [ROL_ADMINISTRADOR, ROL_OPERADOR, ROL_CLIENTE];

const ETIQUETAS = {
  [ROL_ADMINISTRADOR]: 'Administrador',
  [ROL_OPERADOR]: 'Operador',
  [ROL_CLIENTE]: 'Cliente',
};

/**
 * Obtiene el rol de mayor privilegio desde el claim "roles" del ACCESS TOKEN de la API
 * (los app roles de mesatech-api vienen en el access token, no en el id token).
 */
export function obtenerRol(claims) {
  const roles = (claims && Array.isArray(claims.roles)) ? claims.roles : [];
  return JERARQUIA.find((rol) => roles.includes(rol)) || null;
}

export function etiquetaRol(rol) {
  return ETIQUETAS[rol] || 'Sin rol';
}

export function esOperadorOAdministrador(rol) {
  return rol === ROL_OPERADOR || rol === ROL_ADMINISTRADOR;
}
