package cl.duoc.mesatech.solicitudes.model;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;


public enum EstadoSolicitud {
	CREADA,
	ASIGNADA,
	EN_PROCESO,
	RESUELTA,
	CERRADA,
	CANCELADA;

	private static final Map<EstadoSolicitud, Set<EstadoSolicitud>> TRANSICIONES = new EnumMap<>(EstadoSolicitud.class);

	static {
		TRANSICIONES.put(CREADA, EnumSet.of(ASIGNADA, CANCELADA));
		TRANSICIONES.put(ASIGNADA, EnumSet.of(EN_PROCESO, CANCELADA));
		
		TRANSICIONES.put(EN_PROCESO, EnumSet.of(RESUELTA, CANCELADA));
		TRANSICIONES.put(RESUELTA, EnumSet.of(CERRADA));
		TRANSICIONES.put(CERRADA, EnumSet.noneOf(EstadoSolicitud.class));
		TRANSICIONES.put(CANCELADA, EnumSet.noneOf(EstadoSolicitud.class));
	}

	public boolean puedePasarA(EstadoSolicitud nuevo) {
		return nuevo != null && TRANSICIONES.get(this).contains(nuevo);
	}
}
