# TiendaWeb — Tienda en Linea

## Que es

La cara visible de la libreria para el cliente. Es el servicio que soporta la tienda virtual: desde navegar el catalogo, pasar productos por el carrito, hacer el pedido, hasta dejar una resena. No almacena productos ni usuarios — los consulta a otros servicios, y guarda solo lo que le compete: carritos, ordenes, y resenas.

## Flujo completo de compra

### 1. Navegar el catalogo
El cliente busca libros. TiendaWeb consulta el **Inventario** (:8094) y muestra los resultados, incluyendo el stock total disponible.

### 2. Agregar al carrito
Se validan dos cosas: que la sesion sea valida (via **Login** :8092) y que haya stock suficiente (via **Inventario**). Si el producto ya esta en el carrito, se suma la cantidad. El carrito guarda el precio al momento de agregarlo — asi si el precio cambia despues, el cliente paga lo que vio.

### 3. Crear la orden
El carrito se "convierte" en orden. Se calcula el subtotal y se agrega 19% de IVA chileno. La orden queda en estado `PENDIENTE_PAGO` y el carrito pasa a `convertido` (ya no se puede seguir agregando productos).

### 4. Confirmar la venta
Se verifica todo el stock de nuevo, se reservan los productos en **Inventario**, y se crea la venta en **Ventas** (:8087). Si se aplica un descuento, se pasa al servicio de Ventas para que lo valide y aplique.

### 5. Rastrear el envio
Una vez que el pedido esta `ENVIADA`, el cliente puede consultar el estado del envio via **Envios** (:8084).

## El sistema de resenas

Los clientes pueden calificar y comentar productos, pero con reglas:
- Solo pueden dejar resenas si tienen al menos un pedido en estado `ENTREGADA`.
- Un cliente solo puede dejar **una resena por producto**.
- Los comentarios deben tener entre 20 y 1000 caracteres.
- Palabras como "spam", "estafa", o "fraude" estan prohibidas.

## Integraciones

Este es el microservicio que mas servicios consulta. Usa **5 clientes HTTP** distintos:

| Servicio | Para que |
|----------|----------|
| Login (:8092) | Validar que el usuario tiene sesion activa |
| Inventario (:8094) | Catalogo, precios, stock, reservas |
| Ventas (:8087) | Descuentos, crear venta |
| Envios (:8084) | Rastreo de envios |
| RegistroUsuarios (:8093) | Perfil del usuario |

## Ejecutar

```cmd
cd TiendaWeb
.\mvnw.cmd spring-boot:run
```

Puerto: **8085** | DB: `Tienda_Web`

## Endpoints principales

**Catalogo:** `GET /api/v1/tienda/catalogo`, `GET /api/v1/tienda/productos/{id}`

**Carrito:** `POST /api/v1/tienda/carrito/agregar`, `PUT /api/v1/tienda/carrito/modificar`, `DELETE /api/v1/tienda/carrito/items/{id}`, `GET /api/v1/tienda/carrito`

**Ordenes:** `POST /api/v1/tienda/ordenes` (crear), `POST /api/v1/tienda/ordenes/{id}/venta` (confirmar compra), `GET /api/v1/tienda/pedidos`

**Resenas:** `POST /api/v1/tienda/resenas`, `GET /api/v1/tienda/productos/{id}/resenas`
