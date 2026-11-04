import { useCallback, useEffect, useState } from 'react';
import api, { API_URL, textoError } from '../api';
import Mensaje from './Mensaje';

const CATEGORIA_VACIA = { id: null, nombre: '', descripcion: '' };
const PRIORIDAD_VACIA = { id: null, nombre: '', nivel: '', slaHoras: '' };

/**
 * Administración del catálogo (solo ROLE_ADMINISTRADOR). Desactivar = DELETE lógico en el backend.
 */
function Catalogo() {
  const [catalogo, setCatalogo] = useState({ categorias: [], prioridades: [] });
  const [categoria, setCategoria] = useState(CATEGORIA_VACIA);
  const [prioridad, setPrioridad] = useState(PRIORIDAD_VACIA);
  const [mensaje, setMensaje] = useState(null);
  // EP2: categorías que ya llegaron a la copia local de ms-solicitudes (consumidor de RabbitMQ)
  const [enSolicitudes, setEnSolicitudes] = useState(null);
  const [ultimoEvento, setUltimoEvento] = useState(null);

  const cargarCopiaLocal = useCallback(async () => {
    try {
      const respuesta = await api.get('/v1/solicitudes/categorias');
      setEnSolicitudes(new Map(respuesta.data.map((c) => [c.categoriaId, c])));
    } catch (error) {
      setEnSolicitudes(null);
    }
  }, []);

  const cargar = useCallback(async () => {
    try {
      const respuesta = await api.get('/v1/catalogo', { params: { incluirInactivas: true } });
      setCatalogo(respuesta.data);
    } catch (error) {
      setMensaje({ tipo: 'error', texto: textoError(error) });
    }
    await cargarCopiaLocal();
  }, [cargarCopiaLocal]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  // Mientras haya categorías activas sin copia en Solicitudes, se consulta cada 3 s (procesamiento asíncrono)
  const hayPendientes = enSolicitudes !== null
    && catalogo.categorias.some((c) => c.activa && !enSolicitudes.has(c.id));
  useEffect(() => {
    if (!hayPendientes) {
      return undefined;
    }
    const temporizador = setInterval(cargarCopiaLocal, 3000);
    return () => clearInterval(temporizador);
  }, [hayPendientes, cargarCopiaLocal]);

  const estadoCopia = (c) => {
    if (enSolicitudes === null) {
      return 'Sin datos';
    }
    return enSolicitudes.has(c.id) ? 'Disponible' : 'Pendiente';
  };

  const ejecutar = async (operacion, descripcion) => {
    try {
      const respuesta = await operacion();
      setMensaje({ tipo: 'ok', texto: `OK ${respuesta.status}: ${descripcion}` });
      await cargar();
      return true;
    } catch (error) {
      setMensaje({ tipo: 'error', texto: `${descripcion}: ${textoError(error)}` });
      return false;
    }
  };

  // ---------- Categorías ----------

  const guardarCategoria = async (evento) => {
    evento.preventDefault();
    const cuerpo = { nombre: categoria.nombre, descripcion: categoria.descripcion || null };
    if (categoria.id) {
      const ok = await ejecutar(() => api.put(`/v1/catalogo/categorias/${categoria.id}`, cuerpo), `categoría #${categoria.id} actualizada`);
      if (ok) {
        setCategoria(CATEGORIA_VACIA);
      }
      return;
    }
    try {
      const respuesta = await api.post('/v1/catalogo/categorias', cuerpo);
      const c = respuesta.data;
      setUltimoEvento({ categoriaId: c.id, eventId: c.eventId });
      setMensaje({
        tipo: 'ok',
        texto: `OK ${respuesta.status}: categoría #${c.id} "${c.nombre}" guardada en Catálogo y evento publicado `
          + `(eventId ${c.eventId}). Quedará disponible en Solicitudes cuando el consumidor lo procese.`,
      });
      setCategoria(CATEGORIA_VACIA);
    } catch (error) {
      const cuerpoError = error.cuerpo || {};
      if (cuerpoError.registroGuardado) {
        setUltimoEvento({ categoriaId: cuerpoError.categoriaId, eventId: cuerpoError.eventId, fallido: true });
      }
      setMensaje({ tipo: 'error', texto: `Crear categoría: ${textoError(error)}` });
    }
    await cargar();
  };

  const desactivarCategoria = (c) => {
    if (window.confirm(`¿Desactivar la categoría "${c.nombre}"?`)) {
      ejecutar(() => api.delete(`/v1/catalogo/categorias/${c.id}`), `categoría #${c.id} desactivada`);
    }
  };

  // ---------- Prioridades ----------

  const guardarPrioridad = async (evento) => {
    evento.preventDefault();
    const cuerpo = {
      nombre: prioridad.nombre,
      nivel: prioridad.nivel === '' ? null : Number(prioridad.nivel),
      slaHoras: prioridad.slaHoras === '' ? null : Number(prioridad.slaHoras),
    };
    const ok = prioridad.id
      ? await ejecutar(() => api.put(`/v1/catalogo/prioridades/${prioridad.id}`, cuerpo), `prioridad #${prioridad.id} actualizada`)
      : await ejecutar(() => api.post('/v1/catalogo/prioridades', cuerpo), `prioridad "${cuerpo.nombre}" creada`);
    if (ok) {
      setPrioridad(PRIORIDAD_VACIA);
    }
  };

  const desactivarPrioridad = (p) => {
    if (window.confirm(`¿Desactivar la prioridad "${p.nombre}"?`)) {
      ejecutar(() => api.delete(`/v1/catalogo/prioridades/${p.id}`), `prioridad #${p.id} desactivada`);
    }
  };

  return (
    <>
      <Mensaje mensaje={mensaje} />
      <p className="url-llamada">GET {API_URL}/v1/catalogo?incluirInactivas=true</p>

      <section className="tarjeta">
        <h2>Categorías</h2>
        <form className="formulario formulario-linea" onSubmit={guardarCategoria}>
          <input
            placeholder="Nombre"
            value={categoria.nombre}
            maxLength={100}
            required
            onChange={(e) => setCategoria({ ...categoria, nombre: e.target.value })}
          />
          <input
            placeholder="Descripción"
            value={categoria.descripcion || ''}
            maxLength={500}
            onChange={(e) => setCategoria({ ...categoria, descripcion: e.target.value })}
          />
          <button type="submit" className="btn">{categoria.id ? `Guardar #${categoria.id}` : 'Crear categoría'}</button>
          {categoria.id && (
            <button type="button" className="btn btn-secundario" onClick={() => setCategoria(CATEGORIA_VACIA)}>Cancelar</button>
          )}
        </form>
        {ultimoEvento && (
          <p className="nota">
            Última creación: categoriaId {ultimoEvento.categoriaId}, eventId {ultimoEvento.eventId || '-'}
            {ultimoEvento.fallido ? ' (evento NO confirmado por RabbitMQ)' : ''}
          </p>
        )}
        <p className="nota">
          La columna &quot;En Solicitudes&quot; consulta GET /v1/solicitudes/categorias (copia local de ms-solicitudes).
          {hayPendientes ? ' Hay categorías pendientes: se vuelve a consultar cada 3 segundos.' : ''}
          {' '}
          <button type="button" className="btn btn-secundario" onClick={cargarCopiaLocal}>Comprobar ahora</button>
        </p>
        <div className="tabla-contenedor">
          <table className="tabla">
            <thead>
              <tr><th>ID</th><th>Nombre</th><th>Descripción</th><th>Activa</th><th>En Solicitudes</th><th /></tr>
            </thead>
            <tbody>
              {catalogo.categorias.map((c) => (
                <tr key={c.id} className={c.activa ? '' : 'inactiva'}>
                  <td>{c.id}</td>
                  <td>{c.nombre}</td>
                  <td>{c.descripcion || '-'}</td>
                  <td>{c.activa ? 'Sí' : 'No'}</td>
                  <td className={estadoCopia(c) === 'Pendiente' ? 'texto-error' : ''}>{estadoCopia(c)}</td>
                  <td className="acciones">
                    <button type="button" className="btn btn-secundario" onClick={() => setCategoria(c)}>Editar</button>
                    {c.activa && (
                      <button type="button" className="btn btn-peligro" onClick={() => desactivarCategoria(c)}>Desactivar</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <section className="tarjeta">
        <h2>Prioridades</h2>
        <form className="formulario formulario-linea" onSubmit={guardarPrioridad}>
          <input
            placeholder="Nombre"
            value={prioridad.nombre}
            maxLength={50}
            required
            onChange={(e) => setPrioridad({ ...prioridad, nombre: e.target.value })}
          />
          <input
            type="number"
            min={1}
            placeholder="Nivel (1 = más baja)"
            value={prioridad.nivel}
            required
            onChange={(e) => setPrioridad({ ...prioridad, nivel: e.target.value })}
          />
          <input
            type="number"
            min={1}
            placeholder="SLA (horas)"
            value={prioridad.slaHoras}
            required
            onChange={(e) => setPrioridad({ ...prioridad, slaHoras: e.target.value })}
          />
          <button type="submit" className="btn">{prioridad.id ? `Guardar #${prioridad.id}` : 'Crear prioridad'}</button>
          {prioridad.id && (
            <button type="button" className="btn btn-secundario" onClick={() => setPrioridad(PRIORIDAD_VACIA)}>Cancelar</button>
          )}
        </form>
        <div className="tabla-contenedor">
          <table className="tabla">
            <thead>
              <tr><th>ID</th><th>Nombre</th><th>Nivel</th><th>SLA (h)</th><th>Activa</th><th /></tr>
            </thead>
            <tbody>
              {catalogo.prioridades.map((p) => (
                <tr key={p.id} className={p.activa ? '' : 'inactiva'}>
                  <td>{p.id}</td>
                  <td>{p.nombre}</td>
                  <td>{p.nivel}</td>
                  <td>{p.slaHoras}</td>
                  <td>{p.activa ? 'Sí' : 'No'}</td>
                  <td className="acciones">
                    <button type="button" className="btn btn-secundario" onClick={() => setPrioridad(p)}>Editar</button>
                    {p.activa && (
                      <button type="button" className="btn btn-peligro" onClick={() => desactivarPrioridad(p)}>Desactivar</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}

export default Catalogo;
