package farmacia;

import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;

import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import farmacia.model.AfiliacionCliente;
import farmacia.model.AfiliacionEmpleado;
import farmacia.model.Cliente;
import farmacia.model.DetalleVenta;
import farmacia.model.Domicilio;
import farmacia.model.Empleado;
import farmacia.model.Laboratorio;
import farmacia.model.ObraSocial;
import farmacia.model.Producto;
import farmacia.model.Sucursal;
import farmacia.model.Venta;

public class MongoLoader {

    private static final String URI = "mongodb+srv://josemanuelruival_db_user:"
            + System.getenv("MONGO_PASSWORD")
            + "@cluster0.rnvm0cw.mongodb.net/?appName=Cluster0";

    public static void main(String[] args) {

        String password = System.getenv("MONGO_PASSWORD");
        if (password == null) {
            System.err.println("ERROR: La variable de entorno MONGO_PASSWORD no está definida.");
            return;
        }

        // Obras Sociales
        ObraSocial osde  = new ObraSocial("OSDE");
        ObraSocial ioma  = new ObraSocial("IOMA");
        ObraSocial swiss = new ObraSocial("Swiss Medical");

        // Laboratorios
        Laboratorio bayer      = new Laboratorio("Bayer");
        Laboratorio genfar     = new Laboratorio("Genfar");
        Laboratorio roche      = new Laboratorio("Roche");
        Laboratorio pfizer     = new Laboratorio("Pfizer");
        Laboratorio unilever   = new Laboratorio("Unilever");
        Laboratorio beiersdorf = new Laboratorio("Beiersdorf");

        // Productos: 7 medicamentos + 3 perfumería
        Producto ibuprofeno      = new Producto(1,  "MEDICAMENTO", "Ibuprofeno 400mg",        850.0, bayer);
        Producto paracetamol     = new Producto(2,  "MEDICAMENTO", "Paracetamol 500mg",       620.0, genfar);
        Producto amoxicilina     = new Producto(3,  "MEDICAMENTO", "Amoxicilina 500mg",      1200.0, pfizer);
        Producto omeprazol       = new Producto(4,  "MEDICAMENTO", "Omeprazol 20mg",          980.0, roche);
        Producto losartan        = new Producto(5,  "MEDICAMENTO", "Losartán 50mg",           750.0, bayer);
        Producto aspirina        = new Producto(6,  "MEDICAMENTO", "Aspirina 100mg",          430.0, bayer);
        Producto loratadina      = new Producto(7,  "MEDICAMENTO", "Loratadina 10mg",         560.0, genfar);
        Producto shampoo         = new Producto(8,  "PERFUMERIA",  "Shampoo Pantene 400ml",  1800.0, unilever);
        Producto protectorSolar  = new Producto(9,  "PERFUMERIA",  "Protector Solar FPS50",  3200.0, beiersdorf);
        Producto cremaHidratante = new Producto(10, "PERFUMERIA",  "Crema Hidratante Nivea", 1500.0, beiersdorf);

        // Empleados – Sucursal 1 Córdoba
        Empleado garcia  = new Empleado(1, "García",    "Roberto", 25000001, 20250000011L,
                new Domicilio("San Martín", 450, "Córdoba",      "Córdoba"),
                List.of(new AfiliacionEmpleado(osde,  111222)));
        Empleado perez   = new Empleado(2, "Pérez",     "Carlos",  28000002, 20280000022L,
                new Domicilio("Colón",      300, "Córdoba",      "Córdoba"),
                List.of(new AfiliacionEmpleado(ioma,  333444)));
        Empleado sanchez = new Empleado(3, "Sánchez",   "Ana",     32000003, 27320000033L,
                new Domicilio("Rivadavia",  780, "Córdoba",      "Córdoba"),
                List.of(new AfiliacionEmpleado(swiss, 445566)));

        // Empleados – Sucursal 2 Buenos Aires
        Empleado lopez    = new Empleado(4, "López",    "María",   30000004, 27300000044L,
                new Domicilio("Belgrano",   120, "Buenos Aires", "Buenos Aires"),
                List.of(new AfiliacionEmpleado(ioma,  555666)));
        Empleado martinez = new Empleado(5, "Martínez", "Diego",   33000005, 20330000055L,
                new Domicilio("Corrientes", 850, "Buenos Aires", "Buenos Aires"),
                List.of(new AfiliacionEmpleado(osde,  667788)));
        Empleado torres   = new Empleado(6, "Torres",   "Claudia", 29000006, 27290000066L,
                new Domicilio("Santa Fe",  1100, "Buenos Aires", "Buenos Aires"),
                List.of(new AfiliacionEmpleado(swiss, 778899)));

        // Empleados – Sucursal 3 Rosario
        Empleado rodriguez = new Empleado(7, "Rodríguez", "Pablo",  31000007, 20310000077L,
                new Domicilio("Pellegrini", 450, "Rosario",      "Santa Fe"),
                List.of(new AfiliacionEmpleado(osde,  889900)));
        Empleado villalba  = new Empleado(8, "Villalba",  "Sofía",  34000008, 27340000088L,
                new Domicilio("Córdoba",    320, "Rosario",      "Santa Fe"),
                List.of(new AfiliacionEmpleado(ioma,  990011)));
        Empleado herrera   = new Empleado(9, "Herrera",   "Luis",   27000009, 20270000099L,
                new Domicilio("Entre Ríos", 670, "Rosario",      "Santa Fe"),
                List.of(new AfiliacionEmpleado(swiss, 112233)));

        // Sucursales
        Sucursal suc1 = new Sucursal(1, "San Martín",  500, "Córdoba",      "Córdoba",       3, garcia);
        Sucursal suc2 = new Sucursal(2, "Corrientes", 1200, "Buenos Aires", "Buenos Aires",  7, lopez);
        Sucursal suc3 = new Sucursal(3, "Pellegrini",  800, "Rosario",      "Santa Fe",     11, rodriguez);

        // Clientes (10)
        Cliente c1  = new Cliente(1,  "Gomez",     "Juan",      30123456, new Domicilio("Rivadavia",    123, "Córdoba",      "Córdoba"),      List.of(new AfiliacionCliente(osde,  987654)));
        Cliente c2  = new Cliente(2,  "Fernández", "Laura",     35987654, new Domicilio("Mitre",        456, "Buenos Aires", "Buenos Aires"), List.of(new AfiliacionCliente(ioma,  654321)));
        Cliente c3  = new Cliente(3,  "Ruiz",      "Marcos",    28500003, new Domicilio("Vélez",        210, "Córdoba",      "Córdoba"),      List.of(new AfiliacionCliente(swiss, 321098)));
        Cliente c4  = new Cliente(4,  "Molina",    "Beatriz",   32400004, new Domicilio("Lavalle",      890, "Buenos Aires", "Buenos Aires"), List.of(new AfiliacionCliente(osde,  210987)));
        Cliente c5  = new Cliente(5,  "Silva",     "Ricardo",   27300005, new Domicilio("Oroño",        340, "Rosario",      "Santa Fe"),     List.of(new AfiliacionCliente(ioma,  109876)));
        Cliente c6  = new Cliente(6,  "Castro",    "Valeria",   31200006, new Domicilio("General Paz",  560, "Córdoba",      "Córdoba"),      List.of(new AfiliacionCliente(swiss, 998877)));
        Cliente c7  = new Cliente(7,  "Díaz",      "Eduardo",   36100007, new Domicilio("Callao",       730, "Buenos Aires", "Buenos Aires"), List.of(new AfiliacionCliente(osde,  887766)));
        Cliente c8  = new Cliente(8,  "Moreno",    "Patricia",  29000008, new Domicilio("Sarmiento",    490, "Rosario",      "Santa Fe"),     List.of(new AfiliacionCliente(ioma,  776655)));
        Cliente c9  = new Cliente(9,  "Ortiz",     "Sebastián", 33900009, new Domicilio("Bv. Illia",    150, "Córdoba",      "Córdoba"),      List.of(new AfiliacionCliente(swiss, 665544)));
        Cliente c10 = new Cliente(10, "Reyes",     "Ana",       26800010, new Domicilio("Arenales",     820, "Buenos Aires", "Buenos Aires"), List.of(new AfiliacionCliente(osde,  554433)));

        List<Venta> ventas = List.of(

            // ═══════════════════════════════════════════════
            // SUCURSAL 1 – CÓRDOBA  (28 ventas, ids 1–28)
            // ═══════════════════════════════════════════════
            new Venta(1,  "2026-01-05", 1001,  850.0, "EFECTIVO",        c1, suc1, perez,   perez,   List.of(new DetalleVenta(1,  ibuprofeno,      1,  850.0,  850.0))),
            new Venta(2,  "2026-01-08", 1002,  620.0, "TARJETA_DEBITO",  c3, suc1, sanchez, sanchez, List.of(new DetalleVenta(2,  paracetamol,     1,  620.0,  620.0))),
            new Venta(3,  "2026-01-12", 1003, 1200.0, "OBRA_SOCIAL",     c6, suc1, perez,   garcia,  List.of(new DetalleVenta(3,  amoxicilina,     1, 1200.0, 1200.0))),
            new Venta(4,  "2026-01-15", 1004,  980.0, "TARJETA_CREDITO", c9, suc1, sanchez, sanchez, List.of(new DetalleVenta(4,  omeprazol,       1,  980.0,  980.0))),
            new Venta(5,  "2026-01-19", 1005,  750.0, "EFECTIVO",        c1, suc1, garcia,  garcia,  List.of(new DetalleVenta(5,  losartan,        1,  750.0,  750.0))),
            new Venta(6,  "2026-01-22", 1006,  430.0, "TARJETA_DEBITO",  c3, suc1, perez,   perez,   List.of(new DetalleVenta(6,  aspirina,        1,  430.0,  430.0))),
            new Venta(7,  "2026-01-26", 1007,  560.0, "OBRA_SOCIAL",     c6, suc1, sanchez, garcia,  List.of(new DetalleVenta(7,  loratadina,      1,  560.0,  560.0))),
            new Venta(8,  "2026-01-29", 1008, 1800.0, "EFECTIVO",        c9, suc1, perez,   perez,   List.of(new DetalleVenta(8,  shampoo,         1, 1800.0, 1800.0))),
            new Venta(9,  "2026-02-02", 1009, 3200.0, "TARJETA_CREDITO", c1, suc1, sanchez, sanchez, List.of(new DetalleVenta(9,  protectorSolar,  1, 3200.0, 3200.0))),
            new Venta(10, "2026-02-05", 1010, 1500.0, "EFECTIVO",        c3, suc1, perez,   garcia,  List.of(new DetalleVenta(10, cremaHidratante, 1, 1500.0, 1500.0))),
            new Venta(11, "2026-02-09", 1011, 1410.0, "TARJETA_DEBITO",  c6, suc1, perez,   perez,   List.of(new DetalleVenta(11, ibuprofeno,      1,  850.0,  850.0), new DetalleVenta(12, loratadina,      1,  560.0,  560.0))),
            new Venta(12, "2026-02-12", 1012, 1670.0, "OBRA_SOCIAL",     c9, suc1, sanchez, garcia,  List.of(new DetalleVenta(13, paracetamol,     2,  620.0, 1240.0), new DetalleVenta(14, aspirina,        1,  430.0,  430.0))),
            new Venta(13, "2026-02-16", 1013, 2180.0, "EFECTIVO",        c1, suc1, perez,   perez,   List.of(new DetalleVenta(15, amoxicilina,     1, 1200.0, 1200.0), new DetalleVenta(16, omeprazol,       1,  980.0,  980.0))),
            new Venta(14, "2026-02-19", 1014, 2120.0, "TARJETA_CREDITO", c3, suc1, garcia,  garcia,  List.of(new DetalleVenta(17, losartan,        2,  750.0, 1500.0), new DetalleVenta(18, paracetamol,     1,  620.0,  620.0))),
            new Venta(15, "2026-02-23", 1015, 2130.0, "EFECTIVO",        c6, suc1, sanchez, sanchez, List.of(new DetalleVenta(19, ibuprofeno,      2,  850.0, 1700.0), new DetalleVenta(20, aspirina,        1,  430.0,  430.0))),
            new Venta(16, "2026-02-26", 1016, 2360.0, "TARJETA_DEBITO",  c9, suc1, perez,   garcia,  List.of(new DetalleVenta(21, loratadina,      1,  560.0,  560.0), new DetalleVenta(22, shampoo,         1, 1800.0, 1800.0))),
            new Venta(17, "2026-03-02", 1017, 2120.0, "OBRA_SOCIAL",     c1, suc1, sanchez, sanchez, List.of(new DetalleVenta(23, cremaHidratante, 1, 1500.0, 1500.0), new DetalleVenta(24, paracetamol,     1,  620.0,  620.0))),
            new Venta(18, "2026-03-05", 1018, 4060.0, "TARJETA_CREDITO", c3, suc1, perez,   garcia,  List.of(new DetalleVenta(25, protectorSolar,  1, 3200.0, 3200.0), new DetalleVenta(26, aspirina,        2,  430.0,  860.0))),
            new Venta(19, "2026-03-09", 1019, 1730.0, "EFECTIVO",        c6, suc1, garcia,  garcia,  List.of(new DetalleVenta(27, omeprazol,       1,  980.0,  980.0), new DetalleVenta(28, losartan,        1,  750.0,  750.0))),
            new Venta(20, "2026-03-12", 1020, 2320.0, "TARJETA_DEBITO",  c9, suc1, perez,   perez,   List.of(new DetalleVenta(29, amoxicilina,     1, 1200.0, 1200.0), new DetalleVenta(30, loratadina,      2,  560.0, 1120.0))),
            new Venta(21, "2026-03-16", 1021, 2650.0, "OBRA_SOCIAL",     c1, suc1, sanchez, garcia,  List.of(new DetalleVenta(31, shampoo,         1, 1800.0, 1800.0), new DetalleVenta(32, ibuprofeno,      1,  850.0,  850.0))),
            new Venta(22, "2026-03-19", 1022, 1370.0, "EFECTIVO",        c3, suc1, perez,   perez,   List.of(new DetalleVenta(33, paracetamol,     1,  620.0,  620.0), new DetalleVenta(34, losartan,        1,  750.0,  750.0))),
            new Venta(23, "2026-03-23", 1023, 2480.0, "TARJETA_CREDITO", c6, suc1, garcia,  garcia,  List.of(new DetalleVenta(35, cremaHidratante, 1, 1500.0, 1500.0), new DetalleVenta(36, omeprazol,       1,  980.0,  980.0))),
            new Venta(24, "2026-04-01", 1024, 1900.0, "EFECTIVO",        c9, suc1, perez,   perez,   List.of(new DetalleVenta(37, ibuprofeno,      1,  850.0,  850.0), new DetalleVenta(38, paracetamol,     1,  620.0,  620.0), new DetalleVenta(39, aspirina,        1,  430.0,  430.0))),
            new Venta(25, "2026-04-06", 1025, 2740.0, "TARJETA_DEBITO",  c1, suc1, sanchez, garcia,  List.of(new DetalleVenta(40, amoxicilina,     1, 1200.0, 1200.0), new DetalleVenta(41, omeprazol,       1,  980.0,  980.0), new DetalleVenta(42, loratadina,      1,  560.0,  560.0))),
            new Venta(26, "2026-04-13", 1026, 3730.0, "OBRA_SOCIAL",     c3, suc1, perez,   garcia,  List.of(new DetalleVenta(43, losartan,        2,  750.0, 1500.0), new DetalleVenta(44, shampoo,         1, 1800.0, 1800.0), new DetalleVenta(45, aspirina,        1,  430.0,  430.0))),
            new Venta(27, "2026-04-20", 1027, 5550.0, "TARJETA_CREDITO", c6, suc1, garcia,  garcia,  List.of(new DetalleVenta(46, protectorSolar,  1, 3200.0, 3200.0), new DetalleVenta(47, cremaHidratante, 1, 1500.0, 1500.0), new DetalleVenta(48, ibuprofeno,      1,  850.0,  850.0))),
            new Venta(28, "2026-05-05", 1028, 2780.0, "EFECTIVO",        c9, suc1, sanchez, sanchez, List.of(new DetalleVenta(49, paracetamol,     2,  620.0, 1240.0), new DetalleVenta(50, loratadina,      1,  560.0,  560.0), new DetalleVenta(51, omeprazol,       1,  980.0,  980.0))),

            // ═══════════════════════════════════════════════
            // SUCURSAL 2 – BUENOS AIRES  (32 ventas, ids 29–60)
            // ═══════════════════════════════════════════════
            new Venta(29, "2026-01-06", 1029,  850.0, "TARJETA_DEBITO",  c2,  suc2, martinez, martinez, List.of(new DetalleVenta(52, ibuprofeno,      1,  850.0,  850.0))),
            new Venta(30, "2026-01-09", 1030,  620.0, "EFECTIVO",        c4,  suc2, torres,   torres,   List.of(new DetalleVenta(53, paracetamol,     1,  620.0,  620.0))),
            new Venta(31, "2026-01-13", 1031, 1200.0, "TARJETA_CREDITO", c7,  suc2, martinez, lopez,    List.of(new DetalleVenta(54, amoxicilina,     1, 1200.0, 1200.0))),
            new Venta(32, "2026-01-16", 1032,  980.0, "OBRA_SOCIAL",     c10, suc2, torres,   torres,   List.of(new DetalleVenta(55, omeprazol,       1,  980.0,  980.0))),
            new Venta(33, "2026-01-20", 1033,  750.0, "EFECTIVO",        c2,  suc2, lopez,    lopez,    List.of(new DetalleVenta(56, losartan,        1,  750.0,  750.0))),
            new Venta(34, "2026-01-23", 1034,  860.0, "TARJETA_DEBITO",  c4,  suc2, martinez, martinez, List.of(new DetalleVenta(57, aspirina,        2,  430.0,  860.0))),
            new Venta(35, "2026-01-27", 1035,  560.0, "OBRA_SOCIAL",     c7,  suc2, torres,   lopez,    List.of(new DetalleVenta(58, loratadina,      1,  560.0,  560.0))),
            new Venta(36, "2026-01-30", 1036, 1800.0, "TARJETA_CREDITO", c10, suc2, martinez, martinez, List.of(new DetalleVenta(59, shampoo,         1, 1800.0, 1800.0))),
            new Venta(37, "2026-02-03", 1037, 3200.0, "EFECTIVO",        c2,  suc2, torres,   torres,   List.of(new DetalleVenta(60, protectorSolar,  1, 3200.0, 3200.0))),
            new Venta(38, "2026-02-06", 1038, 1500.0, "TARJETA_DEBITO",  c4,  suc2, lopez,    lopez,    List.of(new DetalleVenta(61, cremaHidratante, 1, 1500.0, 1500.0))),
            new Venta(39, "2026-02-10", 1039, 1700.0, "OBRA_SOCIAL",     c7,  suc2, martinez, lopez,    List.of(new DetalleVenta(62, ibuprofeno,      2,  850.0, 1700.0))),
            new Venta(40, "2026-02-13", 1040,  620.0, "EFECTIVO",        c10, suc2, torres,   torres,   List.of(new DetalleVenta(63, paracetamol,     1,  620.0,  620.0))),
            new Venta(41, "2026-02-17", 1041, 2050.0, "TARJETA_CREDITO", c2,  suc2, martinez, martinez, List.of(new DetalleVenta(64, amoxicilina,     1, 1200.0, 1200.0), new DetalleVenta(65, ibuprofeno,      1,  850.0,  850.0))),
            new Venta(42, "2026-02-20", 1042, 1600.0, "EFECTIVO",        c4,  suc2, torres,   lopez,    List.of(new DetalleVenta(66, omeprazol,       1,  980.0,  980.0), new DetalleVenta(67, paracetamol,     1,  620.0,  620.0))),
            new Venta(43, "2026-02-24", 1043, 1310.0, "TARJETA_DEBITO",  c7,  suc2, lopez,    lopez,    List.of(new DetalleVenta(68, losartan,        1,  750.0,  750.0), new DetalleVenta(69, loratadina,      1,  560.0,  560.0))),
            new Venta(44, "2026-02-27", 1044, 1480.0, "OBRA_SOCIAL",     c10, suc2, martinez, martinez, List.of(new DetalleVenta(70, aspirina,        2,  430.0,  860.0), new DetalleVenta(71, paracetamol,     1,  620.0,  620.0))),
            new Venta(45, "2026-03-03", 1045, 2650.0, "EFECTIVO",        c2,  suc2, torres,   lopez,    List.of(new DetalleVenta(72, shampoo,         1, 1800.0, 1800.0), new DetalleVenta(73, ibuprofeno,      1,  850.0,  850.0))),
            new Venta(46, "2026-03-06", 1046, 4180.0, "TARJETA_CREDITO", c4,  suc2, martinez, martinez, List.of(new DetalleVenta(74, protectorSolar,  1, 3200.0, 3200.0), new DetalleVenta(75, omeprazol,       1,  980.0,  980.0))),
            new Venta(47, "2026-03-10", 1047, 2250.0, "TARJETA_DEBITO",  c7,  suc2, lopez,    lopez,    List.of(new DetalleVenta(76, cremaHidratante, 1, 1500.0, 1500.0), new DetalleVenta(77, losartan,        1,  750.0,  750.0))),
            new Venta(48, "2026-03-13", 1048, 2260.0, "EFECTIVO",        c10, suc2, torres,   torres,   List.of(new DetalleVenta(78, ibuprofeno,      2,  850.0, 1700.0), new DetalleVenta(79, loratadina,      1,  560.0,  560.0))),
            new Venta(49, "2026-03-17", 1049, 1670.0, "OBRA_SOCIAL",     c2,  suc2, martinez, lopez,    List.of(new DetalleVenta(80, paracetamol,     2,  620.0, 1240.0), new DetalleVenta(81, aspirina,        1,  430.0,  430.0))),
            new Venta(50, "2026-03-20", 1050, 3000.0, "TARJETA_CREDITO", c4,  suc2, torres,   torres,   List.of(new DetalleVenta(82, amoxicilina,     1, 1200.0, 1200.0), new DetalleVenta(83, shampoo,         1, 1800.0, 1800.0))),
            new Venta(51, "2026-03-24", 1051, 2480.0, "EFECTIVO",        c7,  suc2, lopez,    lopez,    List.of(new DetalleVenta(84, omeprazol,       1,  980.0,  980.0), new DetalleVenta(85, cremaHidratante, 1, 1500.0, 1500.0))),
            new Venta(52, "2026-03-27", 1052, 1930.0, "TARJETA_DEBITO",  c10, suc2, martinez, martinez, List.of(new DetalleVenta(86, losartan,        2,  750.0, 1500.0), new DetalleVenta(87, aspirina,        1,  430.0,  430.0))),
            new Venta(53, "2026-04-01", 1053, 1740.0, "OBRA_SOCIAL",     c2,  suc2, torres,   lopez,    List.of(new DetalleVenta(88, loratadina,      2,  560.0, 1120.0), new DetalleVenta(89, paracetamol,     1,  620.0,  620.0))),
            new Venta(54, "2026-04-07", 1054, 1830.0, "EFECTIVO",        c4,  suc2, martinez, martinez, List.of(new DetalleVenta(90, ibuprofeno,      1,  850.0,  850.0), new DetalleVenta(91, omeprazol,       1,  980.0,  980.0))),
            new Venta(55, "2026-04-14", 1055, 2030.0, "TARJETA_CREDITO", c7,  suc2, lopez,    lopez,    List.of(new DetalleVenta(92, ibuprofeno,      1,  850.0,  850.0), new DetalleVenta(93, paracetamol,     1,  620.0,  620.0), new DetalleVenta(94, loratadina,      1,  560.0,  560.0))),
            new Venta(56, "2026-04-21", 1056, 3040.0, "TARJETA_DEBITO",  c10, suc2, torres,   torres,   List.of(new DetalleVenta(95, amoxicilina,     1, 1200.0, 1200.0), new DetalleVenta(96, aspirina,        2,  430.0,  860.0), new DetalleVenta(97, omeprazol,       1,  980.0,  980.0))),
            new Venta(57, "2026-04-28", 1057, 3790.0, "EFECTIVO",        c2,  suc2, martinez, lopez,    List.of(new DetalleVenta(98, losartan,        1,  750.0,  750.0), new DetalleVenta(99, shampoo,         1, 1800.0, 1800.0), new DetalleVenta(100, paracetamol,    2,  620.0, 1240.0))),
            new Venta(58, "2026-05-05", 1058, 6400.0, "TARJETA_CREDITO", c4,  suc2, lopez,    lopez,    List.of(new DetalleVenta(101, protectorSolar, 1, 3200.0, 3200.0), new DetalleVenta(102, cremaHidratante,1, 1500.0, 1500.0), new DetalleVenta(103, ibuprofeno,     2,  850.0, 1700.0))),
            new Venta(59, "2026-05-12", 1059, 2190.0, "OBRA_SOCIAL",     c7,  suc2, torres,   torres,   List.of(new DetalleVenta(104, amoxicilina,    1, 1200.0, 1200.0), new DetalleVenta(105, loratadina,     1,  560.0,  560.0), new DetalleVenta(106, aspirina,       1,  430.0,  430.0))),
            new Venta(60, "2026-05-19", 1060, 3560.0, "EFECTIVO",        c10, suc2, martinez, martinez, List.of(new DetalleVenta(107, omeprazol,      2,  980.0, 1960.0), new DetalleVenta(108, losartan,       1,  750.0,  750.0), new DetalleVenta(109, ibuprofeno,     1,  850.0,  850.0))),

            // ═══════════════════════════════════════════════
            // SUCURSAL 3 – ROSARIO  (30 ventas, ids 61–90)
            // ═══════════════════════════════════════════════
            new Venta(61, "2026-01-07", 1061,  850.0, "EFECTIVO",        c5, suc3, villalba,  villalba,  List.of(new DetalleVenta(110, ibuprofeno,     1,  850.0,  850.0))),
            new Venta(62, "2026-01-10", 1062, 1240.0, "OBRA_SOCIAL",     c8, suc3, herrera,   herrera,   List.of(new DetalleVenta(111, paracetamol,    2,  620.0, 1240.0))),
            new Venta(63, "2026-01-14", 1063, 1200.0, "TARJETA_DEBITO",  c5, suc3, rodriguez, rodriguez, List.of(new DetalleVenta(112, amoxicilina,    1, 1200.0, 1200.0))),
            new Venta(64, "2026-01-17", 1064,  980.0, "EFECTIVO",        c8, suc3, villalba,  villalba,  List.of(new DetalleVenta(113, omeprazol,      1,  980.0,  980.0))),
            new Venta(65, "2026-01-21", 1065,  750.0, "TARJETA_CREDITO", c5, suc3, herrera,   rodriguez, List.of(new DetalleVenta(114, losartan,       1,  750.0,  750.0))),
            new Venta(66, "2026-01-24", 1066,  430.0, "TARJETA_DEBITO",  c8, suc3, rodriguez, rodriguez, List.of(new DetalleVenta(115, aspirina,       1,  430.0,  430.0))),
            new Venta(67, "2026-01-28", 1067, 1800.0, "OBRA_SOCIAL",     c5, suc3, villalba,  villalba,  List.of(new DetalleVenta(116, shampoo,        1, 1800.0, 1800.0))),
            new Venta(68, "2026-01-31", 1068, 3200.0, "EFECTIVO",        c8, suc3, herrera,   herrera,   List.of(new DetalleVenta(117, protectorSolar, 1, 3200.0, 3200.0))),
            new Venta(69, "2026-02-04", 1069, 1470.0, "TARJETA_CREDITO", c5, suc3, villalba,  rodriguez, List.of(new DetalleVenta(118, ibuprofeno,     1,  850.0,  850.0), new DetalleVenta(119, paracetamol,    1,  620.0,  620.0))),
            new Venta(70, "2026-02-07", 1070, 1630.0, "EFECTIVO",        c8, suc3, herrera,   herrera,   List.of(new DetalleVenta(120, amoxicilina,    1, 1200.0, 1200.0), new DetalleVenta(121, aspirina,       1,  430.0,  430.0))),
            new Venta(71, "2026-02-11", 1071, 1730.0, "TARJETA_DEBITO",  c5, suc3, rodriguez, rodriguez, List.of(new DetalleVenta(122, omeprazol,      1,  980.0,  980.0), new DetalleVenta(123, losartan,       1,  750.0,  750.0))),
            new Venta(72, "2026-02-14", 1072, 1970.0, "OBRA_SOCIAL",     c8, suc3, villalba,  villalba,  List.of(new DetalleVenta(124, loratadina,     2,  560.0, 1120.0), new DetalleVenta(125, ibuprofeno,     1,  850.0,  850.0))),
            new Venta(73, "2026-02-18", 1073, 2420.0, "EFECTIVO",        c5, suc3, herrera,   rodriguez, List.of(new DetalleVenta(126, shampoo,        1, 1800.0, 1800.0), new DetalleVenta(127, paracetamol,    1,  620.0,  620.0))),
            new Venta(74, "2026-02-21", 1074, 2350.0, "TARJETA_CREDITO", c8, suc3, rodriguez, rodriguez, List.of(new DetalleVenta(128, cremaHidratante,1, 1500.0, 1500.0), new DetalleVenta(129, ibuprofeno,     1,  850.0,  850.0))),
            new Venta(75, "2026-02-25", 1075, 3630.0, "TARJETA_DEBITO",  c5, suc3, villalba,  villalba,  List.of(new DetalleVenta(130, protectorSolar, 1, 3200.0, 3200.0), new DetalleVenta(131, aspirina,       1,  430.0,  430.0))),
            new Venta(76, "2026-02-28", 1076, 1820.0, "OBRA_SOCIAL",     c8, suc3, herrera,   herrera,   List.of(new DetalleVenta(132, amoxicilina,    1, 1200.0, 1200.0), new DetalleVenta(133, paracetamol,    1,  620.0,  620.0))),
            new Venta(77, "2026-03-04", 1077, 1310.0, "EFECTIVO",        c5, suc3, rodriguez, rodriguez, List.of(new DetalleVenta(134, losartan,       1,  750.0,  750.0), new DetalleVenta(135, loratadina,     1,  560.0,  560.0))),
            new Venta(78, "2026-03-07", 1078, 2780.0, "TARJETA_CREDITO", c8, suc3, villalba,  rodriguez, List.of(new DetalleVenta(136, omeprazol,      1,  980.0,  980.0), new DetalleVenta(137, shampoo,        1, 1800.0, 1800.0))),
            new Venta(79, "2026-03-11", 1079, 2130.0, "TARJETA_DEBITO",  c5, suc3, herrera,   herrera,   List.of(new DetalleVenta(138, ibuprofeno,     2,  850.0, 1700.0), new DetalleVenta(139, aspirina,       1,  430.0,  430.0))),
            new Venta(80, "2026-03-14", 1080, 2740.0, "EFECTIVO",        c8, suc3, rodriguez, rodriguez, List.of(new DetalleVenta(140, cremaHidratante,1, 1500.0, 1500.0), new DetalleVenta(141, paracetamol,    2,  620.0, 1240.0))),
            new Venta(81, "2026-03-18", 1081, 1950.0, "OBRA_SOCIAL",     c5, suc3, villalba,  villalba,  List.of(new DetalleVenta(142, amoxicilina,    1, 1200.0, 1200.0), new DetalleVenta(143, losartan,       1,  750.0,  750.0))),
            new Venta(82, "2026-03-21", 1082, 1540.0, "TARJETA_DEBITO",  c8, suc3, herrera,   herrera,   List.of(new DetalleVenta(144, loratadina,     1,  560.0,  560.0), new DetalleVenta(145, omeprazol,      1,  980.0,  980.0))),
            new Venta(83, "2026-03-25", 1083, 2350.0, "EFECTIVO",        c5, suc3, rodriguez, rodriguez, List.of(new DetalleVenta(146, ibuprofeno,     1,  850.0,  850.0), new DetalleVenta(147, cremaHidratante,1, 1500.0, 1500.0))),
            new Venta(84, "2026-03-28", 1084, 2420.0, "TARJETA_CREDITO", c8, suc3, villalba,  villalba,  List.of(new DetalleVenta(148, paracetamol,    1,  620.0,  620.0), new DetalleVenta(149, shampoo,        1, 1800.0, 1800.0))),
            new Venta(85, "2026-04-04", 1085, 1610.0, "OBRA_SOCIAL",     c5, suc3, herrera,   rodriguez, List.of(new DetalleVenta(150, aspirina,       2,  430.0,  860.0), new DetalleVenta(151, losartan,       1,  750.0,  750.0))),
            new Venta(86, "2026-04-11", 1086, 3760.0, "EFECTIVO",        c8, suc3, rodriguez, rodriguez, List.of(new DetalleVenta(152, protectorSolar, 1, 3200.0, 3200.0), new DetalleVenta(153, loratadina,     1,  560.0,  560.0))),
            new Venta(87, "2026-04-18", 1087, 2670.0, "TARJETA_DEBITO",  c5, suc3, villalba,  villalba,  List.of(new DetalleVenta(154, ibuprofeno,     1,  850.0,  850.0), new DetalleVenta(155, paracetamol,    1,  620.0,  620.0), new DetalleVenta(156, amoxicilina,    1, 1200.0, 1200.0))),
            new Venta(88, "2026-04-25", 1088, 2590.0, "OBRA_SOCIAL",     c8, suc3, herrera,   herrera,   List.of(new DetalleVenta(157, omeprazol,      1,  980.0,  980.0), new DetalleVenta(158, losartan,       1,  750.0,  750.0), new DetalleVenta(159, aspirina,       2,  430.0,  860.0))),
            new Venta(89, "2026-05-09", 1089, 3860.0, "TARJETA_CREDITO", c5, suc3, rodriguez, rodriguez, List.of(new DetalleVenta(160, shampoo,        1, 1800.0, 1800.0), new DetalleVenta(161, cremaHidratante,1, 1500.0, 1500.0), new DetalleVenta(162, loratadina,     1,  560.0,  560.0))),
            new Venta(90, "2026-05-16", 1090, 5520.0, "EFECTIVO",        c8, suc3, villalba,  villalba,  List.of(new DetalleVenta(163, protectorSolar, 1, 3200.0, 3200.0), new DetalleVenta(164, ibuprofeno,     2,  850.0, 1700.0), new DetalleVenta(165, paracetamol,    1,  620.0,  620.0)))
        );

        Gson gson = new Gson();
        List<Document> documentos = ventas.stream()
                .map(v -> Document.parse(gson.toJson(v)))
                .collect(Collectors.toList());

        try (MongoClient client = MongoClients.create(URI)) {
            MongoDatabase db = client.getDatabase("farmacia");
            MongoCollection<Document> coleccion = db.getCollection("ventas");

            coleccion.drop();
            coleccion.insertMany(documentos);
            System.out.println("Colección recreada. Ventas insertadas: " + documentos.size());
        }
    }
}
