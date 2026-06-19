package farmacia;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Accumulators.avg;
import static com.mongodb.client.model.Accumulators.first;
import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.sort;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lte;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Sorts.orderBy;
import static com.mongodb.client.model.Aggregates.unwind;

public class Consultas {

    private static final String URI = "mongodb+srv://josemanuelruival_db_user:"
            + System.getenv("MONGO_PASSWORD")
            + "@cluster0.rnvm0cw.mongodb.net/?appName=Cluster0";

    public static void main(String[] args) {

        String password = System.getenv("MONGO_PASSWORD");
        if (password == null) {
            System.err.println("ERROR: La variable de entorno MONGO_PASSWORD no está definida.");
            return;
        }

        String desde = "2026-01-01";
        String hasta = "2026-03-31";

        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase("farmacia");
            MongoCollection<Document> col = db.getCollection("ventas");

            System.out.println("=".repeat(60));
            System.out.println("PERÍODO: " + desde + " → " + hasta);
            System.out.println("=".repeat(60));

            detalleVentas(col, desde, hasta);
            totalesPorCadena(col, desde, hasta);
            totalesPorSucursal(col, desde, hasta);
            rankingClientesCadenaPorMonto(col, desde, hasta);
            rankingClientesSucursalPorMonto(col, desde, hasta);
            rankingClientesCadenaPorCantidadCompras(col, desde, hasta);
            rankingClientesSucursalPorCantidadCompras(col, desde, hasta);
        }
    }

    static void detalleVentas(MongoCollection<Document> col, String desde, String hasta) {
        System.out.println("\n── DETALLE DE VENTAS ──────────────────────────────────");

        Bson filtro = and(gte("fecha", desde), lte("fecha", hasta));

        for (Document venta : col.find(filtro).sort(ascending("fecha"))) {
            Document cliente = (Document) venta.get("cliente");
            Document sucursal = (Document) venta.get("sucursal");

            System.out.printf("%n  [%s] Ticket #%-4d | %-18s | %s %s | %s | $%.2f%n",
                    venta.getString("fecha"),
                    venta.getInteger("nroTicket"),
                    venta.getString("formaPago"),
                    cliente.getString("nombre"),
                    cliente.getString("apellido"),
                    sucursal.getString("localidad"),
                    venta.getDouble("total"));

            List<Document> detalle = venta.getList("detalle", Document.class);
            for (Document item : detalle) {
                Document producto = (Document) item.get("producto");
                System.out.printf("       %-30s x%-2d  $%.2f%n",
                        producto.getString("descripcion"),
                        item.getInteger("cantidad"),
                        item.getDouble("totalProducto"));
            }
        }
    }

    static void totalesPorCadena(MongoCollection<Document> col, String desde, String hasta) {
        System.out.println("\n── TOTAL CADENA COMPLETA ──────────────────────────────");

        List<Bson> pipeline = Arrays.asList(
                match(and(gte("fecha", desde), lte("fecha", hasta))),
                group(null,
                        sum("cantidadVentas", 1),
                        sum("totalRecaudado", "$total"),
                        avg("ticketPromedio", "$total")));

        Document r = col.aggregate(pipeline).first();
        if (r != null) {
            System.out.printf("  Ventas:          %d%n", r.getInteger("cantidadVentas"));
            System.out.printf("  Total recaudado: $%.2f%n", r.getDouble("totalRecaudado"));
            System.out.printf("  Ticket promedio: $%.2f%n", r.getDouble("ticketPromedio"));
        }
    }

    static void totalesPorSucursal(MongoCollection<Document> col, String desde, String hasta) {
        System.out.println("\n── TOTALES POR SUCURSAL ───────────────────────────────");

        List<Bson> pipeline = Arrays.asList(
                match(and(gte("fecha", desde), lte("fecha", hasta))),
                group("$sucursal.idSucursal",
                        first("localidad", "$sucursal.localidad"),
                        first("provincia", "$sucursal.provincia"),
                        sum("cantidadVentas", 1),
                        sum("totalRecaudado", "$total"),
                        avg("ticketPromedio", "$total")),
                sort(ascending("_id")));

        for (Document suc : col.aggregate(pipeline)) {
            System.out.printf("%n  Sucursal %d – %s (%s)%n",
                    suc.getInteger("_id"),
                    suc.getString("localidad"),
                    suc.getString("provincia"));
            System.out.printf("    Ventas:          %d%n", suc.getInteger("cantidadVentas"));
            System.out.printf("    Total recaudado: $%.2f%n", suc.getDouble("totalRecaudado"));
            System.out.printf("    Ticket promedio: $%.2f%n", suc.getDouble("ticketPromedio"));
        }
    }

    static void rankingClientesCadenaPorMonto(MongoCollection<Document> col, String desde, String hasta) {
        System.out.println("\n── RANKING DE CLIENTES POR MONTO (CADENA COMPLETA) ──────────────");

        List<Bson> pipeline = Arrays.asList(
                match(and(gte("fecha", desde), lte("fecha", hasta))),
                group("$cliente.idCliente",
                        first("nombre", "$cliente.nombre"),
                        first("apellido", "$cliente.apellido"),
                        sum("montoTotal", "$total"),
                        sum("cantidadCompras", 1)),
                sort(descending("montoTotal")));

        int puesto = 1;

        for (Document cliente : col.aggregate(pipeline)) {
            System.out.printf(
                    "%d) %s %s | Compras: %d | Total: $%.2f%n",
                    puesto++,
                    cliente.getString("nombre"),
                    cliente.getString("apellido"),
                    cliente.getInteger("cantidadCompras"),
                    cliente.getDouble("montoTotal"));
        }
    }

    static void rankingClientesSucursalPorMonto(MongoCollection<Document> col, String desde, String hasta) {
        System.out.println("\n── RANKING DE CLIENTES PORMONTO (SUCURSALES) ───────────────────");

        List<Bson> pipeline = Arrays.asList(
                match(and(gte("fecha", desde), lte("fecha", hasta))),
                group(
                        new Document("sucursal", "$sucursal.idSucursal")
                                .append("cliente", "$cliente.idCliente"),

                        first("localidad", "$sucursal.localidad"),
                        first("nombre", "$cliente.nombre"),
                        first("apellido", "$cliente.apellido"),

                        sum("montoTotal", "$total"),
                        sum("cantidadCompras", 1)),
                sort(orderBy(
                        ascending("_id.sucursal"),
                        descending("montoTotal"))));

        Integer sucursalActual = null;

        for (Document cliente : col.aggregate(pipeline)) {

            Document id = (Document) cliente.get("_id");
            Integer sucursal = id.getInteger("sucursal");

            if (!sucursal.equals(sucursalActual)) {
                sucursalActual = sucursal;

                System.out.printf(
                        "%nSucursal %d - %s%n",
                        sucursal,
                        cliente.getString("localidad"));
            }

            System.out.printf(
                    "   %s %s | Compras: %d | Total: $%.2f%n",
                    cliente.getString("nombre"),
                    cliente.getString("apellido"),
                    cliente.getInteger("cantidadCompras"),
                    cliente.getDouble("montoTotal"));
        }
    }

    static void rankingClientesCadenaPorCantidadCompras(MongoCollection<Document> col, String desde, String hasta) {
        System.out.println("\n── RANKING DE CLIENTES POR CANTIDAD DE COMPRAS (CADENA COMPLETA) ──────────────");

        List<Bson> pipeline = Arrays.asList(
                match(and(gte("fecha", desde), lte("fecha", hasta))),
                group("$cliente.idCliente",
                        first("nombre", "$cliente.nombre"),
                        first("apellido", "$cliente.apellido"),
                        sum("montoTotal", "$total"),
                        sum("cantidadCompras", 1)),
                sort(descending("montoTotal")));

        int puesto = 1;

        for (Document cliente : col.aggregate(pipeline)) {
            System.out.printf(
                    "%d) %s %s | Compras: %d | Total: $%.2f%n",
                    puesto++,
                    cliente.getString("nombre"),
                    cliente.getString("apellido"),
                    cliente.getInteger("cantidadCompras"),
                    cliente.getDouble("montoTotal"));
        }
    }

    static void rankingClientesSucursalPorCantidadCompras(MongoCollection<Document> col, String desde, String hasta) {

        System.out.println("\n── RANKING CLIENTES POR CANTIDAD DE COMPRAS (SUCURSALES) ──────────────────────");

        List<Bson> pipeline = Arrays.asList(

                match(and(
                        gte("fecha", desde),
                        lte("fecha", hasta))),

                unwind("$detalle"),

                group(
                        new Document("sucursal", "$sucursal.idSucursal")
                                .append("cliente", "$cliente.idCliente"),

                        first("localidad", "$sucursal.localidad"),
                        first("nombre", "$cliente.nombre"),
                        first("apellido", "$cliente.apellido"),

                        sum("cantidadVendida", "$detalle.cantidad")),

                sort(orderBy(
                        ascending("_id.sucursal"),
                        descending("cantidadVendida"))));

        Integer sucursalActual = null;

        for (Document cliente : col.aggregate(pipeline)) {

            Document id = (Document) cliente.get("_id");

            Integer sucursal = id.getInteger("sucursal");

            if (!sucursal.equals(sucursalActual)) {

                sucursalActual = sucursal;

                System.out.printf(
                        "%nSucursal %d - %s%n",
                        sucursal,
                        cliente.getString("localidad"));
            }

            System.out.printf(
                    "   %s %s | Productos vendidos: %d%n",
                    cliente.getString("nombre"),
                    cliente.getString("apellido"),
                    cliente.getInteger("cantidadVendida"));
        }
    }
}
