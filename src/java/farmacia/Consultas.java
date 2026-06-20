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
            System.out.println("✅ ¡Conexión exitosa a MongoDB Atlas confirmada!");
            MongoCollection<Document> col = db.getCollection("ventas");

            System.out.println("=".repeat(60));
            System.out.println("PERÍODO: " + desde + " → " + hasta);
            System.out.println("=".repeat(60));

            detalleVentas(col, desde, hasta);
            totalesPorCadena(col, desde, hasta);
            totalesPorSucursal(col, desde, hasta);

            rankingPorMonto(col, desde, hasta);
            rankingPorCantidad(col, desde, hasta);
        }
    }

    static void detalleVentas(MongoCollection<Document> col, String desde, String hasta) {
        System.out.println("\n── DETALLE DE VENTAS ──────────────────────────────────");

        Bson filtro = and(gte("fecha", desde), lte("fecha", hasta));

        for (Document venta : col.find(filtro).sort(ascending("fecha"))) {
            Document cliente  = (Document) venta.get("cliente");
            Document sucursal = (Document) venta.get("sucursal");

            System.out.printf("%n  [%s] Ticket #%-4d | %-18s | %s %s | %s | $%.2f%n",
                venta.getString("fecha"),
                venta.getInteger("nroTicket"),
                venta.getString("formaPago"),
                cliente.getString("nombre"),
                cliente.getString("apellido"),
                sucursal.getString("localidad"),
                venta.getDouble("total")
            );

            List<Document> detalle = venta.getList("detalle", Document.class);
            for (Document item : detalle) {
                Document producto = (Document) item.get("producto");
                System.out.printf("       %-30s x%-2d  $%.2f%n",
                    producto.getString("descripcion"),
                    item.getInteger("cantidad"),
                    item.getDouble("totalProducto")
                );
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
                avg("ticketPromedio", "$total")
            )
        );

        Document r = col.aggregate(pipeline).first();
        if (r != null) {
            System.out.printf("  Ventas:          %d%n",    r.getInteger("cantidadVentas"));
            System.out.printf("  Total recaudado: $%.2f%n", r.getDouble("totalRecaudado"));
            System.out.printf("  Ticket promedio: $%.2f%n", r.getDouble("ticketPromedio"));
        }
    }

    static void totalesPorSucursal(MongoCollection<Document> col, String desde, String hasta) {
        System.out.println("\n── TOTALES POR SUCURSAL ───────────────────────────────");

        List<Bson> pipeline = Arrays.asList(
            match(and(gte("fecha", desde), lte("fecha", hasta))),
            group("$sucursal.idSucursal",
                first("localidad",    "$sucursal.localidad"),
                first("provincia",    "$sucursal.provincia"),
                sum("cantidadVentas", 1),
                sum("totalRecaudado", "$total"),
                avg("ticketPromedio", "$total")
            ),
            sort(ascending("_id"))
        );

        for (Document suc : col.aggregate(pipeline)) {
            System.out.printf("%n  Sucursal %d – %s (%s)%n",
                suc.getInteger("_id"),
                suc.getString("localidad"),
                suc.getString("provincia")
            );
            System.out.printf("    Ventas:          %d%n",    suc.getInteger("cantidadVentas"));
            System.out.printf("    Total recaudado: $%.2f%n", suc.getDouble("totalRecaudado"));
            System.out.printf("    Ticket promedio: $%.2f%n", suc.getDouble("ticketPromedio"));
        }
    }

    static void rankingPorMonto(MongoCollection<Document> col, String desde, String hasta) {
        System.out.println("\n── 5. RANKING DE PRODUCTOS POR MONTO VENDIDO ──────────────────");

        // 5A: Total de la cadena completa
        System.out.println("\n  >> Total Cadena Completa:");
        List<Bson> pipelineCadena = Arrays.asList(
            match(and(gte("fecha", desde), lte("fecha", hasta))),
            unwind("$detalle"),
            group("$detalle.producto.descripcion",
                sum("montoTotal", "$detalle.totalProducto")
            ),
            sort(descending("montoTotal"))
        );

        for (Document doc : col.aggregate(pipelineCadena)) {
            System.out.printf("     %-25s | $%.2f%n", doc.getString("_id"), doc.getDouble("montoTotal"));
        }

        // 5B: Agrupado por Sucursal
        System.out.println("\n  >> Por Sucursal:");
        List<Bson> pipelineSucursal = Arrays.asList(
            match(and(gte("fecha", desde), lte("fecha", hasta))),
            unwind("$detalle"),
            // Agrupamos por un objeto compuesto: {idSucursal, producto}
            group(new Document("idSucursal", "$sucursal.idSucursal").append("producto", "$detalle.producto.descripcion"),
                sum("montoTotal", "$detalle.totalProducto")
            ),
            // Ordenamos por sucursal (ascendente) y luego por monto (descendente)
            sort(new Document("_id.idSucursal", 1).append("montoTotal", -1))
        );

        for (Document doc : col.aggregate(pipelineSucursal)) {
            Document id = (Document) doc.get("_id");
            System.out.printf("     Sucursal %d | %-20s | $%.2f%n", 
                id.getInteger("idSucursal"), id.getString("producto"), doc.getDouble("montoTotal"));
        }
    }

    static void rankingPorCantidad(MongoCollection<Document> col, String desde, String hasta) {
        System.out.println("\n── 6. RANKING DE PRODUCTOS POR CANTIDAD VENDIDA ───────────────");

        // 6A: Total de la cadena completa
        System.out.println("\n  >> Total Cadena Completa:");
        List<Bson> pipelineCadena = Arrays.asList(
            match(and(gte("fecha", desde), lte("fecha", hasta))),
            unwind("$detalle"),
            group("$detalle.producto.descripcion",
                sum("cantidadTotal", "$detalle.cantidad")
            ),
            sort(descending("cantidadTotal"))
        );

        for (Document doc : col.aggregate(pipelineCadena)) {
            System.out.printf("     %-25s | %d unid.%n", doc.getString("_id"), doc.getInteger("cantidadTotal"));
        }

        // 6B: Agrupado por Sucursal
        System.out.println("\n  >> Por Sucursal:");
        List<Bson> pipelineSucursal = Arrays.asList(
            match(and(gte("fecha", desde), lte("fecha", hasta))),
            unwind("$detalle"),
            group(new Document("idSucursal", "$sucursal.idSucursal").append("producto", "$detalle.producto.descripcion"),
                sum("cantidadTotal", "$detalle.cantidad")
            ),
            sort(new Document("_id.idSucursal", 1).append("cantidadTotal", -1))
        );

        for (Document doc : col.aggregate(pipelineSucursal)) {
            Document id = (Document) doc.get("_id");
            System.out.printf("     Sucursal %d | %-20s | %d unid.%n", 
                id.getInteger("idSucursal"), id.getString("producto"), doc.getInteger("cantidadTotal"));
        }
    }
}
