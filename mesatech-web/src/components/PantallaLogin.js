import { useMsal } from '@azure/msal-react';
import { loginRequest } from '../authConfig';

function PantallaLogin() {
  const { instance } = useMsal();

  const iniciarSesion = () => {
    instance.loginRedirect(loginRequest);
  };

  return (
    <div className="login">
      <h1>MesaTech Cloud</h1>
      <p>No has iniciado sesión</p>
      <button type="button" className="btn" onClick={iniciarSesion}>
        Iniciar sesión con Microsoft
      </button>
    </div>
  );
}

export default PantallaLogin;
