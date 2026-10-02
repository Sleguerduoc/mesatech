package cl.duoc.mesatech.bff.dto;

import java.util.List;

public record SolicitudesV2Response(String version, int total, List<SolicitudV2Dto> solicitudes) {
}
