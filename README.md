# TiendaWeb

API de la tienda en linea. Carrito de compras, ordenes de compra, y resenas de productos.

## Puerto

**8085** | DB: `Tienda_Web`

## Endpoints

### Carrito (`/api/v1/tienda`)

| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| GET | `/tienda/catalogo` | Ver catalogo |
| GET | `/tienda/productos/{id}` | Ver producto |
| POST | `/tienda/carrito/agregar` | Agregar item al carrito |
| PUT | `/tienda/carrito/modificar` | Modificar item |
| DELETE | `/tienda/carrito/items/{idItem}` | Quitar item |
| GET | `/tienda/carrito` | Ver carrito actual |
| POST | `/tienda/carrito` | Crear carrito |
| GET | `/tienda/carritos` | Listar carritos |
| GET | `/tienda/carritos/{id}` | Ver carrito por ID |
| PUT | `/tienda/carritos/{id}` | Actualizar carrito |
| DELETE | `/tienda/carritos/{id}` | Eliminar carrito |

### Ordenes (`/api/v1/tienda`)

| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| POST | `/tienda/ordenes` | Crear orden |
| POST | `/tienda/ordenes/{id}/venta` | Confirmar venta |
| GET | `/tienda/ordenes` | Listar ordenes |
| GET | `/tienda/ordenes/{id}` | Ver orden |
| PUT | `/tienda/ordenes/{id}` | Actualizar orden |
| DELETE | `/tienda/ordenes/{id}` | Eliminar orden |
| GET | `/tienda/ordenes/{id}/envio` | Ver envio de una orden |
| GET | `/tienda/pedidos` | Ver pedidos |
| GET | `/tienda/perfil` | Ver perfil |
| GET | `/tienda/soporte` | Ver soporte |

### Resenas (`/api/v1/tienda`)

| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| POST | `/tienda/resenas` | Crear resena |
| PUT | `/tienda/resenas` | Actualizar resena |
| GET | `/tienda/productos/{idProducto}/resenas` | Resenas de un producto |
| GET | `/tienda/resenas` | Listar resenas |
| GET | `/tienda/resenas/{id}` | Ver resena |
| DELETE | `/tienda/resenas/{id}` | Eliminar resena |

## Ejecucion

```cmd
cd TiendaWeb
.\mvnw.cmd spring-boot:run
```
