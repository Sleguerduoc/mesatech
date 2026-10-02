/**
 * Mensaje visible de éxito o error. mensaje = { tipo: 'ok' | 'error', texto }.
 */
function Mensaje({ mensaje }) {
  if (!mensaje) {
    return null;
  }
  return <div className={`mensaje mensaje-${mensaje.tipo}`}>{mensaje.texto}</div>;
}

export default Mensaje;
