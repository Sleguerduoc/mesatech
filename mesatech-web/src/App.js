import { useEffect, useState } from 'react';
import { AuthenticatedTemplate, UnauthenticatedTemplate, useMsal } from '@azure/msal-react';
import { InteractionStatus } from '@azure/msal-browser';
import { jwtDecode } from 'jwt-decode';
import './App.css';
import { obtenerAccessToken, textoError } from './api';
import { esOperadorOAdministrador, obtenerRol, ROL_ADMINISTRADOR } from './roles';
import PantallaLogin from './components/PantallaLogin';
import Encabezado from './components/Encabezado';
import Mensaje from './components/Mensaje';
import MiToken from './components/MiToken';
import MisSolicitudes from './components/MisSolicitudes';
import TodasSolicitudes from './components/TodasSolicitudes';
import Catalogo from './components/Catalogo';
import PruebasSeguridad from './components/PruebasSeguridad';

function App() {
  return (
    <div className="app">
      <UnauthenticatedTemplate>
        <PantallaLogin />
      </UnauthenticatedTemplate>
      <AuthenticatedTemplate>
        <PanelPrincipal />
      </AuthenticatedTemplate>
    </div>
  );
}

function PanelPrincipal() {
  const { inProgress } = useMsal();
  const [claims, setClaims] = useState(null);
  const [errorToken, setErrorToken] = useState(null);
  const [pestana, setPestana] = useState('token');

  // Decodifica el ACCESS TOKEN de la API para conocer el rol (claim "roles")
  useEffect(() => {
    if (inProgress !== InteractionStatus.None) {
      return;
    }
    obtenerAccessToken()
      .then((token) => {
        setClaims(jwtDecode(token));
        setErrorToken(null);
      })
      .catch((error) => setErrorToken(textoError(error)));
  }, [inProgress]);

  const rol = obtenerRol(claims);

  // Ocultar pestañas es solo UX: el BFF vuelve a verificar el rol en cada endpoint
  const pestanas = [
    { id: 'token', titulo: 'Mi token', visible: true },
    { id: 'mias', titulo: 'Mis solicitudes', visible: true },
    { id: 'todas', titulo: 'Todas las solicitudes', visible: esOperadorOAdministrador(rol) },
    { id: 'catalogo', titulo: 'Catálogo', visible: rol === ROL_ADMINISTRADOR },
    { id: 'pruebas', titulo: 'Pruebas de seguridad', visible: true },
  ].filter((p) => p.visible);

  const activa = pestanas.some((p) => p.id === pestana) ? pestana : 'token';

  return (
    <>
      <Encabezado rol={rol} />
      <main className="contenido">
        {errorToken && <Mensaje mensaje={{ tipo: 'error', texto: `No se pudo obtener el access token. ${errorToken}` }} />}
        {claims && !rol && (
          <div className="aviso">Usuario sin rol asignado en Entra ID</div>
        )}

        <nav className="pestanas">
          {pestanas.map((p) => (
            <button
              key={p.id}
              type="button"
              className={p.id === activa ? 'pestana activa' : 'pestana'}
              onClick={() => setPestana(p.id)}
            >
              {p.titulo}
            </button>
          ))}
        </nav>

        {activa === 'token' && <MiToken />}
        {activa === 'mias' && <MisSolicitudes />}
        {activa === 'todas' && <TodasSolicitudes />}
        {activa === 'catalogo' && <Catalogo />}
        {activa === 'pruebas' && <PruebasSeguridad />}
      </main>
    </>
  );
}

export default App;
