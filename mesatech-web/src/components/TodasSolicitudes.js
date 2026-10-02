import { useCallback, useEffect, useState } from 'react';
import api, { API_URL, textoError } from '../api';
import { formatearFecha } from '../utils';
import Mensaje from './Mensaje';

// Se ofrecen TODOS los estados a propósito: el front no filtra transiciones para poder evidenciar
// que la regla de negocio la aplica el backend (responde 409 ante una transición inválida).
const ESTADOS = ['CREADA', 'ASIGNADA', 'EN_PROCESO', 'RESUELTA', 'CERRADA', 'CANCELADA'];

function TodasSolicitudes() {
  const [solicitudes, setSolicitudes] = useState([]);
  const [cambios, setCambios] = useState({});
  const [mensaje, setMensaje] = useState(null);

  const cargar = useCallback(async () => {
    try {
      const respuesta = await api.get('/v1/solicitudes');
      setSolicitudes(respuesta.data);
      setCambios({});
      return respuesta;
    } catch (error) {
      setSolicitudes([]);
      setMensaje({ tipo: 'error', texto: textoError(error) });
      return null;
    }
  }, []);

  useEffect(() => {
    cargar().then((respuesta) => {
      if (respuesta) {
        setMensaje({ tipo: 'ok', texto: `OK ${respuesta.status}: ${respuesta.data.length} solicitud(es)` });
      }
    });
  }, [cargar]);

  const cambioDe = (s) => cambios[s.id] || { estado: s.estado, detalle: '' };

  const actualizarCampo = (s, campo, valor) => {
    setCambios({ ...cambios, [s.id]: { ...cambioDe(s), [campo]: valor } });
  };

  const actualizar = async (s) => {
    const cambio = cambioDe(s);
    try {
      const respuesta = await api.patch(`/v1/solicitudes/${s.id}/estado`, {
        estado: cambio.estado,
        detalle: cambio.detalle.trim() === '' ? null : cambio.detalle,
      });
      await cargar();
      setMensaje({
        tipo: 'ok',
        texto: `OK ${respuesta.status}: solicitud #${s.id} pasó de ${s.estado} a ${respuesta.data.estado}`,
      });
    } catch (error) {
      setMensaje({ tipo: 'error', texto: `Solicitud #${s.id} (${s.estado} -> ${cambio.estado}): ${textoError(error)}` });
    }
  };

  return (
    <section className="tarjeta">
      <div className="titulo-con-acciones">
        <h2>Todas las solicitudes</h2>
        <button type="button" className="btn btn-secundario" onClick={cargar}>Recargar</button>
      </div>
      <p className="url-llamada">GET {API_URL}/v1/solicitudes · PATCH {API_URL}/v1/solicitudes/&#123;id&#125;/estado</p>
      <p className="nota">
        Se muestran todos los estados sin filtrar: si intentas una transición no permitida
        (por ejemplo CREADA → RESUELTA) el backend responde 409.
      </p>
      <Mensaje mensaje={mensaje} />

      <div className="tabla-contenedor">
        <table className="tabla">
          <thead>
            <tr>
              <th>ID</th>
              <th>Título</th>
              <th>Solicitante</th>
              <th>Estado</th>
              <th>Operador</th>
              <th>Detalle de atención</th>
              <th>Creada</th>
              <th>Nuevo estado</th>
              <th>Detalle de la atención</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {solicitudes.map((s) => {
              const cambio = cambioDe(s);
              return (
                <tr key={s.id}>
                  <td>{s.id}</td>
                  <td title={s.descripcion}>{s.titulo}</td>
                  <td>{s.solicitanteEmail || s.solicitanteOid}</td>
                  <td><span className={`estado estado-${s.estado}`}>{s.estado}</span></td>
                  <td>{s.operadorAsignado || '-'}</td>
                  <td className="detalle">{s.detalleAtencion || '-'}</td>
                  <td>{formatearFecha(s.fechaCreacion)}</td>
                  <td>
                    <select value={cambio.estado} onChange={(e) => actualizarCampo(s, 'estado', e.target.value)}>
                      {ESTADOS.map((estado) => (
                        <option key={estado} value={estado}>{estado}</option>
                      ))}
                    </select>
                  </td>
                  <td>
                    <input
                      value={cambio.detalle}
                      onChange={(e) => actualizarCampo(s, 'detalle', e.target.value)}
                      placeholder="Detalle (opcional)"
                    />
                  </td>
                  <td>
                    <button type="button" className="btn" onClick={() => actualizar(s)}>Actualizar</button>
                  </td>
                </tr>
              );
            })}
            {solicitudes.length === 0 && (
              <tr>
                <td colSpan={10} className="vacio">Sin solicitudes</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}

export default TodasSolicitudes;
