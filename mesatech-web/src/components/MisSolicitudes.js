import { useCallback, useEffect, useState } from 'react';
import api, { API_URL, textoError } from '../api';
import { formatearFecha } from '../utils';
import Mensaje from './Mensaje';

const FORM_VACIO = { titulo: '', descripcion: '', categoriaId: '', prioridadId: '' };

function MisSolicitudes() {
  const [catalogo, setCatalogo] = useState({ categorias: [], prioridades: [] });
  // EP2: las categorías salen de la copia local de ms-solicitudes (recibidas por RabbitMQ)
  const [categoriasLocales, setCategoriasLocales] = useState([]);
  const [form, setForm] = useState(FORM_VACIO);
  const [mensajeForm, setMensajeForm] = useState(null);

  const [version, setVersion] = useState('v1');
  const [solicitudes, setSolicitudes] = useState([]);
  const [mensajeTabla, setMensajeTabla] = useState(null);

  const ruta = `/${version}/solicitudes/mias`;

  const cargarCategoriasLocales = useCallback(async () => {
    try {
      const respuesta = await api.get('/v1/solicitudes/categorias');
      setCategoriasLocales(respuesta.data);
    } catch (error) {
      setMensajeForm({ tipo: 'error', texto: `No se pudieron cargar las categorías disponibles. ${textoError(error)}` });
    }
  }, []);

  // Prioridades (desde Catálogo) y categorías disponibles (copia local de Solicitudes) para los selects
  useEffect(() => {
    api.get('/v1/catalogo')
      .then((respuesta) => setCatalogo(respuesta.data))
      .catch((error) => setMensajeForm({ tipo: 'error', texto: `No se pudo cargar el catálogo. ${textoError(error)}` }));
    cargarCategoriasLocales();
  }, [cargarCategoriasLocales]);

  const cargarSolicitudes = useCallback(async () => {
    setSolicitudes([]);
    try {
      const respuesta = await api.get(ruta);
      // v1 responde una lista; v2 responde { version, total, solicitudes }
      const lista = version === 'v2' ? respuesta.data.solicitudes : respuesta.data;
      setSolicitudes(lista);
      setMensajeTabla({ tipo: 'ok', texto: `OK ${respuesta.status}: ${lista.length} solicitud(es)` });
    } catch (error) {
      setMensajeTabla({ tipo: 'error', texto: textoError(error) });
    }
  }, [ruta, version]);

  useEffect(() => {
    cargarSolicitudes();
  }, [cargarSolicitudes]);

  const cambiar = (evento) => setForm({ ...form, [evento.target.name]: evento.target.value });

  const crear = async (evento) => {
    evento.preventDefault();
    setMensajeForm(null);
    try {
      // Solo se envían estos 4 campos: el solicitante lo toma el BFF desde el token
      const respuesta = await api.post('/v1/solicitudes', {
        titulo: form.titulo,
        descripcion: form.descripcion,
        categoriaId: form.categoriaId ? Number(form.categoriaId) : null,
        prioridadId: form.prioridadId ? Number(form.prioridadId) : null,
      });
      setMensajeForm({ tipo: 'ok', texto: `OK ${respuesta.status}: solicitud #${respuesta.data.id} creada` });
      setForm(FORM_VACIO);
      cargarSolicitudes();
    } catch (error) {
      setMensajeForm({ tipo: 'error', texto: textoError(error) });
    }
  };

  const nombreCategoria = (id) => (categoriasLocales.find((c) => c.categoriaId === id) || {}).nombre;

  return (
    <>
      <section className="tarjeta">
        <h2>Nueva solicitud</h2>
        <form className="formulario" onSubmit={crear}>
          <label>
            Título
            <input name="titulo" value={form.titulo} onChange={cambiar} maxLength={150} required />
          </label>
          <label>
            Descripción
            <textarea name="descripcion" value={form.descripcion} onChange={cambiar} maxLength={2000} rows={3} required />
          </label>
          <div className="fila-campos">
            <label>
              Categoría
              <select name="categoriaId" value={form.categoriaId} onChange={cambiar} required>
                <option value="">Seleccione...</option>
                {categoriasLocales.map((c) => (
                  <option key={c.categoriaId} value={c.categoriaId}>{c.nombre}</option>
                ))}
              </select>
            </label>
            <label>
              Prioridad
              <select name="prioridadId" value={form.prioridadId} onChange={cambiar} required>
                <option value="">Seleccione...</option>
                {catalogo.prioridades.map((p) => (
                  <option key={p.id} value={p.id}>{p.nombre} (SLA {p.slaHoras} h)</option>
                ))}
              </select>
            </label>
          </div>
          <p className="nota">
            Categorías disponibles en Solicitudes: {categoriasLocales.length} (copia local recibida desde Catálogo por
            RabbitMQ). Una categoría recién creada aparece cuando el consumidor procesa su evento.
            {' '}
            <button type="button" className="btn btn-secundario" onClick={cargarCategoriasLocales}>Actualizar categorías</button>
          </p>
          <div>
            <button type="submit" className="btn">Crear solicitud</button>
          </div>
        </form>
        <Mensaje mensaje={mensajeForm} />
      </section>

      <section className="tarjeta">
        <div className="titulo-con-acciones">
          <h2>Mis solicitudes</h2>
          <div className="acciones">
            <label className="en-linea">
              Versión de la API
              <select value={version} onChange={(e) => setVersion(e.target.value)}>
                <option value="v1">v1</option>
                <option value="v2">v2</option>
              </select>
            </label>
            <button type="button" className="btn btn-secundario" onClick={cargarSolicitudes}>Recargar</button>
          </div>
        </div>

        <p className="url-llamada">GET {API_URL}{ruta}</p>
        <p className="nota">
          {version === 'v1'
            ? 'v1: lista plana de solicitudes, solo con los IDs de categoría y prioridad.'
            : 'v2: respuesta { version, total, solicitudes } donde cada solicitud agrega categoriaNombre, prioridadNombre, '
              + 'slaHoras, horasAbierta y vencida. El BFF combina ms-solicitudes y ms-catalogo; v1 sigue disponible sin cambios.'}
        </p>
        <Mensaje mensaje={mensajeTabla} />

        <div className="tabla-contenedor">
          <table className="tabla">
            <thead>
              <tr>
                <th>ID</th>
                <th>Título</th>
                <th>categoriaId</th>
                <th>prioridadId</th>
                <th>Estado</th>
                <th>Fecha</th>
                {version === 'v2' && (
                  <>
                    <th>categoriaNombre</th>
                    <th>prioridadNombre</th>
                    <th>slaHoras</th>
                    <th>horasAbierta</th>
                    <th>vencida</th>
                  </>
                )}
              </tr>
            </thead>
            <tbody>
              {solicitudes.map((s) => (
                <tr key={s.id}>
                  <td>{s.id}</td>
                  <td title={s.descripcion}>{s.titulo}</td>
                  <td title={nombreCategoria(s.categoriaId)}>{s.categoriaId}</td>
                  <td>{s.prioridadId}</td>
                  <td><span className={`estado estado-${s.estado}`}>{s.estado}</span></td>
                  <td>{formatearFecha(s.fechaCreacion)}</td>
                  {version === 'v2' && (
                    <>
                      <td>{s.categoriaNombre || '-'}</td>
                      <td>{s.prioridadNombre || '-'}</td>
                      <td>{s.slaHoras ?? '-'}</td>
                      <td>{s.horasAbierta}</td>
                      <td className={s.vencida ? 'texto-error' : ''}>{s.vencida ? 'Sí' : 'No'}</td>
                    </>
                  )}
                </tr>
              ))}
              {solicitudes.length === 0 && (
                <tr>
                  <td colSpan={version === 'v2' ? 11 : 6} className="vacio">Sin solicitudes</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}

export default MisSolicitudes;
