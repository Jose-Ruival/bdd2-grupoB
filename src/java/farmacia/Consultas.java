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
import static com.mongodb.client.model.Aggregates.unwind;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lte;
import static com.mongodb.client.model.Filters.ne;
import static com.mongodb.client.model.Sorts.ascending;

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

        String desde = "2026-05-01";
        String hasta = "2026-05-31";

        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase("farmacia");
            MongoCollection<Document> col = db.getCollection("ventas");

            System.out.println("=".repeat(60));
            System.out.println("PERÍODO: " + desde + " → " + hasta);
            System.out.println("=".repeat(60));

            detalleVentas(col, desde, hasta);
            totalesPorCadena(col, desde, hasta);
            totalesPorSucursal(col, desde, hasta);
            ventasPorTipoCliente(col, desde, hasta);
            ventasPorCategoria(col, desde, hasta);
            reporteCobranza(col, desde, hasta);
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

    static void ventasPorTipoCliente(MongoCollection<Document> col, String desde, String hasta) {
        System.out.println("\n── VENTAS OBRA SOCIAL / PRIVADO ───────────────────────────");

        Bson filtroFecha = and(gte("fecha", desde), lte("fecha", hasta));

        String[][] tipos = {
            {"OBRA_SOCIAL", "OBRA SOCIAL"},
            {"PRIVADO",     "PRIVADO    "}
        };

        for (String[] tipo : tipos) {
            String clave = tipo[0];
            String label = tipo[1];

            Bson filtroPago = clave.equals("OBRA_SOCIAL")
                ? and(filtroFecha, eq("formaPago", "OBRA_SOCIAL"))
                : and(filtroFecha, ne("formaPago", "OBRA_SOCIAL"));

            System.out.println("\n  [" + label + "]");

            for (Document venta : col.find(filtroPago).sort(ascending("fecha"))) {
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
                for (Document item : venta.getList("detalle", Document.class)) {
                    Document producto = (Document) item.get("producto");
                    System.out.printf("       %-30s x%-2d  $%.2f%n",
                        producto.getString("descripcion"),
                        item.getInteger("cantidad"),
                        item.getDouble("totalProducto")
                    );
                }
            }

            List<Bson> pipelineCadena = Arrays.asList(
                match(filtroPago),
                group(null,
                    sum("cantidadVentas", 1),
                    sum("totalRecaudado", "$total"),
                    avg("ticketPromedio", "$total")
                )
            );
            Document r = col.aggregate(pipelineCadena).first();
            if (r != null) {
                System.out.printf("%n  Cadena  →  Ventas: %d | Total: $%.2f | Promedio: $%.2f%n",
                    r.getInteger("cantidadVentas"),
                    r.getDouble("totalRecaudado"),
                    r.getDouble("ticketPromedio")
                );
            }

            List<Bson> pipelineSuc = Arrays.asList(
                match(filtroPago),
                group("$sucursal.idSucursal",
                    first("localidad",    "$sucursal.localidad"),
                    sum("cantidadVentas", 1),
                    sum("totalRecaudado", "$total"),
                    avg("ticketPromedio", "$total")
                ),
                sort(ascending("_id"))
            );
            for (Document suc : col.aggregate(pipelineSuc)) {
                System.out.printf("  Suc. %d (%s)  →  Ventas: %d | Total: $%.2f | Promedio: $%.2f%n",
                    suc.getInteger("_id"),
                    suc.getString("localidad"),
                    suc.getInteger("cantidadVentas"),
                    suc.getDouble("totalRecaudado"),
                    suc.getDouble("ticketPromedio")
                );
            }
        }
    }

    static void ventasPorCategoria(MongoCollection<Document> col, String desde, String hasta) {
        System.out.println("\n── VENTAS POR CATEGORÍA (FARMACIA / PERFUMERÍA) ───────────");

        Bson filtroFecha = and(gte("fecha", desde), lte("fecha", hasta));

        String[][] categorias = {
            {"MEDICAMENTO", "FARMACIA (Medicamentos)"},
            {"PERFUMERIA",  "PERFUMERÍA             "}
        };

        for (String[] cat : categorias) {
            String tipo  = cat[0];
            String label = cat[1];

            System.out.println("\n  [" + label + "]");

            List<Bson> detallePipeline = Arrays.asList(
                match(filtroFecha),
                unwind("$detalle"),
                match(eq("detalle.producto.tipo", tipo)),
                sort(ascending("fecha"))
            );
            for (Document row : col.aggregate(detallePipeline)) {
                Document sucursal = (Document) row.get("sucursal");
                Document item     = (Document) row.get("detalle");
                Document producto = (Document) item.get("producto");
                System.out.printf("  [%s] %-30s x%-2d  $%.2f | %s%n",
                    row.getString("fecha"),
                    producto.getString("descripcion"),
                    item.getInteger("cantidad"),
                    item.getDouble("totalProducto"),
                    sucursal.getString("localidad")
                );
            }

            List<Bson> pipelineCadena = Arrays.asList(
                match(filtroFecha),
                unwind("$detalle"),
                match(eq("detalle.producto.tipo", tipo)),
                group(null,
                    sum("cantUnidades",   "$detalle.cantidad"),
                    sum("totalRecaudado", "$detalle.totalProducto")
                )
            );
            Document r = col.aggregate(pipelineCadena).first();
            if (r != null) {
                System.out.printf("%n  Cadena  →  Unidades: %d | Total: $%.2f%n",
                    r.getInteger("cantUnidades"),
                    r.getDouble("totalRecaudado")
                );
            }

            List<Bson> pipelineSuc = Arrays.asList(
                match(filtroFecha),
                unwind("$detalle"),
                match(eq("detalle.producto.tipo", tipo)),
                group("$sucursal.idSucursal",
                    first("localidad",    "$sucursal.localidad"),
                    sum("cantUnidades",   "$detalle.cantidad"),
                    sum("totalRecaudado", "$detalle.totalProducto")
                ),
                sort(ascending("_id"))
            );
            for (Document suc : col.aggregate(pipelineSuc)) {
                System.out.printf("  Suc. %d (%s)  →  Unidades: %d | Total: $%.2f%n",
                    suc.getInteger("_id"),
                    suc.getString("localidad"),
                    suc.getInteger("cantUnidades"),
                    suc.getDouble("totalRecaudado")
                );
            }
        }
    }

    static void reporteCobranza(MongoCollection<Document> col, String desde, String hasta) {
        System.out.println("\n── REPORTE DE COBRANZA (" + desde + " → " + hasta + ") ──────────────────");

        Bson filtroFecha = and(gte("fecha", desde), lte("fecha", hasta));

        // Total cadena completa
        List<Bson> pipelineCadena = Arrays.asList(
            match(filtroFecha),
            group(null,
                sum("cantidadVentas", 1),
                sum("totalCobranza",  "$total"),
                avg("ticketPromedio", "$total")
            )
        );

        Document cadena = col.aggregate(pipelineCadena).first();
        System.out.println("\n  CADENA COMPLETA");
        if (cadena != null) {
            System.out.printf("    Cantidad de ventas: %d%n",    cadena.getInteger("cantidadVentas"));
            System.out.printf("    Total cobranza:     $%.2f%n", cadena.getDouble("totalCobranza"));
            System.out.printf("    Ticket promedio:    $%.2f%n", cadena.getDouble("ticketPromedio"));
        } else {
            System.out.println("    Sin ventas en el período.");
        }

        // Cobranza agrupada por sucursal
        List<Bson> pipelineSuc = Arrays.asList(
            match(filtroFecha),
            group("$sucursal.idSucursal",
                first("localidad",      "$sucursal.localidad"),
                first("provincia",      "$sucursal.provincia"),
                sum("cantidadVentas",   1),
                sum("totalCobranza",    "$total"),
                avg("ticketPromedio",   "$total")
            ),
            sort(ascending("_id"))
        );

        System.out.println("\n  POR SUCURSAL");
        boolean haySucursales = false;
        for (Document suc : col.aggregate(pipelineSuc)) {
            haySucursales = true;
            System.out.printf("%n    Sucursal %d – %s (%s)%n",
                suc.getInteger("_id"),
                suc.getString("localidad"),
                suc.getString("provincia")
            );
            System.out.printf("      Cantidad de ventas: %d%n",    suc.getInteger("cantidadVentas"));
            System.out.printf("      Total cobranza:     $%.2f%n", suc.getDouble("totalCobranza"));
            System.out.printf("      Ticket promedio:    $%.2f%n", suc.getDouble("ticketPromedio"));
        }
        if (!haySucursales) {
            System.out.println("    Sin ventas en el período.");
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
}
