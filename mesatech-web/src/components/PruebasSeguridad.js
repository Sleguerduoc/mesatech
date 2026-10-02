import { useState } from 'react';
import api, { API_URL, obtenerAccessToken, textoError } from '../api';
import { aTexto } from '../utils';
import Mensaje from './Mensaje';

/** Llamada con fetch y un header Authorization opcional (sin pasar por el interceptor de axios). */
async function llamarConFetch(ruta, authorization) {
  const headers = authorization ? { Authorization: authorization } : {};
  const respuesta = await fetch(`${API_URL}${ruta}`, { headers });
  const texto = await respuesta.text();
  let cuerpo = texto;
  try {
    cuerpo = JSON.parse(texto);
  } catch {
    // no era JSON: se muestra como texto
  }
  return { status: respuesta.status, cuerpo };
}

/** Llamada con token válido que devuelve la respuesta aunque sea 4xx/5xx. */
async function llamarConToken(metodo, ruta, data) {
  const respuesta = await api.request({ method: metodo, url: ruta, data, validateStatus: () => true });
  return { status: respuesta.status, cuerpo: respuesta.data };
}

/** Altera un carácter de la FIRMA del JWT: el token sigue bien formado, pero la firma ya no es válida. */
function alterarToken(token) {
  const partes = token.split('.');
  const firma = partes[2];
  const i = Math.floor(firma.length / 2);
  partes[2] = firma.slice(0, i) + (firma[i] === 'A' ? 'B' : 'A') + firma.slice(i + 1);
  return partes.join('.');
}

const PRUEBAS = [
  {
    id: 'todas',
    titulo: 'Listar todas las solicitudes',
    metodo: 'GET',
    ruta: '/v1/solicitudes',
    esperado: '403 para CLIENTE (200 para OPERADOR/ADMINISTRADOR)',
    ejecutar: () => llamarConToken('GET', '/v1/solicitudes'),
  },
  {
    id: 'crearCategoria',
    titulo: 'Crear categoría de prueba',
    metodo: 'POST',
    ruta: '/v1/catalogo/categorias',
    esperado: '403 para CLIENTE y OPERADOR (si eres ADMINISTRADOR se crea de verdad: 201)',
    ejecutar: () => llamarConToken('POST', '/v1/catalogo/categorias', {
      nombre: `Prueba seguridad ${Date.now()}`,
      descripcion: 'Categoría creada desde la pestaña Pruebas de seguridad',
    }),
  },
  {
    id: 'sinToken',
    titulo: 'Catálogo SIN token',
    metodo: 'GET',
    ruta: '/v1/catalogo',
    esperado: '401 de API Gateway (la petición no llega al BFF)',
    ejecutar: () => llamarConFetch('/v1/catalogo', null),
  },
  {
    id: 'tokenAlterado',
    titulo: 'Catálogo con token ALTERADO',
    metodo: 'GET',
    ruta: '/v1/catalogo',
    esperado: '401 (firma inválida)',
    ejecutar: async () => llamarConFetch('/v1/catalogo', `Bearer ${alterarToken(await obtenerAccessToken())}`),
  },
  {
    id: 'mias',
    titulo: 'Mis solicitudes con token válido',
    metodo: 'GET',
    ruta: '/v1/solicitudes/mias',
    esperado: '200',
    ejecutar: () => llamarConToken('GET', '/v1/solicitudes/mias'),
  },
];

/**
 * Genera evidencias de las pruebas mínimas de seguridad: método, URL, status HTTP y cuerpo de cada respuesta.
 */
function PruebasSeguridad() {
  const [resultados, setResultados] = useState({});
  const [mensaje, setMensaje] = useState(null);

  const ejecutar = async (prueba) => {
    setResultados((prev) => ({ ...prev, [prueba.id]: { cargando: true } }));
    try {
      const resultado = await prueba.ejecutar();
      setResultados((prev) => ({ ...prev, [prueba.id]: { ...resultado, fecha: new Date().toLocaleString('es-CL') } }));
      setMensaje(null);
    } catch (error) {
      setResultados((prev) => ({ ...prev, [prueba.id]: null }));
      setMensaje({ tipo: 'error', texto: `${prueba.titulo}: ${textoError(error)}` });
    }
  };

  const ejecutarTodas = async () => {
    for (const prueba of PRUEBAS) {
      // secuencial para que las evidencias queden en orden
      // eslint-disable-next-line no-await-in-loop
      await ejecutar(prueba);
    }
  };

  const claseStatus = (status) => {
    if (status >= 200 && status < 300) return 'status status-ok';
    if (status >= 400 && status < 500) return 'status status-4xx';
    return 'status status-error';
  };

  return (
    <section className="tarjeta">
      <div className="titulo-con-acciones">
        <h2>Pruebas de seguridad</h2>
        <button type="button" className="btn" onClick={ejecutarTodas}>Ejecutar todas</button>
      </div>
      <p className="nota">Cada prueba muestra el método, la URL, el status HTTP y el cuerpo de la respuesta.</p>
      <Mensaje mensaje={mensaje} />

      {PRUEBAS.map((prueba) => {
        const r = resultados[prueba.id];
        return (
          <div key={prueba.id} className="prueba">
            <div className="titulo-con-acciones">
              <div>
                <strong>{prueba.titulo}</strong>
                <div className="url-llamada">{prueba.metodo} {API_URL}{prueba.ruta}</div>
                <div className="nota">Esperado: {prueba.esperado}</div>
              </div>
              <button type="button" className="btn btn-secundario" onClick={() => ejecutar(prueba)}>Ejecutar</button>
            </div>
            {r && r.cargando && <p className="nota">Ejecutando...</p>}
            {r && !r.cargando && (
              <>
                <p>
                  Status: <span className={claseStatus(r.status)}>{r.status}</span>
                  <span className="nota"> · {r.fecha}</span>
                </p>
                <pre>{aTexto(r.cuerpo)}</pre>
              </>
            )}
          </div>
        );
      })}
    </section>
  );
}

export default PruebasSeguridad;
