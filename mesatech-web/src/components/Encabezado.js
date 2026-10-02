import { useMsal } from '@azure/msal-react';
import { etiquetaRol } from '../roles';

function Encabezado({ rol }) {
  const { instance, accounts } = useMsal();
  const cuenta = accounts[0];

  const cerrarSesion = () => {
    instance.logoutRedirect({ account: instance.getActiveAccount() || cuenta });
  };

  return (
    <header className="encabezado">
      <div className="marca">MesaTech Cloud</div>
      <div className="usuario">
        <div>
          <div className="usuario-nombre">{cuenta && cuenta.name}</div>
          <div className="usuario-correo">{cuenta && cuenta.username}</div>
        </div>
        <span className={rol ? 'badge' : 'badge badge-sin-rol'}>{etiquetaRol(rol)}</span>
        <button type="button" className="btn btn-secundario" onClick={cerrarSesion}>
          Cerrar sesión
        </button>
      </div>
    </header>
  );
}

export default Encabezado;
