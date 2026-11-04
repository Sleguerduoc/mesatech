package cl.duoc.mesatech.solicitudes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Copia local de una categoría recibida por evento. La clave es el categoriaId original de Catálogo
 * (no se autogenera), así el registro se relaciona directamente con su origen.
 */
@Entity
@Table(name = "categorias_recibidas")
public class CategoriaLocal {

	@Id
	@Column(name = "categoria_id")
	private Long categoriaId;

	@Column(nullable = false, length = 100)
	private String nombre;

	@Column(nullable = false, length = 36)
	private String eventId;

	@Column(nullable = false, length = 40)
	private String fechaEvento;

	@Column(nullable = false)
	private LocalDateTime fechaRecepcion;

	public CategoriaLocal() {
	}

	public CategoriaLocal(Long categoriaId, String nombre, String eventId, String fechaEvento,
			LocalDateTime fechaRecepcion) {
		this.categoriaId = categoriaId;
		this.nombre = nombre;
		this.eventId = eventId;
		this.fechaEvento = fechaEvento;
		this.fechaRecepcion = fechaRecepcion;
	}

	public Long getCategoriaId() {
		return categoriaId;
	}

	public void setCategoriaId(Long categoriaId) {
		this.categoriaId = categoriaId;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getFechaEvento() {
		return fechaEvento;
	}

	public void setFechaEvento(String fechaEvento) {
		this.fechaEvento = fechaEvento;
	}

	public LocalDateTime getFechaRecepcion() {
		return fechaRecepcion;
	}

	public void setFechaRecepcion(LocalDateTime fechaRecepcion) {
		this.fechaRecepcion = fechaRecepcion;
	}
}
