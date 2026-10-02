package cl.duoc.mesatech.catalogo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "prioridades")
public class Prioridad {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 50)
	private String nombre;

	/** Nivel de prioridad: 1 = más baja. */
	@Column(nullable = false)
	private Integer nivel;

	/** Horas máximas de atención comprometidas (SLA). */
	@Column(nullable = false)
	private Integer slaHoras;

	@Column(nullable = false)
	private boolean activa = true;

	public Prioridad() {
	}

	public Prioridad(String nombre, Integer nivel, Integer slaHoras) {
		this.nombre = nombre;
		this.nivel = nivel;
		this.slaHoras = slaHoras;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Integer getNivel() {
		return nivel;
	}

	public void setNivel(Integer nivel) {
		this.nivel = nivel;
	}

	public Integer getSlaHoras() {
		return slaHoras;
	}

	public void setSlaHoras(Integer slaHoras) {
		this.slaHoras = slaHoras;
	}

	public boolean isActiva() {
		return activa;
	}

	public void setActiva(boolean activa) {
		this.activa = activa;
	}
}
