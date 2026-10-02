package cl.duoc.mesatech.solicitudes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes")
public class Solicitud {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 150)
	private String titulo;

	@Column(nullable = false, length = 2000)
	private String descripcion;

	@Column(nullable = false)
	private Long categoriaId;

	@Column(nullable = false)
	private Long prioridadId;

	@Column(nullable = false, length = 100)
	private String solicitanteOid;

	@Column(length = 200)
	private String solicitanteEmail;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoSolicitud estado;

	@Column(nullable = false)
	private LocalDateTime fechaCreacion;

	private LocalDateTime fechaActualizacion;

	@Column(length = 200)
	private String operadorAsignado;

	@Column(length = 4000)
	private String detalleAtencion;

	@PrePersist
	protected void alCrear() {
		this.fechaCreacion = LocalDateTime.now();
		this.estado = EstadoSolicitud.CREADA;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Long getCategoriaId() {
		return categoriaId;
	}

	public void setCategoriaId(Long categoriaId) {
		this.categoriaId = categoriaId;
	}

	public Long getPrioridadId() {
		return prioridadId;
	}

	public void setPrioridadId(Long prioridadId) {
		this.prioridadId = prioridadId;
	}

	public String getSolicitanteOid() {
		return solicitanteOid;
	}

	public void setSolicitanteOid(String solicitanteOid) {
		this.solicitanteOid = solicitanteOid;
	}

	public String getSolicitanteEmail() {
		return solicitanteEmail;
	}

	public void setSolicitanteEmail(String solicitanteEmail) {
		this.solicitanteEmail = solicitanteEmail;
	}

	public EstadoSolicitud getEstado() {
		return estado;
	}

	public void setEstado(EstadoSolicitud estado) {
		this.estado = estado;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public LocalDateTime getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}

	public String getOperadorAsignado() {
		return operadorAsignado;
	}

	public void setOperadorAsignado(String operadorAsignado) {
		this.operadorAsignado = operadorAsignado;
	}

	public String getDetalleAtencion() {
		return detalleAtencion;
	}

	public void setDetalleAtencion(String detalleAtencion) {
		this.detalleAtencion = detalleAtencion;
	}
}
