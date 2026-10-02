/** Fecha ISO (LocalDateTime del backend) en hora local legible. */
export function formatearFecha(valor) {
  if (!valor) {
    return '-';
  }
  const fecha = new Date(valor);
  return Number.isNaN(fecha.getTime()) ? String(valor) : fecha.toLocaleString('es-CL');
}

/** Fecha en segundos epoch (claims iat/exp del JWT) en hora local legible. */
export function formatearEpoch(segundos) {
  return segundos ? new Date(segundos * 1000).toLocaleString('es-CL') : '-';
}

/** Muestra cualquier valor (objeto, lista, texto) como texto para las tablas y bloques <pre>. */
export function aTexto(valor) {
  if (valor === undefined || valor === null || valor === '') {
    return '-';
  }
  if (Array.isArray(valor)) {
    return valor.join(', ');
  }
  return typeof valor === 'object' ? JSON.stringify(valor, null, 2) : String(valor);
}
