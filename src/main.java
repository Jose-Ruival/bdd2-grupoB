package farmacia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import farmacia.model.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {

        // --- Obras sociales ---
        ObraSocial osde  = new ObraSocial("OSDE");
        ObraSocial ioma  = new ObraSocial("IOMA");
        ObraSocial swiss = new ObraSocial("Swiss Medical");

        // --- Laboratorios ---
        Laboratorio bayer    = new Laboratorio("Bayer");
        Laboratorio genfar   = new Laboratorio("Genfar");
        Laboratorio roemmers = new Laboratorio("Roemmers");

        // --- Productos
        // Estos mismos objetos van a quedar EMBEBIDOS en múltiples ventas,
        // haciendo visible la desnormalización: sus datos se repiten en cada DetalleVenta.
        Producto ibuprofeno  = new Producto(1, "MEDICAMENTO", "Ibuprofeno 400mg",  850.0, bayer);
        Producto paracetamol = new Producto(2, "MEDICAMENTO", "Paracetamol 500mg", 620.0, genfar);
        Producto amoxicilina = new Producto(3, "MEDICAMENTO", "Amoxicilina 500mg", 1200.0, roemmers);

        // --- Empleados ---
        Empleado encargado1 = new Empleado(1, "García", "Roberto", 25000001, 20250000011L,
                new Domicilio("San Martín", 450, "Córdoba", "Córdoba"),
                List.of(new AfiliacionEmpleado(osde, 111222)));

        Empleado farmaceutico1 = new Empleado(2, "Pérez", "Carlos", 28000002, 20280000022L,
                new Domicilio("Colón", 300, "Córdoba", "Córdoba"),
                List.of(new AfiliacionEmpleado(ioma, 333444)));

        Empleado encargado2 = new Empleado(3, "López", "María", 30000003, 27300000033L,
                new Domicilio("Belgrano", 120, "Buenos Aires", "Buenos Aires"),
                List.of(new AfiliacionEmpleado(swiss, 555666)));

        Empleado farmaceutico2 = new Empleado(4, "Díaz", "Ana", 32000004, 27320000044L,
                new Domicilio("Rivadavia", 890, "Buenos Aires", "Buenos Aires"),
                List.of(new AfiliacionEmpleado(osde, 777888)));

        // --- Sucursales ---
        Sucursal sucursal1 = new Sucursal(1, "San Martín", 500, "Córdoba", "Córdoba", 3, encargado1);
        Sucursal sucursal2 = new Sucursal(2, "Corrientes", 1200, "Buenos Aires", "Buenos Aires", 7, encargado2);

        // --- Clientes ---
        Cliente cliente1 = new Cliente(1, "Gomez", "Juan", 30123456,
                new Domicilio("Rivadavia", 123, "Córdoba", "Córdoba"),
                List.of(new AfiliacionCliente(osde, 987654)));

        Cliente cliente2 = new Cliente(2, "Fernández", "Laura", 35987654,
                new Domicilio("Mitre", 456, "Buenos Aires", "Buenos Aires"),
                List.of(new AfiliacionCliente(ioma, 654321), new AfiliacionCliente(swiss, 112233)));

        Cliente cliente3 = new Cliente(3, "Torres", "Marcos", 28765432,
                new Domicilio("Moreno", 789, "Rosario", "Santa Fe"),
                List.of());

        // --- Ventas (documento raíz desnormalizado) ---
        // Notar que ibuprofeno, paracetamol y amoxicilina aparecen con todos sus datos
        // repetidos en distintas ventas: esa redundancia es la esencia de la desnormalización.
        List<Venta> ventas = List.of(

            new Venta(1, "2026-05-10", 1001, 2320.0, "EFECTIVO",
                cliente1, sucursal1, farmaceutico1, farmaceutico1,
                List.of(
                    new DetalleVenta(1, ibuprofeno,  2, 850.0,  1700.0),
                    new DetalleVenta(2, paracetamol, 1, 620.0,   620.0)
                )),

            new Venta(2, "2026-05-11", 1002, 3600.0, "TARJETA_DEBITO",
                cliente2, sucursal1, farmaceutico1, farmaceutico1,
                List.of(
                    new DetalleVenta(3, ibuprofeno,  2,  850.0, 1700.0),   // ibuprofeno repetido
                    new DetalleVenta(4, amoxicilina, 1, 1200.0, 1200.0),
                    new DetalleVenta(5, paracetamol, 1,  620.0,  620.0)    // paracetamol repetido
                )),

            new Venta(3, "2026-05-12", 1003, 2400.0, "OBRA_SOCIAL",
                cliente1, sucursal2, farmaceutico2, farmaceutico2,
                List.of(
                    new DetalleVenta(6, amoxicilina, 2, 1200.0, 2400.0)   // amoxicilina repetida
                )),

            new Venta(4, "2026-05-13", 1004, 1240.0, "EFECTIVO",
                cliente3, sucursal2, farmaceutico2, farmaceutico2,
                List.of(
                    new DetalleVenta(7, paracetamol, 2, 620.0, 1240.0)    // paracetamol repetido (3ra vez)
                )),

            new Venta(5, "2026-05-14", 1005, 850.0, "TARJETA_CREDITO",
                cliente2, sucursal1, farmaceutico1, farmaceutico1,
                List.of(
                    new DetalleVenta(8, ibuprofeno, 1, 850.0, 850.0)      // ibuprofeno repetido (3ra vez)
                ))
        );

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(ventas);

        try (FileWriter writer = new FileWriter("ventas.json")) {
            writer.write(json);
        }

        System.out.println("ventas.json generado. Total ventas: " + ventas.size());
    }
}
