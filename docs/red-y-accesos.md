# Red y accesos de MesaTech Cloud (EP2)

Todos los recursos están en la VPC por defecto de us-east-1. Las aplicaciones se comunican entre sí por IP privada.

## Matriz de reglas de entrada (security groups)

| Destino | Puerto TCP | Origen autorizado | Propósito |
|---|---|---|---|
| EC2 A (sg-0360b3c361e88927a) | 8080 | Prefix list pl-03ce40356ec6dc0af (rangos API_GATEWAY de us-east-1) | BFF, integración HTTP del API Gateway (conservada de la EP1) |
| EC2 A | 22 | 201.188.66.77/32 | SSH desde la IP administrativa |
| EC2 B (sg-0a79928d2b0d7972a) | 8081 | sg-0360b3c361e88927a (EC2 A) | ms-solicitudes, llamado solo desde el BFF |
| EC2 B | 22 | 201.188.66.77/32 | SSH desde la IP administrativa |
| EC2 C (sg-04cc53da94fc7bf00) | 5672 | sg-0360b3c361e88927a (EC2 A) | AMQP del publicador (ms-catalogo) por red privada |
| EC2 C | 5672 | sg-0a79928d2b0d7972a (EC2 B) | AMQP del consumidor (ms-solicitudes) por red privada |
| EC2 C | 15672 | 201.188.66.77/32 | Interfaz de administración de RabbitMQ |
| EC2 C | 22 | 201.188.66.77/32 | SSH desde la IP administrativa |
| RDS Catálogo (sg-070d48aade04e8fe6) | 3306 | sg-0360b3c361e88927a (EC2 A) | MySQL, solo ms-catalogo |
| RDS Solicitudes (sg-0a6606097b6a10843) | 3306 | sg-0a79928d2b0d7972a (EC2 B) | MySQL, solo ms-solicitudes |

Ninguna regla permite acceso mundial (0.0.0.0/0). Ambas RDS están creadas sin acceso público. ms-catalogo (puerto 8082) no tiene regla de entrada: solo lo llama el BFF dentro de la misma EC2 A.

## Puerto 8080 del BFF

El API Gateway HTTP API se integra con el BFF por la IP pública (Elastic IP 54.227.218.48) de la EC2 A, igual que en la EP1. API Gateway no tiene IPs fijas por API, así que la regla usa una prefix list administrada por el equipo con los 16 rangos que AWS publica para el servicio API_GATEWAY en us-east-1 (https://ip-ranges.amazonaws.com/ip-ranges.json, filtro service=API_GATEWAY y region=us-east-1, consultado el 2026-11-05).

Comprobación (evidencias 155 y 156): la aplicación sigue funcionando a través del API Gateway, y un acceso directo al 8080 desde una IP fuera de esos rangos no recibe respuesta. Además, el BFF valida el JWT por su cuenta, así que aunque se llegara al puerto sin pasar por el API Gateway, una petición sin token recibe 401.

Si AWS cambia esos rangos, hay que actualizar la prefix list con la misma consulta.

## Puertos del contenedor RabbitMQ

compose.yaml publica 5672:5672 y 15672:15672 en la EC2 C. La publicación del puerto en Docker y la regla del security group son controles distintos: Docker abre el puerto en la instancia y el security group decide quién puede llegar a él.

## Firewall del sistema operativo

ufw viene inactivo por defecto en Ubuntu 24.04 (verificado en la EC2 C, evidencia 031). El filtrado de red se hace solo con los security groups.

## Administración por SSH

El SSH se permite solo desde la IP administrativa /32 con la llave del laboratorio (vockey). Durante la instalación se usó también la consola EC2 Instance Connect; la regla de ese servicio (prefix list pl-0e4bcff02b13bef1e) se eliminó al terminar las pruebas para dejar la matriz igual a la pedida (evidencias 157 a 159).

Para administrar desde el navegador se usa AWS Systems Manager Session Manager (EC2, Connect, SSM Session Manager). No usa SSH ni requiere ninguna regla de entrada: el agente SSM de cada instancia abre la conexión hacia AWS y la autorización se hace con IAM, usando el rol LabRole que ya tienen las tres EC2. Las tres aparecen Online en Fleet Manager (evidencias 160 y 161). La sesión entra como ssm-user; para trabajar como el usuario de la instalación se usa `sudo -iu ubuntu`.

Si cambia la IP pública del administrador, hay que actualizar las reglas de 22 y 15672. Las Elastic IP no evitan esto, porque fijan la IP de la instancia (destino) y no la del administrador (origen).
