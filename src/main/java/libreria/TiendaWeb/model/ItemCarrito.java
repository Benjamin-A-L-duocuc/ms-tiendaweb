package libreria.TiendaWeb.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "items_carrito")
public class ItemCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // idProducto: Long — external from Inventario microservice (not in scope)

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    // precioUnitario: double — external from Inventario microservice (not in scope)

    @Column(name = "carrito_id")
    private Long carritoId;
}
