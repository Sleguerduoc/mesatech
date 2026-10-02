import { useCallback, useEffect, useState } from 'react';
import { jwtDecode } from 'jwt-decode';
import api, { API_URL, obtenerAccessToken, textoError } from '../api';
import { aTexto, formatearEpoch } from '../utils';
import Mensaje from './Mensaje';

const CLAIMS = ['name', 'preferred_username', 'oid', 'roles', 'scp', 'aud', 'iss', 'iat', 'exp'];

function tiempoRestante(exp, ahora) {
  if (!exp) {
    return '-';
  }
  const segundos = Math.floor(exp - ahora / 1000);
  if (segundos <= 0) {
    return 'Expirado';
  }
  const min = Math.floor(segundos / 60);
  const seg = segundos % 60;
  return `${min} min ${String(seg).padStart(2, '0')} s`;
}

/**
 * Inspección del ACCESS TOKEN que se envía a API Gateway (el mismo que valida el BFF).
 */
function MiToken() {
  const [token, setToken] = useState(null);
  const [claims, setClaims] = useState(null);
  const [mostrarCrudo, setMostrarCrudo] = useState(false);
  const [ahora, setAhora] = useState(Date.now());
  const [mensaje, setMensaje] = useState(null);
  const [perfil, setPerfil] = useState(null);

  const cargarToken = useCallback(async () => {
    try {
      const accessToken = await obtenerAccessToken();
      setToken(accessToken);
      setClaims(jwtDecode(accessToken));
    } catch (error) {
      setMensaje({ tipo: 'error', texto: `No se pudo obtener el token. ${textoError(error)}` });
    }
  }, []);

  useEffect(() => {
    cargarToken();
  }, [cargarToken]);

  // Reloj para el tiempo restante del token
  useEffect(() => {
    const intervalo = setInterval(() => setAhora(Date.now()), 1000);
    return () => clearInterval(intervalo);
  }, []);

  const copiarToken = async () => {
    try {
      await navigator.clipboard.writeText(token);
      setMensaje({ tipo: 'ok', texto: 'Token copiado. Pégalo en https://jwt.ms para inspeccionarlo.' });
    } catch {
      setMensaje({ tipo: 'error', texto: 'No se pudo copiar el token al portapapeles' });
    }
  };

  const consultarPerfil = async () => {
    setPerfil(null);
    try {
      const respuesta = await api.get('/v1/perfil');
      setPerfil(respuesta.data);
      setMensaje({ tipo: 'ok', texto: `OK ${respuesta.status}: claims validados por el BFF` });
    } catch (error) {
      setMensaje({ tipo: 'error', texto: textoError(error) });
    }
  };

  const valorClaim = (nombre) => {
    if (nombre === 'iat' || nombre === 'exp') {
      return `${formatearEpoch(claims[nombre])} (${claims[nombre]})`;
    }
    return aTexto(claims[nombre]);
  };

  return (
    <section className="tarjeta">
      <h2>Mi access token</h2>
      <p className="nota">
        Claims del access token de la API (mesatech-api) que se envía como <code>Authorization: Bearer</code> a API Gateway.
      </p>
      <Mensaje mensaje={mensaje} />

      {claims && (
        <table className="tabla tabla-claims">
          <tbody>
            {CLAIMS.map((nombre) => (
              <tr key={nombre}>
                <th>{nombre}</th>
                <td>{valorClaim(nombre)}</td>
              </tr>
            ))}
            <tr>
              <th>tiempo restante</th>
              <td>{tiempoRestante(claims.exp, ahora)}</td>
            </tr>
          </tbody>
        </table>
      )}

      <div className="acciones">
        <button type="button" className="btn" onClick={() => setMostrarCrudo(!mostrarCrudo)} disabled={!token}>
          {mostrarCrudo ? 'Ocultar token' : 'Mostrar token'}
        </button>
        <button type="button" className="btn" onClick={copiarToken} disabled={!token}>
          Copiar token
        </button>
        <button type="button" className="btn btn-secundario" onClick={cargarToken}>
          Recargar token
        </button>
        <button type="button" className="btn" onClick={consultarPerfil}>
          Consultar /v1/perfil
        </button>
      </div>

      {mostrarCrudo && token && <pre className="token-crudo">{token}</pre>}

      {perfil && (
        <>
          <h3>Respuesta de GET {API_URL}/v1/perfil</h3>
          <pre>{JSON.stringify(perfil, null, 2)}</pre>
        </>
      )}
    </section>
  );
}

export default MiToken;
