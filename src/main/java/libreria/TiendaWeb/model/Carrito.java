package libreria.TiendaWeb.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import libreria.TiendaWeb.model.enums.EstadoCarrito;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "carritos")
public class Carrito {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "id_usuario", nullable = false)
	private Long idUsuario;

	@Column(name = "fecha_creacion", nullable = false)
	private LocalDateTime fechaCreacion;

	@Column(name = "fecha_actualizacion")
	private LocalDateTime fechaActualizacion;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false)
	private EstadoCarrito estado;

	@OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ItemCarrito> items;
}
